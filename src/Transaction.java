import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Жетілдірілген Transaction класы
 * - Enum пайдалану
 * - Immutable (өзгермейтін)
 * - Жақсы formatting
 */

public class Transaction implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final int fromAccountId;
    private final int toAccountId;
    private final String description;

    /**
     * Негізгі конструктор
     */
    public Transaction(TransactionType type, double amount, int fromAccountId, int toAccountId) {
        this(type, amount, fromAccountId, toAccountId, null);
    }

    /**
     * Description-пен конструктор
     */
    public Transaction(TransactionType type, double amount, int fromAccountId,
                       int toAccountId, String description) {
        this.type = type;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    // ============ GETTERS ============

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public String getDescription() {
        return description;
    }

    // ============ UTILITY METHODS ============

    /**
     * Transfer транзакция екенін тексеру
     */
    public boolean isTransfer() {
        return type == TransactionType.TRANSFER_IN ||
                type == TransactionType.TRANSFER_OUT;
    }

    /**
     * Қысқаша ақпарат
     */
    public String toShortString() {
        return String.format("%s: %.2f", type.getDisplayName(), amount);
    }

    /**
     * Толық ақпарат
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp.format(FORMATTER)).append("] ");
        sb.append(type.getDisplayName());

        if (amount > 0) {
            sb.append(String.format(" %.2f KZT", amount));
        }

        // Transfer болса, аккаунт ID-лерін көрсету
        if (isTransfer()) {
            if (type == TransactionType.TRANSFER_OUT) {
                sb.append(" → Account #").append(toAccountId);
            } else {
                sb.append(" ← Account #").append(fromAccountId);
            }
        }

        if (description != null && !description.isEmpty()) {
            sb.append(" (").append(description).append(")");
        }

        return sb.toString();
    }

    /**
     * Түсті консоль шығысы
     */
    public String toColoredString() {
        String color;
        switch (type) {
            case DEPOSIT:
            case TRANSFER_IN:
                color = Colors.GREEN;
                break;
            case WITHDRAW:
            case TRANSFER_OUT:
                color = Colors.RED;
                break;
            case PIN_CHANGED:
            case ACCOUNT_LOCKED:
                color = Colors.YELLOW;
                break;
            default:
                color = Colors.CYAN;
        }

        return color + toString() + Colors.RESET;
    }

    /**
     * CSV форматында
     */
    public String toCsv() {
        return String.format("%s,%s,%.2f,%d,%d,%s",
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                type.name(),
                amount,
                fromAccountId,
                toAccountId,
                description != null ? description.replace(",", ";") : ""
        );
    }
}
