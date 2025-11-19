/**
 * PIN код қате енгізілгенде лақтырылады
 */
public class InvalidPinException extends Exception {
    private int accountId;
    private int attemptsRemaining;

    public InvalidPinException(int accountId) {
        this(accountId, -1);
    }

    public InvalidPinException(int accountId, int attemptsRemaining) {
        super("Invalid PIN code for account ID: " + accountId);
        this.accountId = accountId;
        this.attemptsRemaining = attemptsRemaining;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public boolean hasAttemptsInfo() {
        return attemptsRemaining >= 0;
    }
}
