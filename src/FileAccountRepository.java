
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * Файлмен жұмыс істейтін Repository
 * - Thread-safe операциялар
 * - Auto-backup
 * - Дұрыс error handling
 * - Atomic file operations
 */
public class FileAccountRepository implements AccountRepository {
    private final Map<Integer, AccountWithHistory> accountsCache;
    private final String dataDirectory;
    private final String fileName;
    private final String backupDirectory;
    private int nextId;

    private static final String DEFAULT_DIR = "data";
    private static final String DEFAULT_FILE = "accounts.dat";
    private static final String BACKUP_DIR = "data/backups";

    /**
     * Конструктор - default файл
     */
    public FileAccountRepository() {
        this(DEFAULT_DIR, DEFAULT_FILE);
    }

    /**
     * Конструктор - custom файл
     */
    public FileAccountRepository(String dataDirectory, String fileName) {
        this.dataDirectory = dataDirectory;
        this.fileName = fileName;
        this.backupDirectory = BACKUP_DIR;
        this.accountsCache = new ConcurrentHashMap<>();
        this.nextId = 1;

        initializeDirectories();
        loadFromFile();
    }

    /**
     * Папкаларды жасау
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(dataDirectory));
            Files.createDirectories(Paths.get(backupDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories", e);
        }
    }

    @Override
    public synchronized AccountWithHistory save(AccountWithHistory account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        accountsCache.put(account.getId(), account);

        // nextId жаңарту
        if (account.getId() >= nextId) {
            nextId = account.getId() + 1;
        }

        return account;
    }

    @Override
    public Optional<AccountWithHistory> findById(int id) {
        return Optional.ofNullable(accountsCache.get(id));
    }

    @Override
    public List<AccountWithHistory> findAll() {
        return new ArrayList<>(accountsCache.values());
    }

    @Override
    public synchronized boolean deleteById(int id) {
        return accountsCache.remove(id) != null;
    }

    @Override
    public List<AccountWithHistory> findByOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchName = ownerName.trim().toLowerCase();
        return accountsCache.values().stream()
                .filter(acc -> acc.getOwnerName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountWithHistory> findByBalanceGreaterThan(double minBalance) {
        return accountsCache.values().stream()
                .filter(acc -> acc.getBalance() > minBalance)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(int id) {
        return accountsCache.containsKey(id);
    }

    @Override
    public int count() {
        return accountsCache.size();
    }

    @Override
    public synchronized int getNextId() {
        return nextId++;
    }

    @Override
    public synchronized void flush() {
        saveToFile();
    }

    /**
     * Файлға сақтау (atomic operation)
     */
    private void saveToFile() {
        String fullPath = dataDirectory + File.separator + fileName;
        String tempPath = fullPath + ".tmp";

        try {
            // Backup жасау (егер файл бар болса)
            File existingFile = new File(fullPath);
            if (existingFile.exists()) {
                createBackup(existingFile);
            }

            // Временный файлға жазу
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(tempPath))) {
                oos.writeObject(new ArrayList<>(accountsCache.values()));
                oos.writeInt(nextId);
            }

            // Atomic rename (Windows-та rename алдында ескі файлды өшіру керек)
            File tempFile = new File(tempPath);
            if (existingFile.exists()) {
                existingFile.delete();
            }

            if (!tempFile.renameTo(existingFile)) {
                throw new IOException("Failed to rename temp file");
            }

        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Файлдан оқу
     */
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        String fullPath = dataDirectory + File.separator + fileName;
        File file = new File(fullPath);

        if (!file.exists()) {
            System.out.println("No existing data file found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fullPath))) {

            List<AccountWithHistory> loadedAccounts =
                    (List<AccountWithHistory>) ois.readObject();

            // nextId оқу (егер бар болса)
            try {
                this.nextId = ois.readInt();
            } catch (EOFException e) {
                // Ескі файлда nextId жоқ болуы мүмкін
                this.nextId = loadedAccounts.isEmpty() ? 1 :
                        loadedAccounts.stream()
                                .mapToInt(AccountWithHistory::getId)
                                .max()
                                .orElse(0) + 1;
            }

            // Cache-ке жүктеу
            accountsCache.clear();
            for (AccountWithHistory account : loadedAccounts) {
                accountsCache.put(account.getId(), account);
            }

            System.out.println("Loaded " + accountsCache.size() + " accounts from file.");

        } catch (FileNotFoundException e) {
            System.out.println("Data file not found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading accounts: " + e.getMessage());

            // Backup-тан қалпына келтіру әрекеті
            if (tryRestoreFromBackup()) {
                System.out.println("Successfully restored from backup.");
            } else {
                System.err.println("Could not restore from backup. Starting fresh.");
            }
        }
    }

    /**
     * Backup жасау
     */
    private void createBackup(File sourceFile) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String backupFileName = "accounts_backup_" + timestamp + ".dat";
            Path backupPath = Paths.get(backupDirectory, backupFileName);

            Files.copy(sourceFile.toPath(), backupPath,
                    StandardCopyOption.REPLACE_EXISTING);

            // Ескі backup-тарды тазалау (тек соңғы 5-еуін қалдыру)
            cleanOldBackups();

        } catch (IOException e) {
            System.err.println("Warning: Failed to create backup: " + e.getMessage());
        }
    }

    /**
     * Ескі backup-тарды өшіру
     */
    private void cleanOldBackups() {
        try {
            File backupDir = new File(backupDirectory);
            File[] backups = backupDir.listFiles((dir, name) ->
                    name.startsWith("accounts_backup_") && name.endsWith(".dat"));

            if (backups != null && backups.length > 5) {
                Arrays.sort(backups, Comparator.comparingLong(File::lastModified));

                // Ескілерін өшіру (тек 5-еуін қалдыру)
                for (int i = 0; i < backups.length - 5; i++) {
                    backups[i].delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to clean old backups: " + e.getMessage());
        }
    }

    /**
     * Backup-тан қалпына келтіру
     */
    private boolean tryRestoreFromBackup() {
        try {
            File backupDir = new File(backupDirectory);
            File[] backups = backupDir.listFiles((dir, name) ->
                    name.startsWith("accounts_backup_") && name.endsWith(".dat"));

            if (backups == null || backups.length == 0) {
                return false;
            }

            // Ең соңғы backup-ты табу
            Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());
            File latestBackup = backups[0];

            // Backup-ты негізгі файлға көшіру
            String fullPath = dataDirectory + File.separator + fileName;
            Files.copy(latestBackup.toPath(), Paths.get(fullPath),
                    StandardCopyOption.REPLACE_EXISTING);

            // Қайта жүктеу
            loadFromFile();
            return true;

        } catch (Exception e) {
            System.err.println("Failed to restore from backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cache-ті тазалау (тестілеу үшін)
     */
    public synchronized void clearCache() {
        accountsCache.clear();
        nextId = 1;
    }

    /**
     * Статистика
     */
    @Override
    public String toString() {
        return String.format(
                "FileAccountRepository[accounts=%d, nextId=%d, file=%s/%s]",
                count(), nextId, dataDirectory, fileName
        );
    }
}
