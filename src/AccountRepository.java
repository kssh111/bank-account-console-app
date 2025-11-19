import java.util.List;
import java.util.Optional;

/**
 * Repository паттерні - деректермен жұмысты абстракциялау
 * Бұл interface арқылы кез келген storage пайдалана аламыз:
 * - Файл (қазір)
 * - Database (болашақта)
 * - Memory (тестілеу үшін)
 */
public interface AccountRepository {
    /**
     * Аккаунтты сақтау немесе жаңарту
     * @param account сақталатын аккаунт
     * @return сақталған аккаунт
     */
    AccountWithHistory save(AccountWithHistory account);

    /**
     * ID бойынша аккаунтты табу
     * @param id аккаунт ID
     * @return Optional<AccountWithHistory>
     */
    Optional<AccountWithHistory> findById(int id);

    /**
     * Барлық аккаунттарды қайтару
     * @return аккаунттар тізімі
     */
    List<AccountWithHistory> findAll();

    /**
     * Аккаунтты өшіру
     * @param id аккаунт ID
     * @return өшірілді ме
     */
    boolean deleteById(int id);

    /**
     * Иесінің аты бойынша іздеу
     * @param ownerName иесінің аты
     * @return табылған аккаунттар
     */
    List<AccountWithHistory> findByOwnerName(String ownerName);

    /**
     * Balance шартымен іздеу
     * @param minBalance минималды balance
     * @return табылған аккаунттар
     */
    List<AccountWithHistory> findByBalanceGreaterThan(double minBalance);

    /**
     * Аккаунт бар ма екенін тексеру
     * @param id аккаунт ID
     * @return бар ма
     */
    boolean existsById(int id);

    /**
     * Барлық аккаунттардың санын қайтару
     * @return аккаунттар саны
     */
    int count();

    /**
     * Келесі бос ID-ді қайтару
     * @return келесі ID
     */
    int getNextId();

    /**
     * Барлық деректерді storage-ке жазу (flush)
     */
    void flush();
}
