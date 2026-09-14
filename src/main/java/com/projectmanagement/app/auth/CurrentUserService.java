package com.projectmanagement.app.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * Returns the currently authenticated User.
     *
     * Spring Security username is the user's email.
     */
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {

            throw new RuntimeException(
                    "Authenticated user not found");
        }

        Object principal = authentication.getPrincipal();

        if (principal == null) {

            throw new RuntimeException(
                    "Authenticated user principal not found");
        }

        String authenticatedEmail;

        // ------------------------------------------------------------
        // Spring Security UserDetails
        // ------------------------------------------------------------

        if (principal instanceof UserDetails userDetails) {

            authenticatedEmail = userDetails.getUsername();
        }

        // ------------------------------------------------------------
        // String principal
        // ------------------------------------------------------------

        else if (principal instanceof String principalString) {

            authenticatedEmail = principalString;
        }

        // ------------------------------------------------------------
        // Fallback
        // ------------------------------------------------------------

        else {

            authenticatedEmail = authentication.getName();
        }

        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {

            throw new RuntimeException(
                    "Unable to determine authenticated user email");
        }

        // ------------------------------------------------------------
        // Final email variable
        // ------------------------------------------------------------

        final String email = authenticatedEmail
                .trim()
                .toLowerCase();

        // ------------------------------------------------------------
        // Find user from database
        // ------------------------------------------------------------

        return userRepository
                .findByEmail(email)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException(
                        "Authenticated user not found with email: "
                                + email));
    }

    /**
     * Returns ID of currently authenticated user.
     */
    public Long getCurrentUserId() {

        return getCurrentUser().getId();
    }

    /**
     * Returns email of currently authenticated user.
     */
    public String getCurrentUserEmail() {

        return getCurrentUser().getEmail();
    }

    /**
     * Returns name of currently authenticated user.
     */
    public String getCurrentUserName() {

        return getCurrentUser().getName();
    }
}