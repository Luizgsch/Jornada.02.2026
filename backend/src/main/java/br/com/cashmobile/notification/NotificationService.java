package br.com.cashmobile.notification;

import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 10, 22);
    private final NotificationRepository repository;
    private final Clock clock;
    public NotificationService(NotificationRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }
    public NotificationView get(String owner, String id) { return view(repository.get(owner, id).orElseThrow(this::notFound)); }
    public NotificationView read(String owner, String id, boolean read) {
        return view(repository.setRead(owner, id, read, clock.instant()).orElseThrow(this::notFound));
    }
    public UnreadCount count(String owner) {
        return new UnreadCount(repository.list(owner).stream().filter(item -> !item.read()).count(), true, REFERENCE_DATE);
    }
    public ReadAllResult readAll(String owner) { return new ReadAllResult(repository.readAll(owner, clock.instant()), 0); }
    public NotificationPage list(String owner, DemoNotification.Category category, Boolean read, String q, int page, int size) {
        var snapshot = repository.list(owner);
        String search = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        var matches = snapshot.stream().filter(item -> category == null || item.category() == category)
                .filter(item -> read == null || item.read() == read)
                .filter(item -> (item.title() + " " + item.description()).toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparing(DemoNotification::receivedAt).reversed().thenComparing(DemoNotification::id))
                .map(this::view).toList();
        int start = (int) Math.min((long) page * size, matches.size());
        return new NotificationPage(true, REFERENCE_DATE, matches.subList(start, Math.min(start + size, matches.size())),
                matches.size(), page, size, (matches.size() + size - 1) / size,
                snapshot.stream().filter(item -> !item.read()).count());
    }
    private NotificationView view(DemoNotification item) {
        return new NotificationView(item.id(), item.category(), item.title(), item.description(), item.receivedAt(), item.read(), item.readAt(),
                item.accountId(), item.accountId() == null ? null : "/api/v1/finance/accounts/" + item.accountId(), true, REFERENCE_DATE);
    }
    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada."); }
    public record NotificationView(String id, DemoNotification.Category category, String title, String description, Instant receivedAt,
            boolean read, Instant readAt, String accountId, String accountPath, boolean demonstration, LocalDate referenceDate) {}
    public record NotificationPage(boolean demonstration, LocalDate referenceDate, List<NotificationView> items, long total,
            int page, int size, int totalPages, long unreadCount) {}
    public record UnreadCount(long unreadCount, boolean demonstration, LocalDate referenceDate) {}
    public record ReadAllResult(int updatedCount, long unreadCount) {}
}
