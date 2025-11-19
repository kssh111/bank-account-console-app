public class InsufficientFundsException  extends Exception {
    private double balance;
    private double requestedAmount;

    public InsufficientFundsException(double balance, double requestedAmount) {
        super(String.format("Insufficient funds. Balance: %.2f, Requested: %.2f",
                balance, requestedAmount));
        this.balance = balance;
        this.requestedAmount = requestedAmount;
    }

    public double getBalance() {
        return balance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getShortage() {
        return requestedAmount - balance;
    }
}
