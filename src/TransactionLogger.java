
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * Transaction логтарын жазу
 * - Thread-safe
 * - Файлға автоматты жазу
 * - Memory-да соңғы логтарды сақтау
 */
public class TransactionLogger {
    private static final String LOG_DIR = "data/logs";
    private static final String LOG_FILE = "transactions.log";
    private static final int MAX_MEMORY_LOGS = 100;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Queue<String> recentLogs;
    private final String logFilePath;
    private PrintWriter writer;

    public TransactionLogger() {
        this.recentLogs = new ConcurrentLinkedQueue<>();
        this.logFilePath = LOG_DIR + File.separator + LOG_FILE;

        initializeLogFile();
    }

    /**
     * Лог файлын инициализациялау
     */
    private void initializeLogFile() {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));

            // Append режимінде ашу
            File logFile = new File(logFilePath);
            this.writer = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(logFile, true)
                    )
            );

            if (logFile.length() == 0) {
                writer.println("=== Transaction Log Started: " +
                        LocalDateTime.now().format(FORMATTER) + " ===");
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Warning: Could not initialize log file: " + e.getMessage());
        }
    }

    /**
     * Лог жазу
     */
    public synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] %s", timestamp, message);

        // Memory-да сақтау
        recentLogs.offer(logEntry);
        if (recentLogs.size() > MAX_MEMORY_LOGS) {
            recentLogs.poll();
        }

        // Файлға жазу
        if (writer != null) {
            writer.println(logEntry);
            writer.flush();
        }
    }

    /**
     * Соңғы логтарды қайтару
     */
    public List<String> getRecentLogs(int count) {
        List<String> logs = new ArrayList<>(recentLogs);
        int size = logs.size();
        int start = Math.max(0, size - count);
        return logs.subList(start, size);
    }

    /**
     * Барлық memory логтарды қайтару
     */
    public List<String> getAllRecentLogs() {
        return new ArrayList<>(recentLogs);
    }

    /**
     * Файлдан логтарды оқу
     */
    public List<String> readLogsFromFile(int maxLines) {
        List<String> logs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(logFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                logs.add(line);
            }

            // Соңғы N жолды қайтару
            int size = logs.size();
            int start = Math.max(0, size - maxLines);
            return logs.subList(start, size);

        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Logger-ді жабу
     */
    public synchronized void close() {
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * Лог файлын тазалау
     */
    public synchronized void clearLogFile() {
        try {
            if (writer != null) {
                writer.close();
            }

            Files.deleteIfExists(Paths.get(logFilePath));
            initializeLogFile();

            log("Log file cleared");

        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }
}
