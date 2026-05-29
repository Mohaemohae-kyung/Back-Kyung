package kyung.kung_backend.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${fcm.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = openCredentialsStream()) {
                if (serviceAccount == null) {
                    log.warn("[FCM] credentials not configured, push notifications disabled");
                    return;
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[FCM] FirebaseApp initialized");
            } catch (IOException e) {
                log.warn("[FCM] failed to initialize FirebaseApp: {}", e.getMessage());
            }
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            return null;
        }
        if (credentialsPath.startsWith("classpath:")) {
            String path = credentialsPath.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        }
        return new java.io.FileInputStream(credentialsPath);
    }
}
