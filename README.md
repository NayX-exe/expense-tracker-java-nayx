# ExpenseTracker

A modern, dark-themed personal finance desktop application built with 
Java Swing. Track your income and expenses, set monthly budgets, and 
visualize your spending — all stored locally in JSON files. No database 
or internet connection required.

---

## Features

-  User authentication (Login & Register)
-  Dashboard with income/expense overview
-  Monthly breakdown with pie charts
-  Transaction list with search & filters
-  Budget tracker per category
-  Add income & expense transactions
-  Data stored in local JSON files
-  Modern dark UI with teal accent theme
-  Custom icon support via assets folder

---

## Screenshots

<img width="554" height="674" alt="image" src="https://github.com/user-attachments/assets/fdd0415f-7ac7-434f-8c68-f0511d0fca37" />
<img width="1215" height="793" alt="image" src="https://github.com/user-attachments/assets/4c469d92-aa05-4c65-97c3-f01e21fafad6" />
<img width="1221" height="812" alt="image" src="https://github.com/user-attachments/assets/67557178-2a9f-4d79-bd26-bf05761f800a" />
<img width="1226" height="817" alt="image" src="https://github.com/user-attachments/assets/2c476e49-643b-4dd0-aa26-d2087b753644" />
<img width="1236" height="812" alt="image" src="https://github.com/user-attachments/assets/71a89f75-8976-4cfa-869c-b45eefb92752" />
<img width="1227" height="816" alt="image" src="https://github.com/user-attachments/assets/31b7db56-28f1-4463-811e-6fd6a4e4a4fa" />


---

## Getting Started

### Requirements
- Java JDK 11 or higher
- Windows / Mac / Linux

### Run from source

**Windows:**
```cmd
for /r src %f in (*.java) do javac -cp src -d out "%f"
java -cp out com.expensetracker.App
```

**Mac / Linux:**
```bash
chmod +x run.sh
./run.sh
```

### Run as JAR
```cmd
java -jar ExpenseTracker.jar
```

---

## Project Structure
```plain text
ExpenseTracker/
├── assets/          # App icons and images
├── src/
│   └── com/expensetracker/
│       ├── model/   # User, Expense, Budget
│       ├── service/ # Business logic & JSON storage
│       ├── ui/      # All Swing UI panels
│       └── util/    # Config, Icons, JSON parser
├── out/             # Compiled classes
├── run.sh           # Linux/Mac build & run
└── run.bat          # Windows build & run
```
---

## Data Storage

All data is saved locally in JSON files at:

Files:
- `users.json` — user accounts
- `expenses.json` — all transactions
- `budgets.json` — monthly budgets

---

## Tech Stack

| Tech | Usage |
|---|---|
| Java 11+ | Core language |
| Java Swing | UI framework |
| JSON (custom parser) | Data storage |
| SHA-256 | Password hashing |

---


## License

No License

---

## Author

Made with by NayX
