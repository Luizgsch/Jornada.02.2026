package br.com.cashmobile.finance;

import br.com.cashmobile.movement.Movement;
import br.com.cashmobile.movement.MovementRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinanceService {
    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 10, 22);
    private static final YearMonth DEMO_MONTH = YearMonth.of(2026, 10);
    public enum Kind { payable, receivable }
    public enum Status { all, open, overdue, paid }
    public record Account(String id, Kind kind, String description, String party, String category,
            LocalDate dueDate, long valueInCents, LocalDate paidDate, String account, Status status) {}
    private static final List<Account> ACCOUNTS = List.of(
            account("energia", Kind.payable, "Energia elétrica", "Fornecedor de energia (exemplo)", "Infraestrutura", "2026-10-18", 68000, null),
            account("aluguel", Kind.payable, "Aluguel do escritório", "Locador (exemplo)", "Infraestrutura", "2026-10-23", 420000, null),
            account("internet", Kind.payable, "Internet e telefonia", "Operadora (exemplo)", "Serviços", "2026-10-26", 36000, null),
            account("software", Kind.payable, "Software de gestão", "Fornecedor de software (exemplo)", "Tecnologia", "2026-10-15", 89000, "2026-10-15"),
            account("receber-notas", Kind.receivable, "Serviços de notas", "Cliente A (exemplo)", "Notas", "2026-10-25", 1800000, null),
            account("receber-registro", Kind.receivable, "Serviços de registro", "Cliente B (exemplo)", "Registro", "2026-10-30", 850000, null));
    private final MovementRepository repository;
    public FinanceService(MovementRepository repository) { this.repository = repository; }

    private static Account account(String id, Kind kind, String description, String party, String category,
            String dueDate, long cents, String paidDate) {
        LocalDate due = LocalDate.parse(dueDate);
        LocalDate paid = paidDate == null ? null : LocalDate.parse(paidDate);
        return new Account(id, kind, description, party, category, due, cents, paid, "Conta principal",
                paid != null ? Status.paid : due.isBefore(REFERENCE_DATE) ? Status.overdue : Status.open);
    }

    public YearMonth month(String value) {
        try {
            var month = YearMonth.parse(value);
            if (month.getYear() < 1900 || month.getYear() > 9999) throw new DateTimeParseException("Ano inválido", value, 0);
            return month;
        } catch (DateTimeParseException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mês deve seguir YYYY-MM, entre 1900-01 e 9999-12.");
        }
    }

    public Summary summary(String owner, String value) {
        var month = month(value);
        var movements = repository.findAll(owner).stream().filter(item -> item.deletedAt() == null).toList();
        long allIn = movements.stream().filter(item -> item.type() == Movement.MovementType.entrada).mapToLong(Movement::valueInCents).sum();
        long allOut = movements.stream().filter(item -> item.type() == Movement.MovementType.saida).mapToLong(Movement::valueInCents).sum();
        var totals = totals(movements, month);
        var payables = ACCOUNTS.stream().filter(item -> item.kind() == Kind.payable && item.status() != Status.paid
                && YearMonth.from(item.dueDate()).equals(month)).toList();
        long receivable = ACCOUNTS.stream().filter(item -> item.kind() == Kind.receivable && item.status() != Status.paid
                && YearMonth.from(item.dueDate()).equals(month)).mapToLong(Account::valueInCents).sum();
        var chart = java.util.stream.IntStream.rangeClosed(0, 5).mapToObj(index -> totals(movements, month.minusMonths(5 - index))).toList();
        return new Summary(month.toString(), true, REFERENCE_DATE, 0, allIn - allOut, totals.incomingCents(), totals.outgoingCents(),
                totals.incomingCents() + totals.outgoingCents(), totals.netCents(), payables.stream().mapToLong(Account::valueInCents).sum(),
                receivable, payables.size(), payables.stream().filter(item -> item.status() == Status.overdue).count(), chart);
    }

    private MonthTotals totals(List<Movement> movements, YearMonth month) {
        long incoming = 0, outgoing = 0;
        for (var item : movements) if (YearMonth.from(item.date()).equals(month)) {
            if (item.type() == Movement.MovementType.entrada) incoming += item.valueInCents(); else outgoing += item.valueInCents();
        }
        return new MonthTotals(month.toString(), incoming, outgoing, incoming - outgoing);
    }

    public AccountPage accounts(String value, Kind kind, Status status, String q, int page, int size) {
        var month = month(value);
        String search = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        var items = ACCOUNTS.stream().filter(item -> item.kind() == kind && YearMonth.from(item.dueDate()).equals(month))
                .filter(item -> status == Status.all || item.status() == status)
                .filter(item -> (item.description() + " " + item.party()).toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparing(Account::dueDate).thenComparing(Account::id)).toList();
        int start = (int) Math.min((long) page * size, items.size());
        return new AccountPage(true, REFERENCE_DATE, items.subList(start, Math.min(start + size, items.size())), items.size(), page, size,
                (items.size() + size - 1) / size, items.stream().mapToLong(Account::valueInCents).sum());
    }

    public Account account(String id) {
        return ACCOUNTS.stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta demonstrativa não encontrada."));
    }

    public RevenueReport revenue(String value) {
        var month = month(value);
        List<RevenueItem> items = month.equals(DEMO_MONTH) ? List.of(new RevenueItem("Notas", "Funcionário A", 1080000),
                new RevenueItem("Notas", "Funcionário B", 720000), new RevenueItem("Registro", "Funcionário C", 850000)) : List.of();
        return new RevenueReport(month.toString(), "catalogo_ficticio", items, items.stream().mapToLong(RevenueItem::valueInCents).sum());
    }

    public BudgetReport budget(String value) {
        var month = month(value);
        List<BudgetItem> items = month.equals(DEMO_MONTH) ? List.of(new BudgetItem("Infraestrutura", 500000, 488000),
                new BudgetItem("Serviços", 30000, 36000), new BudgetItem("Tecnologia", 100000, 89000)) : List.of();
        long planned = items.stream().mapToLong(BudgetItem::plannedCents).sum();
        long actual = items.stream().mapToLong(BudgetItem::actualCents).sum();
        return new BudgetReport(month.toString(), "catalogo_ficticio", items, planned, actual, planned - actual);
    }

    public record MonthTotals(String month, long incomingCents, long outgoingCents, long netCents) {}
    public record Summary(String month, boolean demonstration, LocalDate catalogReferenceDate, long initialBalanceCents,
            long balanceCents, long incomingCents, long outgoingCents, long movedCents, long netCents, long payableOpenCents,
            long receivableOpenCents, long pendingCount, long overdueCount, List<MonthTotals> chart) {}
    public record AccountPage(boolean demonstration, LocalDate referenceDate, List<Account> items, long total, int page,
            int size, int totalPages, long totalCents) {}
    public record RevenueItem(String sector, String employee, long valueInCents) {}
    public record RevenueReport(String month, String source, List<RevenueItem> items, long totalCents) {}
    public record BudgetItem(String category, long plannedCents, long actualCents) {}
    public record BudgetReport(String month, String source, List<BudgetItem> items, long plannedCents, long actualCents, long availableCents) {}
}
