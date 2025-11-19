/**
 * Account жасау үшін Builder паттерні
 * Ескі кодпен compatibility қамтамасыз етеді
 */
public class AccountBuilder {
    private int id;
    private String ownerName;
    private String pinCode;
    private double initialBalance = 0.0;

    public AccountBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public AccountBuilder setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public AccountBuilder setPinCode(String pinCode) {
        this.pinCode = pinCode;
        return this;
    }

    public AccountBuilder setInitialBalance(double balance) {
        this.initialBalance = balance;
        return this;
    }

    public Account build() {
        Account account = new Account(id, ownerName, pinCode);
        if (initialBalance > 0) {
            try {
                account.deposit(initialBalance);
            } catch (InvalidAmountException e) {
                // Болмауы керек, себебі біз тексердік
            }
        }
        return account;
    }

    /**
     * Ескі форматтан жаңа форматқа миграция
     */
    public static Account fromLegacyAccount(Account oldAccount, String plainTextPin) {
        try {
            Account newAccount = new Account(
                    oldAccount.getId(),
                    oldAccount.getOwnerName(),
                    plainTextPin // Ескі PIN (хэштелмеген)
            );
            // Balance-ті көшіру
            if (oldAccount.getBalance() > 0) {
                newAccount.deposit(oldAccount.getBalance());
            }
            return newAccount;
        } catch (Exception e) {
            throw new RuntimeException("Failed to migrate account", e);
        }
    }
}
