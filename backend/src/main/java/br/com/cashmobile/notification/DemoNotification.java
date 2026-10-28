package br.com.cashmobile.notification;

import java.time.Instant;

public record DemoNotification(String id, Category category, String title, String description,
        Instant receivedAt, String accountId, Instant readAt) {
    public enum Category { financeira, sistema }
    public boolean read() { return readAt != null; }
    public DemoNotification reading(boolean read, Instant now) {
        return new DemoNotification(id, category, title, description, receivedAt, accountId,
                read ? readAt == null ? now : readAt : null);
    }
}
