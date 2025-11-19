/**
 * ANSI түстер және форматтау
 * Console шығысын әсемдеу үшін
 */
public class Colors {
    // Негізгі түстер
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Жарқын түстер
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Background түстер
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";

    // Форматтау
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String HIDDEN = "\u001B[8m";
    public static final String STRIKETHROUGH = "\u001B[9m";

    /**
     * Түсті мәтінді қайтару
     */
    public static String colored(String text, String color) {
        return color + text + RESET;
    }

    /**
     * Қызыл мәтін
     */
    public static String red(String text) {
        return RED + text + RESET;
    }

    /**
     * Жасыл мәтін
     */
    public static String green(String text) {
        return GREEN + text + RESET;
    }

    /**
     * Сары мәтін
     */
    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }

    /**
     * Көк мәтін
     */
    public static String blue(String text) {
        return BLUE + text + RESET;
    }

    /**
     * Cyan мәтін
     */
    public static String cyan(String text) {
        return CYAN + text + RESET;
    }

    /**
     * Bold мәтін
     */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    /**
     * Түсті және bold
     */
    public static String coloredBold(String text, String color) {
        return BOLD + color + text + RESET;
    }

    /**
     * Сәтті хабарлама (жасыл галочкамен)
     */
    public static String success(String text) {
        return GREEN + "✓ " + text + RESET;
    }

    /**
     * Қате хабарламасы (қызыл крестикпен)
     */
    public static String error(String text) {
        return RED + "✗ " + text + RESET;
    }

    /**
     * Ескерту (сары таңбамен)
     */
    public static String warning(String text) {
        return YELLOW + "⚠ " + text + RESET;
    }

    /**
     * Ақпарат (көк таңбамен)
     */
    public static String info(String text) {
        return CYAN + "ℹ " + text + RESET;
    }

}
