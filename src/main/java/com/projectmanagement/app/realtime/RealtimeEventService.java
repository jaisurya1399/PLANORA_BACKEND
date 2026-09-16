package com.projectmanagement.app.realtime;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Fan-out for SSE events.
 *
 * IMPORTANT: publishProject()/publishUser() are called from inside
 *
 * @Transactional service methods (chat, comments, ticket updates, etc.).
 *                SseEmitter#send() is a BLOCKING socket write. If it runs on
 *                the calling
 *                thread, one slow/dead client can hold that thread - and the DB
 *                connection/transaction it's carrying - hostage. Under load
 *                this exhausts
 *                the Hikari pool and Tomcat's thread pool, which is why
 *                unrelated API
 *                calls slow down as the number of connected users grows.
 *
 *                Fix: every send is dispatched to a small dedicated executor,
 *                so the
 *                caller (and its DB transaction) never waits on client network
 *                I/O.
 *                The executor's queue is bounded; if it's ever overwhelmed we
 *                drop the
 *                event for that one emitter and disconnect it rather than let
 *                work pile
 *                up unboundedly or let a caller block.
 */
@Service
public class RealtimeEventService {
    private static final Logger log = LoggerFactory.getLogger(RealtimeEventService.class);
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, Set<SseEmitter>> projectEmitters = new ConcurrentHashMap<>();
    private final Map<Long, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    // Bounded pool + bounded queue: sends never block a request thread,
    // and a burst of slow clients can't cause unbounded memory growth.
    private final ThreadPoolExecutor sseExecutor = new ThreadPoolExecutor(
            4, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> {
                Thread t = new Thread(r, "sse-publisher");
                t.setDaemon(true);
                return t;
            },
            (r, executor) -> log.warn("SSE publisher queue full, dropping an event"));

    public SseEmitter subscribeProject(Long projectId) {
        return subscribe(projectEmitters, projectId);
    }

    public SseEmitter subscribeUser(Long userId) {
        return subscribe(userEmitters, userId);
    }

    public void publishProject(Long projectId, String type, Object payload) {
        publish(projectEmitters.get(projectId), type, payload);
    }

    public void publishUser(Long userId, String type, Object payload) {
        publish(userEmitters.get(userId), type, payload);
    }

    private SseEmitter subscribe(Map<Long, Set<SseEmitter>> groups, Long key) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        Set<SseEmitter> emitters = groups.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet());
        emitters.add(emitter);
        Runnable remove = () -> {
            emitters.remove(emitter);
            if (emitters.isEmpty())
                groups.remove(key, emitters);
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());
        sendAsync(emitter, "connected", Map.of("stream", key), remove);
        return emitter;
    }

    private void publish(Set<SseEmitter> emitters, String type, Object payload) {
        if (emitters == null || emitters.isEmpty())
            return;
        // Snapshot: avoids re-iterating a live set if it mutates mid-publish.
        for (SseEmitter emitter : List.copyOf(emitters)) {
            Runnable remove = () -> emitters.remove(emitter);
            sendAsync(emitter, type, payload, remove);
        }
    }

    private void sendAsync(SseEmitter emitter, String type, Object payload, Runnable onFailure) {
        try {
            sseExecutor.execute(() -> {
                try {
                    emitter.send(SseEmitter.event().name(type).data(payload));
                } catch (IOException | IllegalStateException ex) {
                    emitter.completeWithError(ex);
                    onFailure.run();
                }
            });
        } catch (RejectedExecutionException ex) {
            // Queue is full - don't let this back up onto the caller. Drop the
            // client instead of the guarantee that publishers never block.
            emitter.completeWithError(ex);
            onFailure.run();
        }
    }
}
