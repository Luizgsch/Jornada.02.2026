package br.com.cashmobile.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    List<DemoNotification> list(String owner);
    Optional<DemoNotification> get(String owner, String id);
    Optional<DemoNotification> setRead(String owner, String id, boolean read, Instant now);
    int readAll(String owner, Instant now);
}
