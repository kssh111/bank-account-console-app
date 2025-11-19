/**
 * Transaction түрлері
 * Enum пайдалану - type safety үшін
 */
public enum TransactionType {
    ACCOUNT_CREATED("Account Created"),
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    PIN_CHANGED("PIN Changed"),
    ACCOUNT_LOCKED("Account Locked"),
    ACCOUNT_UNLOCKED("Account Unlocked");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
