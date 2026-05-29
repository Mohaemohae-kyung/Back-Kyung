package kyung.kung_backend.global.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    public void sendPaymentNotification(String token, String title, String body, String orderId) {
        if (token == null || token.isBlank()) {
            log.debug("[FCM] skip: empty token");
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("[FCM] FirebaseApp not initialized, skip send");
            return;
        }
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            AndroidNotification androidNotification = AndroidNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setVisibility(AndroidNotification.Visibility.PUBLIC)
                    .setPriority(AndroidNotification.Priority.MAX)
                    .build();

            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(androidNotification)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig)
                    .putData("orderId", orderId == null ? "" : orderId)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] sent: {}", response);
        } catch (Exception e) {
            log.warn("[FCM] send failed: {}", e.getMessage());
        }
    }
}
