package com.chocobi.leafy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {
    @Value("${FIREBASE_SECRET:}")
    private String firebaseServiceAccountJson;


//    @PostConstruct
//    public void init() throws IOException {
//        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
//                firebaseServiceAccountJson.getBytes());
//
//        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount);
//
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(googleCredentials)
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            FirebaseApp.initializeApp(options);
//            log.info("Firebase application has been initialized");
//        }
//    }
}
