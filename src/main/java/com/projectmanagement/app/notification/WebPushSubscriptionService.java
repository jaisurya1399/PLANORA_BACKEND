package com.projectmanagement.app.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.user.User;

@Service
@Transactional
public class WebPushSubscriptionService {
    private final WebPushSubscriptionRepository repository;
    private final CurrentUserService currentUserService;

    public WebPushSubscriptionService(WebPushSubscriptionRepository repository, CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    public void upsert(WebPushSubscriptionRequest request) {
        User user = currentUserService.getCurrentUser();
        WebPushSubscription subscription = repository.findByEndpoint(request.getEndpoint())
                .orElseGet(WebPushSubscription::new);
        if (subscription.getId() != null && !subscription.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Push subscription belongs to another user");
        }
        subscription.setUser(user);
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getP256dh());
        subscription.setAuth(request.getAuth());
        repository.save(subscription);
    }

    public void remove(String endpoint) {
        repository.deleteByUserIdAndEndpoint(currentUserService.getCurrentUserId(), endpoint);
    }
}
