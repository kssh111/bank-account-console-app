import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Memory-дағы Repository (тестілеу және демо үшін)
 * Thread-safe, жылдам, файлсыз
 */
public class InMemoryAccountRepository implements AccountRepository{
    private final Map<Integer, AccountWithHistory> accounts;
    private int nextId;

    public InMemoryAccountRepository() {
        this.accounts = new ConcurrentHashMap<>();
        this.nextId = 1;
    }

    @Override
    public AccountWithHistory save(AccountWithHistory account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<AccountWithHistory> findById(int id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<AccountWithHistory> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public boolean deleteById(int id) {
        return accounts.remove(id) != null;
    }

    @Override
    public List<AccountWithHistory> findByOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchName = ownerName.trim().toLowerCase();

        return accounts.values().stream()
                .filter(acc -> acc.getOwnerName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountWithHistory> findByBalanceGreaterThan(double minBalance) {
        return accounts.values().stream()
                .filter(acc -> acc.getBalance() > minBalance)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public int count() {
        return accounts.size();
    }

    @Override
    public synchronized int getNextId() {
        return nextId++;
    }

    @Override
    public void flush() {
        // Memory-да flush керек емес
    }

    /**
     * Барлық деректерді тазалау
     */
    public void clear() {
        accounts.clear();
        nextId = 1;
    }

    @Override
    public String toString() {
        return String.format("InMemoryAccountRepository[accounts=%d, nextId=%d]",
                count(), nextId);
    }
}
