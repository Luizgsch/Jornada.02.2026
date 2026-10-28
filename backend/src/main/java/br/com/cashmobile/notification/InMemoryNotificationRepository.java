package br.com.cashmobile.notification;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {
    private static final List<DemoNotification> CATALOG = List.of(
            new DemoNotification("aluguel", DemoNotification.Category.financeira, "Uma conta vence amanhã",
                    "Exemplo com referência em 22/10/2026: aluguel de R$ 4.200,00 vence em 23/10/2026.",
                    Instant.parse("2026-10-22T09:42:00Z"), "aluguel", null),
            new DemoNotification("pagamento", DemoNotification.Category.financeira, "Pagamento realizado",
                    "Exemplo: software de gestão de R$ 890,00 pago em 15/10/2026. Nenhum pagamento é executado por este alerta.",
                    Instant.parse("2026-10-22T08:00:00Z"), "software", null),
            new DemoNotification("movimentacao", DemoNotification.Category.sistema, "Nova movimentação registrada",
                    "Alerta fictício de entrada de R$ 2.450,00. Não representa um registro cadastrado nesta API.",
                    Instant.parse("2026-10-21T10:00:00Z"), null, Instant.parse("2026-10-22T07:00:00Z")),
            new DemoNotification("documento", DemoNotification.Category.sistema, "Documento anexado",
                    "Alerta fictício de comprovante. Nenhum arquivo é criado por este exemplo.",
                    Instant.parse("2026-10-20T09:00:00Z"), null, Instant.parse("2026-10-22T07:00:00Z")));
    private final Map<String, Map<String, DemoNotification>> byOwner = new HashMap<>();

    private Map<String, DemoNotification> state(String owner) {
        return byOwner.computeIfAbsent(owner, key -> {
            var map = new LinkedHashMap<String, DemoNotification>();
            CATALOG.forEach(item -> map.put(item.id(), item));
            return map;
        });
    }

    @Override public synchronized List<DemoNotification> list(String owner) { return List.copyOf(state(owner).values()); }
    @Override public synchronized Optional<DemoNotification> get(String owner, String id) { return Optional.ofNullable(state(owner).get(id)); }
    @Override public synchronized Optional<DemoNotification> setRead(String owner, String id, boolean read, Instant now) {
        var item = state(owner).get(id);
        if (item == null) return Optional.empty();
        var changed = item.reading(read, now);
        state(owner).put(id, changed);
        return Optional.of(changed);
    }
    @Override public synchronized int readAll(String owner, Instant now) {
        var map = state(owner);
        int count = (int) map.values().stream().filter(item -> !item.read()).count();
        map.replaceAll((id, item) -> item.reading(true, now));
        return count;
    }
}
