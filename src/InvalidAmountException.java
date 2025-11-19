/**
 * Қате сома енгізілгенде лақтырылады (теріс немесе 0)
 */
public class InvalidAmountException extends Exception {
    private double amount;

    public InvalidAmountException(double amount) {
        super("Invalid amount: " + amount + ". Amount must be positive.");
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}

