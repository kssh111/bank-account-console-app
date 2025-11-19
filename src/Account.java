import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Жетілдірілген Account класы
 * - Exception handling
 * - PIN хэштеу (SHA-256)
 * - Validation
 * - Immutable ID
 */

public class Account implements Serializable {
    private static final long serialVersionUID = 2L;

    // Константалар
    private static final double MIN_AMOUNT = 0.01;
    private static final int PIN_LENGTH = 4;

    // Өрістер
    private final int id;
    private String ownerName;
    private double balance;
    private String pinCodeHash; // Хэштелген PIN
    private final LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private boolean isLocked; // Блокталған аккаунт
    private int failedPinAttempts;

    /**
     * Конструктор - тек BankService пайдалануы керек
     */
    public Account(int id, String ownerName, String pinCode) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }
        if (!isValidPin(pinCode)) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }

        this.id = id;
        this.ownerName = ownerName.trim();
        this.pinCodeHash = hashPin(pinCode);
        this.balance = 0.0;
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.isLocked = false;
        this.failedPinAttempts = 0;
    }

    /**
     * PIN кодты тексереді
     */
    public boolean checkPin(String inputPin) throws InvalidPinException {
        if (isLocked) {
            throw new InvalidPinException(id, 0);
        }

        boolean correct = pinCodeHash.equals(hashPin(inputPin));

        if (!correct) {
            failedPinAttempts++;
            if (failedPinAttempts >= 3) {
                isLocked = true;
                throw new InvalidPinException(id, 0);
            }
            throw new InvalidPinException(id, 3 - failedPinAttempts);
        }

        // PIN дұрыс болса, әрекеттерді reset қылу
        failedPinAttempts = 0;
        return true;
    }

    /**
     * Ақша салу
     */
    public void deposit(double amount) throws InvalidAmountException {
        validateAmount(amount);
        balance += amount;
        lastModified = LocalDateTime.now();
    }

    /**
     * Ақша алу
     */
    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        validateAmount(amount);

        if (amount > balance) {
            throw new InsufficientFundsException(balance, amount);
        }

        balance -= amount;
        lastModified = LocalDateTime.now();
    }

    /**
     * PIN-ді өзгерту
     */
    public void changePin(String oldPin, String newPin) throws InvalidPinException {
        if (!checkPin(oldPin)) {
            throw new InvalidPinException(id);
        }

        if (!isValidPin(newPin)) {
            throw new IllegalArgumentException("New PIN must be exactly 4 digits");
        }

        this.pinCodeHash = hashPin(newPin);
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Аккаунтты unlock жасау (тек админ үшін)
     */
    public void unlock() {
        this.isLocked = false;
        this.failedPinAttempts = 0;
    }

    // ============ VALIDATION ============

    private void validateAmount(double amount) throws InvalidAmountException {
        if (amount < MIN_AMOUNT) {
            throw new InvalidAmountException(amount);
        }
    }

    private boolean isValidPin(String pin) {
        if (pin == null || pin.length() != PIN_LENGTH) {
            return false;
        }
        // Тек сандардан тұруы керек
        return pin.matches("\\d{4}");
    }

    // ============ PIN ХЭШТЕУ (SHA-256) ============

    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());

            // Byte массивін hex string-ке айналдыру
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ============ GETTERS ============

    public int getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public int getFailedPinAttempts() {
        return failedPinAttempts;
    }

    // ============ SETTERS (шектеулі) ============

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }
        this.ownerName = ownerName.trim();
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Тек файлдан оқығанда пайдалану үшін
     */
    protected void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("Account[ID=%d, Owner=%s, Balance=%.2f, Locked=%s]",
                id, ownerName, balance, isLocked);
    }

}
