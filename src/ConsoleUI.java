import java.util.Scanner;

/**
 * Console UI утилиталары
 * - Түрлі-түсті шығыс
 * - Форматталған хабарламалар
 * - Loading эффекттері
 */
public class ConsoleUI {

    /**
     * Экранды тазалау
     */
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Экранды тазалау мүмкін болмаса, бірнеше жол қосу
            for (int i = 0; i < 3; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Негізгі менюді басып шығару
     */
    public void printMenu() {
        clearScreen();

        System.out.println(Colors.CYAN + Colors.BOLD);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║               BANKING SYSTEM v2.0              ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println(Colors.RESET);

        System.out.println(Colors.GREEN);
        System.out.println("  📋 Account Operations:");
        System.out.println("    1️⃣  Create new account");
        System.out.println("    2️⃣  Deposit money");
        System.out.println("    3️⃣  Withdraw money");
        System.out.println("    4️⃣  Transfer money");
        System.out.println();
        System.out.println("  📊 Information:");
        System.out.println("    5️⃣  Check balance");
        System.out.println("    6️⃣  View transaction history");
        System.out.println("    7️⃣  Change PIN code");
        System.out.println();
        System.out.println("  🔍 Search & Reports:");
        System.out.println("    8️⃣  Search account by name");
        System.out.println("    9️⃣  Show bank statistics");
        System.out.println("    🔟 Show all accounts");
        System.out.println();
        System.out.println("    0️⃣  Exit");
        System.out.println(Colors.RESET);
        System.out.println(Colors.CYAN + "════════════════════════════════════════════════" + Colors.RESET);
        System.out.println();
    }

    /**
     * Қош келдіңіз хабарламасы
     */
    public void printWelcome() {
        clearScreen();

        System.out.println(Colors.CYAN + Colors.BOLD);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                                                ║");
        System.out.println("║          Welcome to Banking System!            ║");
        System.out.println("║                                                ║");
        System.out.println("║          Secure • Fast • Reliable              ║");
        System.out.println("║                                                ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println(Colors.RESET);

        System.out.println(Colors.YELLOW + "Loading system..." + Colors.RESET);
        sleep(800);
        System.out.println(Colors.GREEN + "✓ System ready!" + Colors.RESET);
        sleep(500);
    }

    /**
     * Қоштасу хабарламасы
     */
    public void printGoodbye() {
        clearScreen();

        System.out.println(Colors.CYAN + Colors.BOLD);
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║                                                ║");
        System.out.println("║         Thank you for using our service!       ║");
        System.out.println("║                                                ║");
        System.out.println("║                    Goodbye!                    ║");
        System.out.println("║                                                ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        System.out.println(Colors.RESET);
    }

    /**
     * Басты жазу
     */
    public void printHeader(String title) {
        System.out.println();
        System.out.println(Colors.CYAN + Colors.BOLD);
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("═══════════════════════════════════════════════");
        System.out.println(Colors.RESET);
        System.out.println();
    }

    /**
     * Сәтті хабарлама
     */
    public void printSuccess(String message) {
        System.out.println(Colors.GREEN + "✓ " + message + Colors.RESET);
    }

    /**
     * Қате хабарламасы
     */
    public void printError(String message) {
        System.out.println(Colors.RED + "✗ " + message + Colors.RESET);
    }

    /**
     * Ақпарат хабарламасы
     */
    public void printInfo(String message) {
        System.out.println(Colors.CYAN + "ℹ " + message + Colors.RESET);
    }

    /**
     * Ескерту хабарламасы
     */
    public void printWarning(String message) {
        System.out.println(Colors.YELLOW + "⚠ " + message + Colors.RESET);
    }

    /**
     * Аккаунт ақпаратын басып шығару
     */
    public void printAccountInfo(AccountWithHistory account) {
        System.out.println(Colors.CYAN + "┌─────────────────────────────────────┐" + Colors.RESET);
        System.out.println(Colors.CYAN + "│  " + Colors.BOLD + "Account Information" +
                Colors.RESET + Colors.CYAN + "                │" + Colors.RESET);
        System.out.println(Colors.CYAN + "├─────────────────────────────────────┤" + Colors.RESET);

        System.out.printf(Colors.CYAN + "│  " + Colors.RESET + "ID: %s%-30s" +
                Colors.CYAN + "│" + Colors.RESET + "%n", Colors.BOLD, account.getId() + Colors.RESET);

        System.out.printf(Colors.CYAN + "│  " + Colors.RESET + "Owner: %-29s" +
                Colors.CYAN + "│" + Colors.RESET + "%n", account.getOwnerName());

        System.out.printf(Colors.CYAN + "│  " + Colors.RESET + "Balance: %s%.2f KZT%s%10s" +
                        Colors.CYAN + "│" + Colors.RESET + "%n",
                Colors.GREEN + Colors.BOLD, account.getBalance(), Colors.RESET, "");

        String status = account.isLocked() ?
                Colors.RED + "🔒 LOCKED" + Colors.RESET :
                Colors.GREEN + "✓ ACTIVE" + Colors.RESET;

        System.out.printf(Colors.CYAN + "│  " + Colors.RESET + "Status: %-37s" +
                Colors.CYAN + "│" + Colors.RESET + "%n", status);

        System.out.println(Colors.CYAN + "└─────────────────────────────────────┘" + Colors.RESET);
    }

    /**
     * Loading эффектін көрсету
     */
    public void showLoading(String message) {
        String[] animation = {"|", "/", "—", "\\"};
        System.out.print(Colors.YELLOW + message + " ");

        for (int i = 0; i < 12; i++) {
            System.out.print("\r" + Colors.YELLOW + message + " " +
                    animation[i % animation.length] + Colors.RESET);
            sleep(100);
        }

        System.out.println("\r" + Colors.GREEN + message + " ✓" + Colors.RESET + "  ");
    }

    /**
     * Progress bar көрсету
     */
    public void showProgressBar(String message, int steps) {
        System.out.print(Colors.CYAN + message + " [");

        for (int i = 0; i <= steps; i++) {
            int percent = (i * 100) / steps;
            int filled = (i * 30) / steps;

            System.out.print("\r" + Colors.CYAN + message + " [");

            for (int j = 0; j < 30; j++) {
                if (j < filled) {
                    System.out.print(Colors.GREEN + "█" + Colors.CYAN);
                } else {
                    System.out.print("░");
                }
            }

            System.out.print("] " + percent + "%");
            sleep(50);
        }

        System.out.println(Colors.GREEN + " ✓" + Colors.RESET);
    }

    /**
     * ENTER батырмасын басуды күту
     */
    public void pressEnterToContinue(Scanner scanner) {
        System.out.println();
        System.out.print(Colors.YELLOW + "Press ENTER to continue..." + Colors.RESET);
        scanner.nextLine();
    }

    /**
     * Растауды сұрау
     */
    public boolean askConfirmation(Scanner scanner, String message) {
        System.out.print(Colors.YELLOW + message + " (yes/no): " + Colors.RESET);
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Кідірту
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Бөлгіш сызық
     */
    public void printSeparator() {
        System.out.println(Colors.CYAN + "════════════════════════════════════════════════" +
                Colors.RESET);
    }

    /**
     * Кестені басып шығару
     */
    public void printTable(String[] headers, String[][] data) {
        // Баған ені есептеу
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
            for (String[] row : data) {
                if (row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
            widths[i] += 2; // Padding
        }

        // Жоғарғы сызық
        System.out.print(Colors.CYAN + "┌");
        for (int width : widths) {
            for (int i = 0; i < width; i++) System.out.print("─");
            System.out.print("┬");
        }
        System.out.println("\b┐" + Colors.RESET);

        // Headers
        System.out.print(Colors.CYAN + "│" + Colors.RESET);
        for (int i = 0; i < headers.length; i++) {
            System.out.print(Colors.BOLD + String.format(" %-" + (widths[i] - 1) + "s",
                    headers[i]) + Colors.RESET + Colors.CYAN + "│" + Colors.RESET);
        }
        System.out.println();

        // Бөлгіш
        System.out.print(Colors.CYAN + "├");
        for (int width : widths) {
            for (int i = 0; i < width; i++) System.out.print("─");
            System.out.print("┼");
        }
        System.out.println("\b┤" + Colors.RESET);

        // Деректер
        for (String[] row : data) {
            System.out.print(Colors.CYAN + "│" + Colors.RESET);
            for (int i = 0; i < row.length; i++) {
                System.out.print(String.format(" %-" + (widths[i] - 1) + "s", row[i]) +
                        Colors.CYAN + "│" + Colors.RESET);
            }
            System.out.println();
        }

        // Төменгі сызық
        System.out.print(Colors.CYAN + "└");
        for (int width : widths) {
            for (int i = 0; i < width; i++) System.out.print("─");
            System.out.print("┴");
        }
        System.out.println("\b┘" + Colors.RESET);
    }
}
