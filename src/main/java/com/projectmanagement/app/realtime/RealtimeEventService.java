package com.projectmanagement.app.realtime;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class RealtimeEventService {
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;
    private final Map<Long, Set<SseEmitter>> projectEmitters = new ConcurrentHashMap<>();
    private final Map<Long, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    public SseEmitter subscribeProject(Long projectId) { return subscribe(projectEmitters, projectId); }
    public SseEmitter subscribeUser(Long userId) { return subscribe(userEmitters, userId); }
    public void publishProject(Long projectId, String type, Object payload) { publish(projectEmitters.get(projectId), type, payload); }
    public void publishUser(Long userId, String type, Object payload) { publish(userEmitters.get(userId), type, payload); }
    private SseEmitter subscribe(Map<Long, Set<SseEmitter>> groups, Long key) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS); Set<SseEmitter> emitters = groups.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()); emitters.add(emitter);
        Runnable remove = () -> { emitters.remove(emitter); if (emitters.isEmpty()) groups.remove(key, emitters); };
        emitter.onCompletion(remove); emitter.onTimeout(remove); emitter.onError(error -> remove.run());
        try { emitter.send(SseEmitter.event().name("connected").data(Map.of("stream", key))); } catch (IOException ignored) { remove.run(); }
        return emitter;
    }
    private void publish(Set<SseEmitter> emitters, String type, Object payload) {
        if (emitters == null) return;
        for (SseEmitter emitter : emitters) try { emitter.send(SseEmitter.event().name(type).data(payload)); } catch (IOException exception) { emitter.complete(); }
    }
}
