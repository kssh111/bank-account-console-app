import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Жетілдірілген AccountWithHistory класы
 * - Thread-safe операциялар
 * - Дұрыс transfer логикасы
 * - Transaction тарихы
 * - Read-only transaction list
 */

public class AccountWithHistory extends Account implements Serializable {
    private static final long serialVersionUID = 2L;

    // Thread-safe болу үшін synchronized қолданамыз
    private final List<Transaction> transactions;
    private static final int MAX_HISTORY_SIZE = 1000; // Шектеу

    /**
     * Конструктор
     */
    public AccountWithHistory(int id, String ownerName, String pinCode) {
        super(id, ownerName, pinCode);
        this.transactions = new ArrayList<>();
        // Аккаунт ашылғанын жазамыз
        addTransaction(new Transaction(TransactionType.ACCOUNT_CREATED, 0.0, id, id));
    }

    /**
     * Ақша салу (override)
     */
    @Override
    public synchronized void deposit(double amount) throws InvalidAmountException {
        super.deposit(amount);
        addTransaction(new Transaction(TransactionType.DEPOSIT, amount, getId(), getId()));
    }

    /**
     * Ақша алу (override)
     */
    @Override
    public synchronized void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        super.withdraw(amount);
        addTransaction(new Transaction(TransactionType.WITHDRAW, amount, getId(), getId()));
    }

    /**
     * Басқа аккаунтқа ақша аудару
     * КРИТИКАЛЫҚ: Екі аккаунтты де lock қылу керек (deadlock болмау үшін ID бойынша сұрыптау)
     */
    public void transferTo(AccountWithHistory toAccount, double amount, String pin)
            throws InvalidAmountException, InsufficientFundsException,
            InvalidPinException, IllegalArgumentException {

        if (toAccount == null) {
            throw new IllegalArgumentException("Destination account cannot be null");
        }

        if (this.getId() == toAccount.getId()) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // PIN тексеру
        this.checkPin(pin);

        // Deadlock болмау үшін: әрқашан кіші ID-ден бастап lock қылу
        AccountWithHistory first = this.getId() < toAccount.getId() ? this : toAccount;
        AccountWithHistory second = this.getId() < toAccount.getId() ? toAccount : this;

        synchronized (first) {
            synchronized (second) {
                // Екі аккаунт та lock қылынған, енді қауіпсіз операция жасаймыз
                super.withdraw(amount); // Өзімізден алу
                toAccount.depositInternal(amount); // Басқаға салу

                // Transaction тарихын жазу
                Transaction fromTx = new Transaction(
                        TransactionType.TRANSFER_OUT,
                        amount,
                        this.getId(),
                        toAccount.getId()
                );

                Transaction toTx = new Transaction(
                        TransactionType.TRANSFER_IN,
                        amount,
                        this.getId(),
                        toAccount.getId()
                );

                this.addTransaction(fromTx);
                toAccount.addTransaction(toTx);
            }
        }
    }

    /**
     * Internal deposit - transfer үшін (PIN тексерусіз)
     */
    private synchronized void depositInternal(double amount) throws InvalidAmountException {
        super.deposit(amount);
    }

    /**
     * PIN өзгерту (override)
     */
    @Override
    public synchronized void changePin(String oldPin, String newPin) throws InvalidPinException {
        super.changePin(oldPin, newPin);
        addTransaction(new Transaction(TransactionType.PIN_CHANGED, 0.0, getId(), getId()));
    }

    /**
     * Transaction қосу (synchronized)
     */
    private synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);

        // Егер тарих тым үлкен болса, ескілерін өшіру
        if (transactions.size() > MAX_HISTORY_SIZE) {
            transactions.remove(0);
        }
    }

    /**
     * Барлық транзакцияларды қайтару (unmodifiable)
     */
    public List<Transaction> getTransactions() {
        synchronized (transactions) {
            return Collections.unmodifiableList(new ArrayList<>(transactions));
        }
    }

    /**
     * Соңғы N транзакцияны қайтару
     */
    public List<Transaction> getLastTransactions(int count) {
        synchronized (transactions) {
            int size = transactions.size();
            int start = Math.max(0, size - count);
            return Collections.unmodifiableList(
                    new ArrayList<>(transactions.subList(start, size))
            );
        }
    }

    /**
     * Transaction санын қайтару
     */
    public int getTransactionCount() {
        synchronized (transactions) {
            return transactions.size();
        }
    }

    /**
     * Белгілі бір типтегі транзакцияларды іздеу
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        synchronized (transactions) {
            List<Transaction> filtered = new ArrayList<>();
            for (Transaction tx : transactions) {
                if (tx.getType() == type) {
                    filtered.add(tx);
                }
            }
            return Collections.unmodifiableList(filtered);
        }
    }

    /**
     * Жалпы депозит сомасын есептеу
     */
    public double getTotalDeposited() {
        synchronized (transactions) {
            return transactions.stream()
                    .filter(tx -> tx.getType() == TransactionType.DEPOSIT ||
                            tx.getType() == TransactionType.TRANSFER_IN)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }
    }

    /**
     * Жалпы алынған сомасын есептеу
     */
    public double getTotalWithdrawn() {
        synchronized (transactions) {
            return transactions.stream()
                    .filter(tx -> tx.getType() == TransactionType.WITHDRAW ||
                            tx.getType() == TransactionType.TRANSFER_OUT)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
        }
    }

    @Override
    public String toString() {
        return String.format("%s, Transactions=%d",
                super.toString(),
                getTransactionCount());
    }

}
