package com.projectmanagement.app.notification.fcm;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
@ConditionalOnProperty(name = "app.notification.fcm.enabled", havingValue = "true")
public class FcmFirebaseConfig {

    @Bean
    FirebaseApp firebaseApp(@Value("${app.notification.fcm.service-account-file:}") String serviceAccountFile)
            throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;
            if (serviceAccountFile != null && !serviceAccountFile.isBlank()) {
                try (InputStream input = new java.io.FileInputStream(serviceAccountFile)) {
                    credentials = GoogleCredentials.fromStream(input);
                }
            } else {
                credentials = GoogleCredentials.getApplicationDefault();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
