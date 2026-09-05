package com.example.routemaker.global.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
public class FirebaseTokenVerifier {

    private static final String APP_NAME = "routemaker-backend";

    private final String projectId;
    private final String serviceAccountPath;
    private final String serviceAccountJsonBase64;
    private volatile FirebaseAuth firebaseAuth;

    public FirebaseTokenVerifier(
            @Value("${firebase.project-id:chungnam-routemaker}") String projectId,
            @Value("${firebase.service-account-path:}") String serviceAccountPath,
            @Value("${firebase.service-account-json-base64:}") String serviceAccountJsonBase64
    ) {
        this.projectId = projectId;
        this.serviceAccountPath = serviceAccountPath;
        this.serviceAccountJsonBase64 = serviceAccountJsonBase64;
    }

    public FirebaseToken verify(String idToken) throws FirebaseAuthException, IOException {
        return getFirebaseAuth().verifyIdToken(idToken);
    }

    private FirebaseAuth getFirebaseAuth() throws IOException {
        FirebaseAuth current = firebaseAuth;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (firebaseAuth == null) {
                FirebaseApp app = FirebaseApp.getApps().stream()
                        .filter(candidate -> APP_NAME.equals(candidate.getName()))
                        .findFirst()
                        .orElseGet(this::initializeApp);
                firebaseAuth = FirebaseAuth.getInstance(app);
            }
            return firebaseAuth;
        }
    }

    private FirebaseApp initializeApp() {
        try {
            GoogleCredentials credentials;
            if (StringUtils.hasText(serviceAccountJsonBase64)) {
                byte[] serviceAccountJson = Base64.getDecoder().decode(serviceAccountJsonBase64.trim());
                try (InputStream input = new ByteArrayInputStream(serviceAccountJson)) {
                    credentials = GoogleCredentials.fromStream(input);
                }
            } else if (StringUtils.hasText(serviceAccountPath)) {
                try (InputStream input = Files.newInputStream(Path.of(serviceAccountPath))) {
                    credentials = GoogleCredentials.fromStream(input);
                }
            } else {
                credentials = GoogleCredentials.getApplicationDefault();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();
            return FirebaseApp.initializeApp(options, APP_NAME);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Firebase Admin 인증 정보를 읽을 수 없습니다. "
                            + "FIREBASE_SERVICE_ACCOUNT_JSON_BASE64 또는 FIREBASE_SERVICE_ACCOUNT_PATH를 확인하세요.",
                    exception
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "FIREBASE_SERVICE_ACCOUNT_JSON_BASE64가 올바른 Base64 형식이 아닙니다.",
                    exception
            );
        }
    }
}
