# Java Library Management System

A menu-driven Java console application for managing books, students, issue/return activity, and daily attendance. The project demonstrates object-oriented programming, Java Collections, input validation, exception handling, and local text-file persistence.

## Objective

Build a small library administration program that keeps book and student records organized and allows an administrator to manage borrowing and attendance through a clear console menu.

## Features implemented

- Admin login with a three-attempt limit (educational demonstration only).
- Add, view, search, issue, return, and remove books.
- Duplicate book ID prevention and validation of positive IDs and non-empty titles/authors.
- Add or update student records, search by ID/name, update names, and delete student records.
- Record daily attendance by date using Present, Absent, or Skip options.
- Attendance register, attendance summary, percentage calculation, and consecutive-absence tracking.
- Timestamped issue/return history.
- Local persistence for books, students, attendance, and issue/return history.
- Warnings for malformed records and readable errors when files cannot be loaded or saved.

## Technologies used

- Java (JDK 8+ compatible source; tested with OpenJDK 21)
- Object-oriented programming: classes, constructors, encapsulation, methods
- Java Collections: `ArrayList`, `List`, `Map`, `LinkedHashMap`, and `TreeMap`
- File I/O: `java.nio.file` and UTF-8 text files
- Date handling: `LocalDate`, `LocalDateTime`, and `DateTimeFormatter`

No third-party libraries or database server are required.

## Project structure

```text
java-week1-assignment/
├── library_management_system/
│   ├── src/
│   │   ├── Main.java
│   │   ├── Book.java
│   │   ├── Student.java
│   │   └── Library.java
│   ├── data/
│   │   ├── books.txt
│   │   ├── students.txt
│   │   ├── attendance.txt
│   │   └── issue_return_history.txt
│   ├── screenshots/
│   ├── tests/
│   │   └── ModelValidationTest.java
│   ├── screenshots/
│   ├── README.md
│   ├── run.bat
│   └── run.sh
├── banking_application/                 # Add the separate assignment here if required
├── java_collections_challenge/           # Add the separate assignment here if required
├── screenshots/                          # Submission-level evidence
└── main_README.md
```

## Prerequisites

Install a Java Development Kit (JDK) and confirm Java is available:

```bash
java -version
javac -version
```

## Compile and run

Run commands from the `library_management_system` directory so the application finds the `data/` folder.

### Windows (Command Prompt or PowerShell)

```bat
javac -Xlint:all -d out src\*.java
java -cp out Main
```

Alternatively, double-click `run.bat` or run `./run.bat` from PowerShell (or use `run.bat` in Command Prompt).

### macOS / Linux

```bash
chmod +x run.sh
./run.sh
```

The program stores data relative to the current working directory. Run it from this project folder to ensure records are loaded from and saved to `data/`.

## Demo login

- Username: `admin`
- Password: `admin123`

**Security limitation:** credentials are hard-coded for this educational assignment. Do not use this login design for a real application or sensitive data.

## Sample execution

```text
========================================
       LIBRARY MANAGEMENT SYSTEM
========================================
--- Admin Login ---
Demo credentials: admin / admin123
Username: admin
Password: admin123
Login successful. Welcome, Admin!

=============== MAIN MENU ===============
 1. Add Book                    10. View All Students
 2. View Books                  11. Search Student
 3. Search Book                 12. Daily Attendance
 4. Issue Book                  13. Advanced Student Search
 5. Return Book                 14. Update Student Name
 6. Remove Book                 15. Delete Student
 7. Add/Update Student          16. Attendance Report
 8. Students Absent > 3 Days    17. Issue/Return History
 9. Exit
=========================================
Choose option: 4
Book ID to issue: 10
Book issued successfully.

Choose option: 5
Book ID to return: 10
Book returned successfully.

Choose option: 99
Invalid choice. Please select an option from 1 to 17.
```

The sample is illustrative. Book IDs available on a fresh run depend on the records in the included data files. For screenshots of actual execution, see `screenshots/`.

## Data files

- `data/books.txt`: book ID, Base64-encoded title, Base64-encoded author, and availability.
- `data/students.txt`: student ID, Base64-encoded name, and consecutive absence count.
- `data/attendance.txt`: student ID, date, and attendance boolean.
- `data/issue_return_history.txt`: timestamp, action, book ID, and Base64-encoded title.

The Base64 encoding is used to keep delimiters in text fields from breaking the simple record format; it is not encryption. Back up the `data/` folder before deleting or replacing it.

## Validation and error handling

- IDs must be positive integers for books; student IDs accept letters, digits, underscores, and hyphens.
- Blank titles, author names, and student names are rejected.
- Negative absence counts are rejected.
- Invalid numeric menu input is requested again; invalid menu choices are handled without terminating the application.
- Duplicate IDs and invalid issue/return operations are rejected.
- Invalid stored records are skipped with a warning instead of being silently ignored.
- File read/write failures are reported to standard error. Users should check these messages before assuming data was persisted.

## Validation test

A small no-dependency model validation test is included. Run it from this project directory after compiling the source:

```bash
javac -Xlint:all -d out src/*.java tests/ModelValidationTest.java
java -ea -cp out ModelValidationTest
```

Expected output: `All model validation checks passed.` This test checks invalid book IDs, blank book fields, negative absence counts, blank student names, and trimming/normalization of valid data.

## Screenshots

The `screenshots/` folder includes sample captures for the main menu, book operations, and invalid menu input. These are visual evidence for the documented flows; run the application locally to capture any additional reviewer-requested scenarios.

## Author

- **Author:** Suraj Upadhyay
- **Course:** Java Week 1 Assignment

## Known limitations

- The login credentials are hard-coded and intended only for demonstration.
- Text files are used instead of a database; simultaneous multi-user access is not supported.
- This is a console application and does not provide a graphical interface.
