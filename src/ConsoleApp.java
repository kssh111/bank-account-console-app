import java.util.List;
import java.util.Scanner;

/**
 * Жетілдірілген Console интерфейсі
 * - Дұрыс exception handling
 * - User-friendly хабарламалар
 * - Input validation
 * - Graceful shutdown
 */
public class ConsoleApp {

    private final Scanner scanner;
    private final BankService bankService;
    private final ConsoleUI ui;

    public ConsoleApp() {
        this.scanner = new Scanner(System.in);

        // Repository таңдау (файл немесе memory)
        AccountRepository repository = new FileAccountRepository();
        this.bankService = new BankService(repository);
        this.ui = new ConsoleUI();

        // Shutdown hook қосу
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n" + Colors.YELLOW + "Saving data..." + Colors.RESET);
            scanner.close();
        }));
    }

    public void run() {
        ui.printWelcome();

        while (true) {
            try {
                ui.printMenu();
                int choice = readMenuChoice();

                if (!handleMenuChoice(choice)) {
                    break; // Exit
                }

                ui.pressEnterToContinue(scanner);

            } catch (Exception e) {
                ui.printError("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        ui.printGoodbye();
    }

    /**
     * Меню таңдау оқу
     */
    private int readMenuChoice() {
        while (true) {
            try {
                System.out.print(Colors.PURPLE + "Choose option ➜ " + Colors.RESET);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {
                ui.printError("Please enter a valid number!");
            }
        }
    }

    /**
     * Менюдегі таңдауды өңдеу
     */
    private boolean handleMenuChoice(int choice) {
        try {
            switch (choice) {
                case 1: handleCreateAccount(); break;
                case 2: handleDeposit(); break;
                case 3: handleWithdraw(); break;
                case 4: handleTransfer(); break;
                case 5: handleCheckBalance(); break;
                case 6: handleViewHistory(); break;
                case 7: handleChangePin(); break;
                case 8: handleSearchAccount(); break;
                case 9: handleShowStatistics(); break;
                case 10: handleShowAllAccounts(); break;
                case 0: return false; // Exit
                default:
                    ui.printError("Invalid option. Please try again!");
            }
        } catch (Exception e) {
            ui.printError("Operation failed: " + e.getMessage());
        }

        return true;
    }

    /**
     * 1. Аккаунт жасау
     */
    private void handleCreateAccount() {
        ui.printHeader("CREATE NEW ACCOUNT");

        System.out.print("Enter owner name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            ui.printError("Name cannot be empty!");
            return;
        }

        String pin = readPin("Enter 4-digit PIN code");
        String confirmPin = readPin("Confirm PIN code");

        if (!pin.equals(confirmPin)) {
            ui.printError("PIN codes do not match!");
            return;
        }

        try {
            ui.showLoading("Creating account");
            AccountWithHistory account = bankService.createAccount(name, pin);

            ui.printSuccess("Account created successfully!");
            ui.printInfo("Your Account ID: " + account.getId());
            ui.printInfo("Owner: " + account.getOwnerName());

        } catch (IllegalArgumentException e) {
            ui.printError(e.getMessage());
        }
    }

    /**
     * 2. Ақша салу
     */
    private void handleDeposit() {
        ui.printHeader("DEPOSIT MONEY");

        int accountId = readAccountId();
        if (accountId == -1) return;

        try {
            AccountWithHistory account = bankService.getAccount(accountId);
            ui.printAccountInfo(account);

            String pin = readPin("Enter PIN code");
            double amount = readAmount("Enter deposit amount");

            if (amount <= 0) {
                ui.printError("Amount must be positive!");
                return;
            }

            ui.showLoading("Processing deposit");
            bankService.deposit(accountId, amount, pin);

            ui.printSuccess("Deposit successful!");
            ui.printInfo(String.format("New balance: %.2f KZT",
                    bankService.getAccount(accountId).getBalance()));

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        } catch (InvalidAmountException e) {
            ui.printError("Invalid amount: " + e.getMessage());
        }
    }

    /**
     * 3. Ақша алу
     */
    private void handleWithdraw() {
        ui.printHeader("WITHDRAW MONEY");

        int accountId = readAccountId();
        if (accountId == -1) return;

        try {
            AccountWithHistory account = bankService.getAccount(accountId);
            ui.printAccountInfo(account);

            String pin = readPin("Enter PIN code");
            double amount = readAmount("Enter withdraw amount");

            if (amount <= 0) {
                ui.printError("Amount must be positive!");
                return;
            }

            ui.showLoading("Processing withdrawal");
            bankService.withdraw(accountId, amount, pin);

            ui.printSuccess("Withdrawal successful!");
            ui.printInfo(String.format("New balance: %.2f KZT",
                    bankService.getAccount(accountId).getBalance()));

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        } catch (InsufficientFundsException e) {
            ui.printError(String.format(
                    "Insufficient funds! Balance: %.2f KZT, Requested: %.2f KZT",
                    e.getBalance(), e.getRequestedAmount()));
        } catch (InvalidAmountException e) {
            ui.printError("Invalid amount: " + e.getMessage());
        }
    }

    /**
     * 4. Ақша аудару
     */
    private void handleTransfer() {
        ui.printHeader("TRANSFER MONEY");

        int fromId = readAccountId("FROM Account ID");
        if (fromId == -1) return;

        try {
            AccountWithHistory fromAccount = bankService.getAccount(fromId);
            ui.printAccountInfo(fromAccount);

            String pin = readPin("Enter PIN code");

            int toId = readAccountId("TO Account ID");
            if (toId == -1) return;

            if (fromId == toId) {
                ui.printError("Cannot transfer to the same account!");
                return;
            }

            AccountWithHistory toAccount = bankService.getAccount(toId);
            System.out.println(Colors.CYAN + "Transfer to: " + toAccount.getOwnerName() +
                    " (ID: " + toId + ")" + Colors.RESET);

            double amount = readAmount("Enter transfer amount");

            System.out.print("Confirm transfer? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes") && !confirm.equals("y")) {
                ui.printInfo("Transfer cancelled.");
                return;
            }

            ui.showLoading("Processing transfer");
            bankService.transfer(fromId, toId, amount, pin);

            ui.printSuccess("Transfer completed successfully!");
            ui.printInfo(String.format("New balance: %.2f KZT",
                    bankService.getAccount(fromId).getBalance()));

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        } catch (InsufficientFundsException e) {
            ui.printError(String.format(
                    "Insufficient funds! Balance: %.2f KZT, Requested: %.2f KZT",
                    e.getBalance(), e.getRequestedAmount()));
        } catch (Exception e) {
            ui.printError("Transfer failed: " + e.getMessage());
        }
    }

    /**
     * 5. Балансты тексеру
     */
    private void handleCheckBalance() {
        ui.printHeader("CHECK BALANCE");

        int accountId = readAccountId();
        if (accountId == -1) return;

        try {
            AccountWithHistory account = bankService.getAccount(accountId);
            String pin = readPin("Enter PIN code");

            account.checkPin(pin);

            ui.printAccountInfo(account);

            AccountStatistics stats = bankService.getAccountStatistics(accountId);
            System.out.println("\n" + stats);

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        }
    }

    /**
     * 6. Транзакция тарихын көру
     */
    private void handleViewHistory() {
        ui.printHeader("TRANSACTION HISTORY");

        int accountId = readAccountId();
        if (accountId == -1) return;

        try {
            AccountWithHistory account = bankService.getAccount(accountId);
            String pin = readPin("Enter PIN code");

            account.checkPin(pin);

            System.out.print("How many transactions to show? (default 10): ");
            String input = scanner.nextLine().trim();
            int count = input.isEmpty() ? 10 : Integer.parseInt(input);

            List<Transaction> transactions = account.getLastTransactions(count);

            if (transactions.isEmpty()) {
                ui.printInfo("No transactions found.");
            } else {
                System.out.println("\n" + Colors.CYAN + Colors.BOLD +
                        "Last " + transactions.size() + " transactions:" + Colors.RESET);

                for (Transaction tx : transactions) {
                    System.out.println("  " + tx.toColoredString());
                }
            }

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        } catch (NumberFormatException e) {
            ui.printError("Invalid number format!");
        }
    }

    /**
     * 7. PIN өзгерту
     */
    private void handleChangePin() {
        ui.printHeader("CHANGE PIN CODE");

        int accountId = readAccountId();
        if (accountId == -1) return;

        try {
            String oldPin = readPin("Enter current PIN code");
            String newPin = readPin("Enter new PIN code (4 digits)");
            String confirmPin = readPin("Confirm new PIN code");

            if (!newPin.equals(confirmPin)) {
                ui.printError("New PIN codes do not match!");
                return;
            }

            ui.showLoading("Changing PIN");
            bankService.changePin(accountId, oldPin, newPin);

            ui.printSuccess("PIN code changed successfully!");

        } catch (AccountNotFoundException e) {
            ui.printError("Account not found!");
        } catch (InvalidPinException e) {
            handlePinError(e);
        }
    }

    /**
     * 8. Аккаунт іздеу
     */
    private void handleSearchAccount() {
        ui.printHeader("SEARCH ACCOUNT");

        System.out.print("Enter owner name to search: ");
        String searchName = scanner.nextLine().trim();

        if (searchName.isEmpty()) {
            ui.printError("Search query cannot be empty!");
            return;
        }

        List<AccountWithHistory> results = bankService.searchByOwner(searchName);

        if (results.isEmpty()) {
            ui.printInfo("No accounts found matching: " + searchName);
        } else {
            System.out.println("\n" + Colors.GREEN + "Found " + results.size() +
                    " account(s):" + Colors.RESET);

            for (AccountWithHistory account : results) {
                System.out.println("  " + account);
            }
        }
    }

    /**
     * 9. Статистиканы көрсету
     */
    private void handleShowStatistics() {
        ui.printHeader("BANK STATISTICS");

        BankStatistics stats = bankService.getBankStatistics();
        System.out.println("\n" + stats);

        System.out.print("\nShow recent activity logs? (yes/no): ");
        String show = scanner.nextLine().trim().toLowerCase();

        if (show.equals("yes") || show.equals("y")) {
            List<String> logs = bankService.getRecentLogs(10);

            if (!logs.isEmpty()) {
                System.out.println("\n" + Colors.CYAN + "Recent Activity:" + Colors.RESET);
                for (String log : logs) {
                    System.out.println("  " + log);
                }
            }
        }
    }

    /**
     * 10. Барлық аккаунттарды көрсету
     */
    private void handleShowAllAccounts() {
        ui.printHeader("ALL ACCOUNTS");

        List<AccountWithHistory> accounts = bankService.getAllAccounts();

        if (accounts.isEmpty()) {
            ui.printInfo("No accounts in the system.");
            return;
        }

        System.out.println(Colors.GREEN + "Total accounts: " + accounts.size() + Colors.RESET);
        System.out.println();

        for (AccountWithHistory account : accounts) {
            System.out.println(account);
            System.out.println("  Transactions: " + account.getTransactionCount());
            System.out.println();
        }
    }

    // ============ HELPER METHODS ============

    private int readAccountId() {
        return readAccountId("Enter Account ID");
    }

    private int readAccountId(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    return -1;
                }

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {
                ui.printError("Invalid ID format! Please enter a number.");
            }
        }
    }

    private String readPin(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    private double readAmount(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);

            } catch (NumberFormatException e) {
                ui.printError("Invalid amount! Please enter a number.");
            }
        }
    }

    private void handlePinError(InvalidPinException e) {
        if (e.hasAttemptsInfo() && e.getAttemptsRemaining() > 0) {
            ui.printError(String.format(
                    "Wrong PIN! Attempts remaining: %d", e.getAttemptsRemaining()));
        } else if (e.getAttemptsRemaining() == 0) {
            ui.printError("Account locked due to too many failed PIN attempts!");
        } else {
            ui.printError("Invalid PIN code!");
        }
    }

    // ============ MAIN ============

    public static void main(String[] args) {
        ConsoleApp app = new ConsoleApp();
        app.run();
    }
}