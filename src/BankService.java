import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Жетілдірілген BankService
 * - Repository паттернін қолдану
 * - Дұрыс exception handling
 * - Business logic бөлу
 * - Logging қосу
 * - Thread-safe операциялар
 */


public class BankService {
    private final AccountRepository repository;
    private final TransactionLogger logger;

    /**
     * Конструктор - Repository injection
     */
    public BankService(AccountRepository repository) {
        this.repository = repository;
        this.logger = new TransactionLogger();
    }

    /**
     * Аккаунт жасау
     */
    public AccountWithHistory createAccount(String ownerName, String pinCode)
            throws IllegalArgumentException {

        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }

        int newId = repository.getNextId();
        AccountWithHistory account = new AccountWithHistory(newId, ownerName, pinCode);

        repository.save(account);
        repository.flush();

        logger.log("Account created: ID=" + newId + ", Owner=" + ownerName);
        return account;
    }

    /**
     * Аккаунтты табу
     */
    public AccountWithHistory getAccount(int id) throws AccountNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    /**
     * Ақша салу
     */
    public void deposit(int accountId, double amount, String pin)
            throws AccountNotFoundException, InvalidPinException,
            InvalidAmountException {

        AccountWithHistory account = getAccount(accountId);
        account.checkPin(pin);
        account.deposit(amount);

        repository.save(account);
        repository.flush();

        logger.log(String.format("Deposit: Account=%d, Amount=%.2f", accountId, amount));
    }

    /**
     * Ақша алу
     */
    public void withdraw(int accountId, double amount, String pin)
            throws AccountNotFoundException, InvalidPinException,
            InvalidAmountException, InsufficientFundsException {

        AccountWithHistory account = getAccount(accountId);
        account.checkPin(pin);
        account.withdraw(amount);

        repository.save(account);
        repository.flush();

        logger.log(String.format("Withdraw: Account=%d, Amount=%.2f", accountId, amount));
    }

    /**
     * Ақша аудару
     */
    public void transfer(int fromId, int toId, double amount, String pin)
            throws AccountNotFoundException, InvalidPinException,
            InvalidAmountException, InsufficientFundsException {

        if (fromId == toId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        AccountWithHistory fromAccount = getAccount(fromId);
        AccountWithHistory toAccount = getAccount(toId);

        // transferTo методы екі аккаунтты да lock қылып, atomically жасайды
        fromAccount.transferTo(toAccount, amount, pin);

        repository.save(fromAccount);
        repository.save(toAccount);
        repository.flush();

        logger.log(String.format("Transfer: From=%d, To=%d, Amount=%.2f",
                fromId, toId, amount));
    }

    /**
     * PIN өзгерту
     */
    public void changePin(int accountId, String oldPin, String newPin)
            throws AccountNotFoundException, InvalidPinException {

        AccountWithHistory account = getAccount(accountId);
        account.changePin(oldPin, newPin);

        repository.save(account);
        repository.flush();

        logger.log("PIN changed: Account=" + accountId);
    }

    /**
     * Аккаунтты unlock жасау (админ функциясы)
     */
    public void unlockAccount(int accountId) throws AccountNotFoundException {
        AccountWithHistory account = getAccount(accountId);
        account.unlock();

        repository.save(account);
        repository.flush();

        logger.log("Account unlocked: " + accountId);
    }

    /**
     * Барлық аккаунттарды қайтару
     */
    public List<AccountWithHistory> getAllAccounts() {
        return repository.findAll();
    }

    /**
     * Иесінің аты бойынша іздеу
     */
    public List<AccountWithHistory> searchByOwner(String ownerName) {
        return repository.findByOwnerName(ownerName);
    }

    /**
     * Минималды балансы бар аккаунттарды табу
     */
    public List<AccountWithHistory> getAccountsWithMinBalance(double minBalance) {
        return repository.findByBalanceGreaterThan(minBalance);
    }

    /**
     * Аккаунт статистикасы
     */
    public AccountStatistics getAccountStatistics(int accountId)
            throws AccountNotFoundException {

        AccountWithHistory account = getAccount(accountId);
        return new AccountStatistics(account);
    }

    /**
     * Жалпы статистика
     */
    public BankStatistics getBankStatistics() {
        List<AccountWithHistory> accounts = repository.findAll();

        double totalBalance = accounts.stream()
                .mapToDouble(AccountWithHistory::getBalance)
                .sum();

        long activeAccounts = accounts.stream()
                .filter(acc -> !acc.isLocked())
                .count();

        long lockedAccounts = accounts.stream()
                .filter(AccountWithHistory::isLocked)
                .count();

        return new BankStatistics(
                accounts.size(),
                (int) activeAccounts,
                (int) lockedAccounts,
                totalBalance
        );
    }

    /**
     * Аккаунтты өшіру
     */
    public boolean deleteAccount(int accountId, String pin)
            throws AccountNotFoundException, InvalidPinException {

        AccountWithHistory account = getAccount(accountId);
        account.checkPin(pin);

        if (account.getBalance() > 0) {
            throw new IllegalStateException(
                    "Cannot delete account with positive balance. Please withdraw all funds first.");
        }

        boolean deleted = repository.deleteById(accountId);
        if (deleted) {
            repository.flush();
            logger.log("Account deleted: " + accountId);
        }

        return deleted;
    }

    /**
     * Жабық аккаунттарды тазалау
     */
    public int cleanupLockedAccounts() {
        List<AccountWithHistory> lockedAccounts = repository.findAll().stream()
                .filter(AccountWithHistory::isLocked)
                .filter(acc -> acc.getBalance() == 0)
                .collect(Collectors.toList());

        int count = 0;
        for (AccountWithHistory account : lockedAccounts) {
            if (repository.deleteById(account.getId())) {
                count++;
            }
        }

        if (count > 0) {
            repository.flush();
            logger.log("Cleaned up " + count + " locked accounts");
        }

        return count;
    }

    /**
     * Логтарды экспорттау
     */
    public List<String> getRecentLogs(int count) {
        return logger.getRecentLogs(count);
    }

    /**
     * Repository санын қайтару
     */
    public int getAccountCount() {
        return repository.count();
    }
}

/**
 * Аккаунт статистикасы
 */
class AccountStatistics {
    private final int accountId;
    private final String ownerName;
    private final double currentBalance;
    private final double totalDeposited;
    private final double totalWithdrawn;
    private final int transactionCount;
    private final boolean isLocked;

    public AccountStatistics(AccountWithHistory account) {
        this.accountId = account.getId();
        this.ownerName = account.getOwnerName();
        this.currentBalance = account.getBalance();
        this.totalDeposited = account.getTotalDeposited();
        this.totalWithdrawn = account.getTotalWithdrawn();
        this.transactionCount = account.getTransactionCount();
        this.isLocked = account.isLocked();
    }

    @Override
    public String toString() {
        return String.format(
                "Account #%d (%s)\n" +
                        "  Current Balance: %.2f KZT\n" +
                        "  Total Deposited: %.2f KZT\n" +
                        "  Total Withdrawn: %.2f KZT\n" +
                        "  Transactions: %d\n" +
                        "  Status: %s",
                accountId, ownerName, currentBalance, totalDeposited,
                totalWithdrawn, transactionCount, isLocked ? "LOCKED" : "ACTIVE"
        );
    }

    // Getters
    public int getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public double getCurrentBalance() { return currentBalance; }
    public double getTotalDeposited() { return totalDeposited; }
    public double getTotalWithdrawn() { return totalWithdrawn; }
    public int getTransactionCount() { return transactionCount; }
    public boolean isLocked() { return isLocked; }
}

/**
 * Банк статистикасы
 */
class BankStatistics {
    private final int totalAccounts;
    private final int activeAccounts;
    private final int lockedAccounts;
    private final double totalBalance;

    public BankStatistics(int totalAccounts, int activeAccounts,
                          int lockedAccounts, double totalBalance) {
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activeAccounts;
        this.lockedAccounts = lockedAccounts;
        this.totalBalance = totalBalance;
    }

    @Override
    public String toString() {
        return String.format(
                "Bank Statistics:\n" +
                        "  Total Accounts: %d\n" +
                        "  Active Accounts: %d\n" +
                        "  Locked Accounts: %d\n" +
                        "  Total Balance: %.2f KZT",
                totalAccounts, activeAccounts, lockedAccounts, totalBalance
        );
    }

    // Getters
    public int getTotalAccounts() { return totalAccounts; }
    public int getActiveAccounts() { return activeAccounts; }
    public int getLockedAccounts() { return lockedAccounts; }
    public double getTotalBalance() { return totalBalance; }
}
