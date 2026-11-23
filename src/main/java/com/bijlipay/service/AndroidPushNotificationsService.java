package com.bijlipay.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AndroidPushNotificationsService {
    @Value("${fcm.FIREBASE_SERVER_KEY}")
    private String FIREBASE_SERVER_KEY;
    @Value("${fcm.FIREBASE_API_URL}")
    private String FIREBASE_API_URL;
    @Value("${push.fcm.key.file.path}")
    private String FIREBASE_KEY_FILE_PATH;
    private static final Logger logger = LoggerFactory.getLogger(AndroidPushNotificationsService.class);

    @Async("asyncExecutor")
    public CompletableFuture<String> send(String request) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        logger.info("Firebase API Url --{}", this.FIREBASE_API_URL);
        logger.info("Firebase API Request Payload --{}", request);
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList();
        interceptors.add(new HeaderRequestInterceptor("Authorization", "Bearer " + this.getAccessToken()));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        restTemplate.setInterceptors(interceptors);
        String firebaseResponse = null;

        try {
            firebaseResponse = (String)restTemplate.postForObject(this.FIREBASE_API_URL, request, String.class, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(firebaseResponse);
    }

    private String getAccessToken() throws IOException {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new FileInputStream(this.FIREBASE_KEY_FILE_PATH)).createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refresh();
            logger.info("token ------------- : {}", googleCredentials.getAccessToken().getTokenValue());
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initialize() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).setDatabaseUrl("POST https://fcm.googleapis.com/v1/projects/vyapaar-pro/messages:send").build();
        FirebaseApp.initializeApp(options);
    }
}