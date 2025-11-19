public class Dashboard {
    public static void printMenu() {
        Screen.clear();
        System.out.println(Colors.CYAN + Colors.BOLD +
                "\n================ BANKING SYSTEM ================" + Colors.RESET);

        System.out.println(Colors.GREEN +
                "\n 1) Create new account");
        System.out.println(" 2) Deposit money");
        System.out.println(" 3) Withdraw money");
        System.out.println(" 4) Transfer money");
        System.out.println(" 5) Show all accounts");
        System.out.println(" 6) Exit\n" + Colors.RESET);

        System.out.println(Colors.YELLOW + "Choose option: " + Colors.RESET);
    }
}
