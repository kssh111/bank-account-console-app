/**
 * Аккаунт табылмағанда лақтырылады
 */
public class AccountNotFoundException extends Exception {
    private int accountId;

    public AccountNotFoundException(int accountId) {
        super("Account not found with ID: " + accountId);
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
