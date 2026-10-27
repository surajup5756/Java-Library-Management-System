import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console entry point for the Library Management System. */
public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final Library LIBRARY = new Library();

    public static void main(String[] args) {
        printHeader();
        if (!login()) {
            System.out.println("Too many failed attempts. Exiting.");
            INPUT.close();
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInteger("Choose option: ");
            switch (choice) {
                case 1: addBook(); break;
                case 2: viewBooks(); break;
                case 3: searchBook(); break;
                case 4: issueBook(); break;
                case 5: returnBook(); break;
                case 6: removeBook(); break;
                case 7: addOrUpdateStudent(); break;
                case 8: printStudents(LIBRARY.getStudentsAbsentMoreThan(3), "Students Absent More Than 3 Consecutive Days"); break;
                case 9: running = false; System.out.println("Goodbye!"); break;
                case 10: printStudents(LIBRARY.getAllStudents(), "All Students"); break;
                case 11: searchStudent(); break;
                case 12: markDailyAttendance(); break;
                case 13: advancedStudentSearch(); break;
                case 14: updateStudent(); break;
                case 15: deleteStudent(); break;
                case 16: attendanceReport(); break;
                case 17: issueReturnHistory(); break;
                default: System.out.println("Invalid choice. Please select an option from 1 to 17.");
            }
            if (running) System.out.println();
        }
        INPUT.close();
    }

    private static void printHeader() {
        System.out.println("========================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM");
        System.out.println("========================================");
    }

    private static boolean login() {
        System.out.println("\n--- Admin Login ---");
        System.out.println("Demo credentials: admin / admin123");
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = readText("Username: ");
            String password = readText("Password: ");
            if ("admin".equals(username) && "admin123".equals(password)) {
                System.out.println("Login successful. Welcome, Admin!");
                return true;
            }
            System.out.println("Invalid credentials. Attempts remaining: " + (3 - attempt));
        }
        return false;
    }

    private static void printMenu() {
        System.out.println("\n=============== MAIN MENU ===============");
        System.out.println(" 1. Add Book                    10. View All Students");
        System.out.println(" 2. View Books                  11. Search Student");
        System.out.println(" 3. Search Book                 12. Daily Attendance");
        System.out.println(" 4. Issue Book                  13. Advanced Student Search");
        System.out.println(" 5. Return Book                 14. Update Student Name");
        System.out.println(" 6. Remove Book                 15. Delete Student");
        System.out.println(" 7. Add/Update Student          16. Attendance Report");
        System.out.println(" 8. Students Absent > 3 Days    17. Issue/Return History");
        System.out.println(" 9. Exit");
        System.out.println("=========================================");
    }

    private static void addBook() {
        int id = readPositiveInteger("Book ID: ");
        String title = readRequiredText("Book title: ");
        String author = readRequiredText("Author: ");
        try {
            boolean added = LIBRARY.addBook(new Book(id, title, author));
            System.out.println(added ? "Book added successfully." : "A book with this ID already exists.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Unable to add book: " + exception.getMessage());
        }
    }

    private static void viewBooks() {
        List<Book> books = LIBRARY.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("\n%-10s | %-30s | %-24s | %-12s%n", "Book ID", "Title", "Author", "Status");
        printDivider(86);
        for (Book book : books) printBook(book);
        printDivider(86);
        System.out.println("Total books: " + books.size());
    }

    private static void printBook(Book book) {
        System.out.printf("%-10d | %-30s | %-24s | %-12s%n",
                book.getBookId(), clip(book.getTitle(), 30), clip(book.getAuthor(), 24),
                book.isAvailable() ? "Available" : "Issued");
    }

    private static void searchBook() {
        System.out.println("1. Search by ID   2. Search by title");
        int choice = readInteger("Choose: ");
        if (choice == 1) {
            Book book = LIBRARY.findBookById(readPositiveInteger("Book ID: "));
            if (book == null) System.out.println("Book not found.");
            else { printDivider(86); printBook(book); printDivider(86); }
        } else if (choice == 2) {
            List<Book> matches = LIBRARY.searchBooksByTitle(readRequiredText("Title or part of title: "));
            if (matches.isEmpty()) System.out.println("No matching books found.");
            else for (Book book : matches) printBook(book);
        } else {
            System.out.println("Invalid search option.");
        }
    }

    private static void issueBook() {
        boolean issued = LIBRARY.issueBook(readPositiveInteger("Book ID to issue: "));
        System.out.println(issued ? "Book issued successfully." : "Book not found or already issued.");
    }

    private static void returnBook() {
        boolean returned = LIBRARY.returnBook(readPositiveInteger("Book ID to return: "));
        System.out.println(returned ? "Book returned successfully." : "Book not found or not currently issued.");
    }

    private static void removeBook() {
        boolean removed = LIBRARY.removeBook(readPositiveInteger("Book ID to remove: "));
        System.out.println(removed ? "Book removed successfully." : "Book not found or currently issued.");
    }

    private static void addOrUpdateStudent() {
        String id = readStudentId("Student ID (e.g. S101): ");
        String name = readRequiredText("Student name: ");
        int absentDays = readNonNegativeInteger("Current consecutive absent days: ");
        try {
            LIBRARY.addOrUpdateStudent(new Student(id, name, absentDays));
            System.out.println("Student saved successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Unable to save student: " + exception.getMessage());
        }
    }

    private static void printStudents(List<Student> students, String heading) {
        System.out.println("\n--- " + heading + " ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.printf("%-12s | %-30s | %-12s%n", "Student ID", "Student Name", "Absent Days");
        printDivider(60);
        for (Student student : students) {
            System.out.printf("%-12s | %-30s | %-12d%n", student.getStudentId(),
                    clip(student.getName(), 30), student.getConsecutiveAbsentDays());
        }
        printDivider(60);
        System.out.println("Total records: " + students.size());
    }

    private static void searchStudent() {
        List<Student> matches = LIBRARY.searchStudents(readRequiredText("Student ID or name: "));
        printStudents(matches, "Student Search Results");
        for (Student student : matches) printAttendanceSummary(student);
    }

    private static void advancedStudentSearch() {
        List<Student> matches = LIBRARY.searchStudents(readRequiredText("Enter student ID or name/part of name: "));
        printStudents(matches, "Advanced Student Search Results");
        for (Student student : matches) printAttendanceSummary(student);
    }

    private static void updateStudent() {
        String id = readStudentId("Student ID to update: ");
        if (LIBRARY.findStudentById(id) == null) {
            System.out.println("Student not found.");
            return;
        }
        String name = readRequiredText("New student name: ");
        try {
            System.out.println(LIBRARY.updateStudent(id, name)
                    ? "Student updated successfully." : "Student update failed.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Unable to update student: " + exception.getMessage());
        }
    }

    private static void deleteStudent() {
        String id = readStudentId("Student ID to delete: ");
        Student student = LIBRARY.findStudentById(id);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }
        if (!confirm("Delete " + student.getName() + " and their attendance records? (y/n): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        System.out.println(LIBRARY.deleteStudent(id) ? "Student deleted successfully." : "Student deletion failed.");
    }

    private static void markDailyAttendance() {
        List<Student> students = LIBRARY.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("Add students before recording attendance.");
            return;
        }
        LocalDate date;
        while (true) {
            String value = readText("Attendance date (YYYY-MM-DD, blank for today): ");
            try {
                date = value.isEmpty() ? LocalDate.now() : LocalDate.parse(value);
                break;
            } catch (DateTimeParseException exception) {
                System.out.println("Invalid date. Enter a date such as 2026-10-27.");
            }
        }
        printStudents(students, "Students");
        System.out.println("Enter P = Present, A = Absent, S = Skip.");
        for (Student student : students) {
            while (true) {
                String status = readText(student.getStudentId() + " - " + student.getName() + " [P/A/S]: ").toUpperCase();
                if ("S".equals(status)) break;
                if ("P".equals(status) || "A".equals(status)) {
                    LIBRARY.markAttendance(student.getStudentId(), date, "P".equals(status));
                    break;
                }
                System.out.println("Invalid status. Enter P, A, or S.");
            }
        }
        System.out.println("Attendance processing completed for " + date + ".");
    }

    private static void attendanceReport() {
        List<String[]> rows = LIBRARY.getAttendanceRows();
        if (rows.isEmpty()) {
            System.out.println("No daily attendance records yet. Use option 12 first.");
            return;
        }
        System.out.println("\nDAILY ATTENDANCE REGISTER");
        System.out.printf("%-12s | %-30s | %-12s | %-10s%n", "Student ID", "Student Name", "Date", "Status");
        printDivider(72);
        for (String[] row : rows) {
            System.out.printf("%-12s | %-30s | %-12s | %-10s%n",
                    row[0], clip(row[1], 30), row[2], row[3]);
        }
        printDivider(72);
        System.out.println("\nSTUDENT ATTENDANCE SUMMARY");
        System.out.printf("%-12s | %-30s | %7s | %7s | %5s | %12s%n",
                "Student ID", "Student Name", "Present", "Absent", "Days", "Attendance %");
        printDivider(88);
        for (Student student : LIBRARY.getAllStudents()) {
            int[] summary = LIBRARY.attendanceSummary(student.getStudentId());
            double percentage = summary[2] == 0 ? 0 : summary[0] * 100.0 / summary[2];
            System.out.printf("%-12s | %-30s | %7d | %7d | %5d | %11.1f%%%n",
                    student.getStudentId(), clip(student.getName(), 30), summary[0], summary[1], summary[2], percentage);
        }
        printDivider(88);
    }

    private static void printAttendanceSummary(Student student) {
        int[] summary = LIBRARY.attendanceSummary(student.getStudentId());
        double percentage = summary[2] == 0 ? 0 : summary[0] * 100.0 / summary[2];
        System.out.printf("Attendance: Present=%d, Absent=%d, Recorded days=%d, Percentage=%.1f%%%n",
                summary[0], summary[1], summary[2], percentage);
    }

    private static void issueReturnHistory() {
        List<String[]> records = LIBRARY.getHistory();
        System.out.println("\nISSUE / RETURN HISTORY");
        if (records.isEmpty()) {
            System.out.println("No issue/return activity recorded yet.");
            return;
        }
        System.out.printf("%-20s | %-10s | %-8s | %-30s%n", "Date & Time", "Action", "Book ID", "Book Title");
        printDivider(76);
        for (String[] record : records) {
            System.out.printf("%-20s | %-10s | %-8s | %-30s%n",
                    record[0], record[1], record[2], clip(record[3], 30));
        }
        printDivider(76);
    }

    private static String readStudentId(String prompt) {
        while (true) {
            String id = readRequiredText(prompt).toUpperCase();
            if (id.matches("[A-Z0-9_-]+")) return id;
            System.out.println("Use letters, numbers, underscores, or hyphens for the student ID.");
        }
    }

    private static int readInteger(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private static int readPositiveInteger(String prompt) {
        while (true) {
            int value = readInteger(prompt);
            if (value > 0) return value;
            System.out.println("Enter a number greater than zero.");
        }
    }

    private static int readNonNegativeInteger(String prompt) {
        while (true) {
            int value = readInteger(prompt);
            if (value >= 0) return value;
            System.out.println("Enter zero or a positive number.");
        }
    }

    private static String readRequiredText(String prompt) {
        while (true) {
            String value = readText(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("This field is required and cannot be blank.");
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return INPUT.nextLine().trim();
    }

    private static boolean confirm(String prompt) {
        return "y".equalsIgnoreCase(readText(prompt));
    }

    private static String clip(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value == null ? "" : value;
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static void printDivider(int length) {
        for (int index = 0; index < length; index++) System.out.print("-");
        System.out.println();
    }
}
