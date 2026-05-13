# Smart Expense Splitter — Demo Modes Guide

This document provides a comprehensive explanation of each demo mode available in Smart Expense Splitter, including the data used, operations performed, and expected outcomes.

---

## Overview

All demo modes use the `run.ps1` script on Windows. The script temporarily replaces the real data files (`data/users.json`, `data/groups.json`, `data/expenses.json`) with seeded test data, runs the application with a scripted set of CLI commands, and then restores the original data files.

### Seeded Demo Data (Used by All Demos)

**Users (data/users.json):**
```
a1b2  →  Alice  (alice@mail.com)
c3d4  →  Bob    (bob@mail.com)
e5f6  →  Charlie (charlie@mail.com)
```

**Groups (data/groups.json):**
```
g1  →  "Trip"
       Members: [a1b2, c3d4]  (initially Alice and Bob only)
```

**Expenses (data/expenses.json) — Initial Seed:**
```
e1  →  "Dinner"
       Total Amount: ₹300.00
       Paid By: a1b2 (Alice)
       Group: g1
       Split Type: EQUAL
       Splits:
         - a1b2 (Alice): ₹150.00
         - c3d4 (Bob): ₹150.00
```

**Financial Interpretation of Seed Data:**
- Alice paid ₹300.00 but her own share is only ₹150.00 → **Alice is owed ₹150.00**
- Bob paid ₹0.00 but his share is ₹150.00 → **Bob owes ₹150.00**
- Net settlement in group `g1`: Bob → Alice ₹150.00

---

## BASIC Demo

### Purpose
Show data visibility and basic balance computation.

### Commands Executed
```
help
list-users
list-groups
list-expenses g1
show-balances g1
exit
```

### What Each Command Does

| Command | Operation | Data Accessed | Output |
|---------|-----------|---------------|--------|
| `help` | Prints available CLI commands | None | Command list and usage |
| `list-users` | Reads and displays all users | `users.json` | Alice, Bob (and Charlie in seed) |
| `list-groups` | Reads and displays all groups | `groups.json` | Trip (g1), members: Alice, Bob |
| `list-expenses g1` | Queries expenses for group `g1` | `expenses.json` | Dinner (e1): ₹300.00, paid by Alice, split equally |
| `show-balances g1` | Computes net balance per user in `g1` using `BalanceService.getGroupBalances()` | `expenses.json` | Alice: +₹150.00 (owed), Bob: -₹150.00 (owes) |
| `exit` | Terminates CLI session | None | Application closes |

### Expected Output Summary
A brief, deterministic walkthrough showing:
- 2 users (Alice, Bob) in the seed
- 1 group (Trip) with 2 members
- 1 expense (Dinner, ₹300)
- Net balances: Alice is owed ₹150, Bob owes ₹150

### Use Case
**Best for:** First-time walkthrough, non-technical audience, quick sanity check of the app.

---

## ANALYTICS Demo

### Purpose
Show settlement optimization and analytical insights.

### Commands Executed
```
help
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit
```

### Additional Commands Explained

| Command | Operation | Data Accessed | Output |
|---------|-----------|---------------|--------|
| `show-balance-between a1b2 c3d4` | Computes bilateral net balance between Alice and Bob (across ALL groups, not just `g1`) using `BalanceService.getBalanceBetween()` | `expenses.json` | Bob owes Alice ₹150.00 (pairwise view) |
| `settle g1` | Runs `DebtSimplifier.simplify()` on group balances to produce a minimized settlement plan | `expenses.json` + BalanceService | Recommended transactions to settle debts (typically fewer transactions than raw balances) |
| `analytics-paid` | Aggregates total amount paid by each user | `expenses.json` | Alice: ₹300.00, Bob: ₹0.00, Charlie: ₹0.00 |
| `analytics-owed` | Aggregates total amount owed (in net terms) by each user | `expenses.json` | Bob: ₹150.00 (highest debtor) |
| `largest-debtor` | Ranks users and prints the one with highest net debt | BalanceService | Bob |
| `largest-creditor` | Ranks users and prints the one with highest net credit | BalanceService | Alice |

### Expected Output Summary
- Shows the seed's Dinner expense in detail
- Net balances in `g1`: Alice +₹150, Bob -₹150
- Pairwise balance: Bob owes Alice ₹150
- Settlement recommendation: 1 transaction (Bob → Alice ₹150)
- Analytics: Alice paid the most (₹300), Bob owes the most (₹150)

### Use Case
**Best for:** Demonstrating business logic, optimization features, and reporting capabilities.

---

## FULL Demo

### Purpose
Comprehensive end-to-end demonstration with multiple split types and multi-user scenario.

### Commands Executed
The `FULL` mode (triggered via `.\run.ps1 -DemoFull`) starts with the same seeded data but then executes additional commands that **mutate the dataset**:

```
help
list-users
list-groups
add-member g1 e5f6
add-expense g1 Brunch 120 a1b2 EQUAL a1b2 c3d4 e5f6
add-expense g1 Cab 90 c3d4 PERCENTAGE a1b2 30 c3d4 30 e5f6 40
add-expense g1 Snacks 60 a1b2 EXACT a1b2 10 c3d4 20 e5f6 30
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
analytics-share g1
monthly-report g1 2025 4
largest-debtor
largest-creditor
exit
```

### New Expenses Added in FULL

**Expense 2: Brunch (EQUAL split)**
```
Description: Brunch
Total: ₹120.00
Paid By: a1b2 (Alice)
Split Type: EQUAL (among 3 people)
Participants: a1b2, c3d4, e5f6
Each Share: ₹40.00

Financial Impact:
  Alice: +₹80.00 (paid 120, owed 40)
  Bob:   -₹40.00 (owes 40)
  Charlie: -₹40.00 (owes 40)
```

**Expense 3: Cab (PERCENTAGE split)**
```
Description: Cab
Total: ₹90.00
Paid By: c3d4 (Bob)
Split Type: PERCENTAGE
Distribution: Alice 30%, Bob 30%, Charlie 40%
Amounts: Alice ₹27, Bob ₹27, Charlie ₹36

Financial Impact:
  Alice: -₹27.00 (owes 27)
  Bob:   +₹63.00 (paid 90, owed 27)
  Charlie: -₹36.00 (owes 36)
```

**Expense 4: Snacks (EXACT split)**
```
Description: Snacks
Total: ₹60.00
Paid By: a1b2 (Alice)
Split Type: EXACT (user specifies exact amounts)
Amounts: Alice ₹10, Bob ₹20, Charlie ₹30

Financial Impact:
  Alice: +₹50.00 (paid 60, owed 10)
  Bob:   -₹20.00 (owes 20)
  Charlie: -₹30.00 (owes 30)
```

### Aggregate Net Balances After All 4 Expenses

```
Cumulative Balances (Dinner + Brunch + Cab + Snacks):

  Alice (a1b2):
    Dinner:   +₹150.00
    Brunch:   +₹80.00
    Cab:      -₹27.00
    Snacks:   +₹50.00
    ─────────────────
    Total:    +₹253.00  (Alice is owed ₹253.00)

  Bob (c3d4):
    Dinner:   -₹150.00
    Brunch:   -₹40.00
    Cab:      +₹63.00
    Snacks:   -₹20.00
    ─────────────────
    Total:    -₹147.00  (Bob owes ₹147.00)

  Charlie (e5f6):
    Dinner:    ₹0.00 (not in group yet)
    Brunch:   -₹40.00
    Cab:      -₹36.00
    Snacks:   -₹30.00
    ─────────────────
    Total:    -₹106.00  (Charlie owes ₹106.00)
```

### Settlement Recommendation (`settle g1`)

The `DebtSimplifier` runs a greedy algorithm to minimize transactions:

```
Recommended Transactions:
  1. Bob → Alice: ₹147.00
  2. Charlie → Alice: ₹106.00

Result: 2 transactions settle all debts optimally
```

### Analytics Output

| Metric | Values |
|--------|--------|
| `analytics-paid` | Alice: ₹480.00 (300+120+60), Bob: ₹90.00, Charlie: ₹0.00 |
| `analytics-owed` | Alice: +₹253.00 (creditor), Bob: -₹147.00, Charlie: -₹106.00 |
| `largest-debtor` | Bob (-₹147.00) |
| `largest-creditor` | Alice (+₹253.00) |
| `monthly-report g1 2025 4` | Summary of all expenses for April 2025 in group `g1` |

### Expected Output Summary
- 3 users in group (Alice, Bob, Charlie after add-member)
- 4 total expenses demonstrating EQUAL, PERCENTAGE, and EXACT split types
- Complex net balances showing the effect of multiple split strategies
- Optimized settlement with 2 transactions
- Comprehensive analytics showing paid amounts, owed amounts, and top debtor/creditor

### Use Case
**Best for:** Complete product demonstration, showing all features (multiple split types, multi-user scenarios, debt optimization, analytics).

---

## CUSTOM Demo

### Purpose
Run user-defined CLI commands against the seeded demo data.

### How It Works
Instead of using a fixed command list, `CUSTOM` mode allows you to provide your own commands:

```powershell
# Local usage
.\run.ps1 -Commands @('help', 'list-users', 'exit')

# Jenkins usage (via Jenkinsfile parameter)
DEMO_MODE = CUSTOM
APP_COMMANDS = """
help
list-users
exit
"""
```

### Typical Use Cases
1. **Specific Feature Test:** Focus on one CLI command (e.g., just `show-balances g1`)
2. **Bug Reproduction:** Recreate a specific user scenario
3. **Partial Walkthrough:** Mix commands from BASIC and ANALYTICS
4. **Custom Data Mutations:** Add your own expenses and check results

### Example Custom Command Sequences

**Example 1: Quick Balance Check**
```
help
show-balances g1
exit
```
Expected: Just the Dinner expense and Alice/Bob balances.

**Example 2: Multi-Group Test (if you had multiple groups in seed)**
```
list-groups
list-expenses g1
show-balances g1
exit
```

**Example 3: Pairwise Balance Deep Dive**
```
show-balance-between a1b2 c3d4
show-balance-between a1b2 e5f6
show-balance-between c3d4 e5f6
exit
```

### Important Notes
- `CUSTOM` uses the **seeded demo data** (same as other modes).
- `Use-DemoDataset` still backs up and restores the original `data/` files after `CUSTOM` runs.
- Commands are executed sequentially; if a command fails, later commands may still run.
- Use valid entity IDs from the seed (users: `a1b2`, `c3d4`, `e5f6`; group: `g1`).

### Use Case
**Best for:** Ad-hoc testing, CI/CD custom scenarios, focused feature validation.

---

## Jenkins Integration

The `Jenkinsfile` pipeline orchestrates demo execution in a CI environment:

### Build Stages
1. **Checkout:** Clones the repository
2. **Build & Test:** Runs `mvn -B clean verify`
3. **Package:** Runs `mvn -B -DskipTests package`
4. **Run App Demo:** Writes `target/commands.txt` and executes:
   ```bash
   java -jar target/expense-splitter.jar < target/commands.txt | tee target/app-output.log
   ```

### Demo Mode Selection
Pipeline parameter `DEMO_MODE` (choices: `BASIC`, `ANALYTICS`, `FULL`, `CUSTOM`) determines which commands are written to `target/commands.txt`:
- `BASIC` → basic command set
- `ANALYTICS` → analytics command set
- `FULL` → full end-to-end command set
- `CUSTOM` → uses `APP_COMMANDS` parameter (user-provided)

### Output Artifacts
- `target/expense-splitter.jar` — packaged application
- `target/app-output.log` — captured CLI output
- `target/surefire-reports/*.xml` — JUnit test results (published via Jenkins)

---

## Running the Demos Locally

### Prerequisites
- Java 17 JDK
- Maven 3.8+
- Windows PowerShell (for `run.ps1`)

### Quick Start

```powershell
# Navigate to project root
Set-Location E:\SmartExpenseSplitter

# Build the project
mvn clean package

# Run BASIC demo
.\run.ps1 -Demo

# Run ANALYTICS demo (requires -Demo flag logic adjustment or edit run.ps1)
# Currently run.ps1 uses BASIC, ANALYTICS, FULL, or CUSTOM selection via -Demo, -DemoWrite, -DemoFull

# Run FULL demo
.\run.ps1 -DemoFull

# Run CUSTOM demo
.\run.ps1 -Commands @('help', 'list-users', 'exit')
```

### What Happens After Each Run
1. CLI commands are executed sequentially
2. Output is displayed in the terminal
3. Demo dataset is restored to backup (original `data/` files are restored)
4. Application exits cleanly

---

## Summary Table

| Demo Mode | Scope | Focus | Expenses | Users | Best For |
|-----------|-------|-------|----------|-------|----------|
| **BASIC** | Data visibility | Simple queries | 1 (Dinner) | 2 (Alice, Bob) | First-time walkthrough |
| **ANALYTICS** | Business insights | Settlement & reporting | 1 (Dinner) | 2 (Alice, Bob) | Feature showcase |
| **FULL** | Complete workflow | Multi-user, multi-split types | 4 (Dinner, Brunch, Cab, Snacks) | 3 (Alice, Bob, Charlie) | End-to-end product demo |
| **CUSTOM** | User-defined | Flexible scenario testing | Seed data (configurable) | Seed data (configurable) | Ad-hoc testing, CI/CD |

---

## Troubleshooting

### Demo Commands Not Recognized
- Verify entity IDs match the seed data (`a1b2`, `c3d4`, `e5f6`, `g1`).
- Check command syntax in `README.md` under "Detailed Walkthrough".

### Data Persists After Demo (Not Restored)
- Ensure `run.ps1` is being used (it has `Use-DemoDataset` for backup/restore).
- If using raw JAR, data mutations are permanent; restore `data/` files manually from version control.

### Maven Build Fails
- Ensure `pom.xml` references `App` (entry point), not `Main`.
- Run `mvn clean validate` to refresh project configuration.

### AppTest Failures
- Ensure `src/test/java/AppTest.java` compiles against model classes.
- Run `mvn test` to see detailed error messages.

---

## Additional Resources

- **README.md:** Comprehensive usage guide and command reference
- **Jenkinsfile:** CI/CD pipeline definition for Jenkins
- **pom.xml:** Maven build configuration with entry point and test settings
- **App.java:** Application entry point (bootstraps repositories, services, CLI)
- **AppTest.java:** Unit tests for core business logic (DebtSimplifier, BalanceService)

