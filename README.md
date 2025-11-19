# 🏦 Bank Account Console Application

A professional banking system console application with deposit, withdrawal, transfer operations, and comprehensive transaction history management.

## 📋 Table of Contents

- [Description](#-description)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Installation](#-installation)
- [Usage](#-usage)
- [Screenshots](#-screenshots)
- [Technical Details](#-technical-details)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)

---

## 🎯 Description

This is a banking system console application written in Java. The application simulates real banking operations: account creation, deposits/withdrawals, transfers, transaction history viewing, and statistics generation.

### Main Highlights:

- ✅ **Thread-safe** operations
- 🔐 **SHA-256** PIN hashing
- 📊 **Complete transaction history**
- 💾 **Automatic backup system**
- 🎨 **Colorful console interface**
- 🏗️ **Repository pattern** (file/memory storage)
- ⚡ **Exception handling** for all operations

---

## 🚀 Key Features

### 1️⃣ Account Management
- Create new accounts (with 4-digit PIN code)
- Change PIN code
- Search accounts (by owner name)
- Unlock locked accounts

### 2️⃣ Financial Operations
- **Deposit**: Add money to account
- **Withdraw**: Remove money from account
- **Transfer**: Transfer money between accounts
- Real-time balance checking

### 3️⃣ Security
- SHA-256 PIN hashing
- Account locks after 3 failed PIN attempts
- PIN verification for all operations

### 4️⃣ Reports and Statistics
- Complete transaction history
- Account statistics (total deposits/withdrawals)
- Bank-wide statistics
- Transaction logs

### 5️⃣ Data Persistence
- Automatic file saving
- Backup system (keeps last 5 backups)
- Atomic file operations
- Recovery after crashes

---

## 🏗️ Architecture

The project is built following **Clean Architecture** principles:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                 │
│  (ConsoleApp, ConsoleUI, Colors)            │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│          Business Logic Layer                │
│         (BankService)                        │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│           Domain Layer                       │
│  (Account, AccountWithHistory, Transaction) │
└─────────────┬───────────────────────────────┘
              │
┌─────────────▼───────────────────────────────┐
│         Data Access Layer                    │
│  (AccountRepository, FileAccountRepository) │
└─────────────────────────────────────────────┘
```

### Design Patterns:
- **Repository Pattern**: Abstraction of data access
- **Builder Pattern**: Account object creation
- **Singleton Pattern**: For logger
- **Strategy Pattern**: Storage strategies (File/Memory)

---

## 💻 Installation

### Requirements:
- ☕ Java 11 or higher
- 📦 Git (for cloning)

### Steps:

1. **Clone the repository:**
```bash
git clone https://github.com/kssh111/bank-account-console-app.git
cd bank-account-console-app
```

2. **Compile:**
```bash
javac *.java
```

3. **Run:**
```bash
java Main
```

### Alternative (Create JAR file):
```bash
jar cvfe BankApp.jar Main *.class
java -jar BankApp.jar
```

---

## 📖 Usage

### Main Menu:

```
================ BANKING SYSTEM ================

 📋 Account Operations:
    1️⃣  Create new account
    2️⃣  Deposit money
    3️⃣  Withdraw money
    4️⃣  Transfer money

 📊 Information:
    5️⃣  Check balance
    6️⃣  View transaction history
    7️⃣  Change PIN code

 🔍 Search & Reports:
    8️⃣  Search account by name
    9️⃣  Show bank statistics
    🔟 Show all accounts

    0️⃣  Exit
```

### Examples:

#### 1. Create Account
```
Enter owner name: John Doe
Enter 4-digit PIN code: 1234
Confirm PIN code: 1234

✓ Account created successfully!
ℹ Your Account ID: 1
ℹ Owner: John Doe
```

#### 2. Deposit Money
```
Enter Account ID: 1
Enter PIN code: 1234
Enter deposit amount: 50000

✓ Deposit successful!
ℹ New balance: 50000.00 KZT
```

#### 3. Transfer Money
```
FROM Account ID: 1
Enter PIN code: 1234
TO Account ID: 2
Enter transfer amount: 10000
Confirm transfer? (yes/no): yes

✓ Transfer completed successfully!
ℹ New balance: 40000.00 KZT
```

#### 4. Transaction History
```
How many transactions to show? (default 10): 5

Last 5 transactions:
  [2024-11-19 15:30:22] Account Created
  [2024-11-19 15:31:45] Deposit 50000.00 KZT
  [2024-11-19 15:35:10] Transfer Out 10000.00 KZT → Account #2
  [2024-11-19 16:20:33] Withdraw 5000.00 KZT
  [2024-11-19 16:45:12] Deposit 15000.00 KZT
```

---

## 📸 Screenshots

### Welcome Screen
```
╔════════════════════════════════════════════════╗
║                                                ║
║          Welcome to Banking System!            ║
║                                                ║
║          Secure • Fast • Reliable              ║
║                                                ║
╚════════════════════════════════════════════════╝
```
*Screenshot: welcome_screen.png*

### Account Information
```
┌─────────────────────────────────────┐
│  Account Information                │
├─────────────────────────────────────┤
│  ID: 1                              │
│  Owner: John Doe                    │
│  Balance: 50000.00 KZT              │
│  Status: ✓ ACTIVE                   │
└─────────────────────────────────────┘
```
*Screenshot: account_info.png*

### Statistics
```
Bank Statistics:
  Total Accounts: 15
  Active Accounts: 13
  Locked Accounts: 2
  Total Balance: 2450000.00 KZT

Account #1 (John Doe)
  Current Balance: 50000.00 KZT
  Total Deposited: 120000.00 KZT
  Total Withdrawn: 70000.00 KZT
  Transactions: 47
  Status: ACTIVE
```
*Screenshot: statistics.png*

---

## 🔧 Technical Details

### Exception Handling

The project includes custom exception classes:

- **`AccountNotFoundException`**: Thrown when account is not found
- **`InvalidPinException`**: Thrown when incorrect PIN is entered
- **`InvalidAmountException`**: Thrown when invalid amount is entered
- **`InsufficientFundsException`**: Thrown when balance is insufficient

### Thread Safety

- **`synchronized`** methods for critical operations
- **`ConcurrentHashMap`** for storing accounts
- **Deadlock prevention**: Sorting by ID
- **Atomic file operations**: To prevent data loss

### Security

```java
// PIN hashing (SHA-256)
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(pin.getBytes());

// 3 attempt limit
if (failedPinAttempts >= 3) {
    isLocked = true;
    throw new InvalidPinException(id, 0);
}
```

### File Structure

```
data/
├── accounts.dat          # Main data file
├── accounts.dat.tmp      # Temporary file (for atomic writes)
└── backups/
    ├── accounts_backup_1700123456789.dat
    ├── accounts_backup_1700234567890.dat
    └── ...
logs/
└── transactions.log      # Transaction logs
```

---

## 📁 Project Structure

```
bank-account-console-app/
│
├── src/
│   ├── Account.java                    # Base account class
│   ├── AccountWithHistory.java         # Account with history
│   ├── AccountBuilder.java             # Builder pattern
│   ├── AccountRepository.java          # Repository interface
│   ├── FileAccountRepository.java      # File storage implementation
│   ├── InMemoryAccountRepository.java  # Memory storage implementation
│   ├── BankService.java                # Business logic
│   ├── ConsoleApp.java                 # Main application
│   ├── ConsoleUI.java                  # UI utilities
│   ├── Transaction.java                # Transaction class
│   ├── TransactionType.java            # Enum: transaction types
│   ├── TransactionLogger.java          # Logging functionality
│   ├── Colors.java                     # ANSI colors
│   ├── Main.java                       # Entry point
│   └── exceptions/
│       ├── AccountNotFoundException.java
│       ├── InvalidPinException.java
│       ├── InvalidAmountException.java
│       └── InsufficientFundsException.java
│
├── data/                               # Data directory (auto-created)
├── README.md                           # This file
└── LICENSE                             # MIT License
```

---

## 🧪 Testing

### Unit Tests (Future implementation):
```java
@Test
public void testDepositIncreasesBalance() {
    AccountWithHistory acc = new AccountWithHistory(1, "Test", "1234");
    acc.checkPin("1234");
    acc.deposit(1000.0);
    assertEquals(1000.0, acc.getBalance(), 0.01);
}
```

### Manual Testing:

1. ✅ Account creation and PIN verification
2. ✅ Deposit/Withdraw operations
3. ✅ Transfer (deadlock testing)
4. ✅ File save/load
5. ✅ Backup/restore mechanism
6. ✅ Exception handling

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style:
- ✅ Java naming conventions
- ✅ Javadoc comments
- ✅ Proper exception handling
- ✅ Thread-safe code

---

## 👨‍💻 Author

**kssh111**

- GitHub: [@kssh111](https://github.com/kssh111)
- Repository: [bank-account-console-app](https://github.com/kssh111/bank-account-console-app)

---

## 📞 Contact

If you have questions or find issues:
- 🐛 Open an issue: [GitHub Issues](https://github.com/kssh111/bank-account-console-app/issues)
- ⭐ Star the project if you like it!

---

## 🎓 Learning Resources

Technologies used in this project:

- [Java Documentation](https://docs.oracle.com/en/java/)
- [Design Patterns](https://refactoring.guru/design-patterns)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Thread Safety in Java](https://docs.oracle.com/javase/tutorial/essential/concurrency/)

---

## 🔮 Future Plans

- [ ] GUI interface (JavaFX)
- [ ] Database support (PostgreSQL/MySQL)
- [ ] REST API
- [ ] Multi-currency support
- [ ] SMS/Email notifications
- [ ] Loan system
- [ ] Unit tests (JUnit)
- [ ] CI/CD pipeline

---

<div align="center">

**⭐ If you like this project, don't forget to give it a star! ⭐**

Made with ❤️ and ☕

</div>
