import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Coordinates library operations and persists records in local text files. */
public class Library {
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Book> books = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final Path dataDirectory = Paths.get("data");
    private final Path booksFile = dataDirectory.resolve("books.txt");
    private final Path studentsFile = dataDirectory.resolve("students.txt");
    private final Path attendanceFile = dataDirectory.resolve("attendance.txt");
    private final Path historyFile = dataDirectory.resolve("issue_return_history.txt");
    private final Map<String, Map<LocalDate, Boolean>> attendance = new LinkedHashMap<>();
    private final List<String> history = new ArrayList<>();

    public Library() {
        loadBooks();
        loadStudents();
        loadAttendance();
        loadHistory();
    }

    public boolean addBook(Book book) {
        if (book == null || findBookById(book.getBookId()) != null) return false;
        books.add(book);
        saveBooks();
        return true;
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books); }

    public Book findBookById(int id) {
        for (Book book : books) if (book.getBookId() == id) return book;
        return null;
    }

    public List<Book> searchBooksByTitle(String query) {
        List<Book> matches = new ArrayList<>();
        if (query == null) return matches;
        String normalizedQuery = query.trim().toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(normalizedQuery)) matches.add(book);
        }
        return matches;
    }

    public boolean issueBook(int id) {
        Book book = findBookById(id);
        if (book == null || !book.isAvailable()) return false;
        book.setAvailable(false);
        recordBookAction("ISSUED", book);
        saveBooks();
        saveHistory();
        return true;
    }

    public boolean returnBook(int id) {
        Book book = findBookById(id);
        if (book == null || book.isAvailable()) return false;
        book.setAvailable(true);
        recordBookAction("RETURNED", book);
        saveBooks();
        saveHistory();
        return true;
    }

    public boolean removeBook(int id) {
        Book book = findBookById(id);
        if (book == null || !book.isAvailable()) return false;
        books.remove(book);
        saveBooks();
        return true;
    }

    private void recordBookAction(String action, Book book) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        history.add(timestamp + "|" + action + "|" + book.getBookId() + "|" + encode(book.getTitle()));
    }

    public List<String[]> getHistory() {
        List<String[]> result = new ArrayList<>();
        for (String entry : history) {
            String[] parts = entry.split("\\|", 4);
            if (parts.length == 4) {
                result.add(new String[] {parts[0], parts[1], parts[2], decode(parts[3])});
            }
        }
        Collections.reverse(result);
        return result;
    }

    public Student findStudentById(String id) {
        if (id == null) return null;
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(id.trim())) return student;
        }
        return null;
    }

    public boolean addOrUpdateStudent(Student student) {
        if (student == null) return false;
        Student existing = findStudentById(student.getStudentId());
        if (existing == null) {
            students.add(student);
        } else {
            existing.setName(student.getName());
            existing.setConsecutiveAbsentDays(student.getConsecutiveAbsentDays());
        }
        saveStudents();
        return true;
    }

    public boolean updateStudent(String id, String name) {
        Student student = findStudentById(id);
        if (student == null) return false;
        student.setName(name);
        saveStudents();
        return true;
    }

    public boolean deleteStudent(String id) {
        Student student = findStudentById(id);
        if (student == null) return false;
        students.remove(student);
        attendance.remove(student.getStudentId());
        saveStudents();
        saveAttendance();
        return true;
    }

    public List<Student> getAllStudents() { return new ArrayList<>(students); }

    public List<Student> searchStudents(String query) {
        List<Student> result = new ArrayList<>();
        if (query == null) return result;
        String normalizedQuery = query.trim().toLowerCase();
        for (Student student : students) {
            if (student.getStudentId().toLowerCase().contains(normalizedQuery)
                    || student.getName().toLowerCase().contains(normalizedQuery)) {
                result.add(student);
            }
        }
        return result;
    }

    public List<Student> getStudentsAbsentMoreThan(int days) {
        List<Student> result = new ArrayList<>();
        for (Student student : students) {
            if (student.getConsecutiveAbsentDays() > days) result.add(student);
        }
        return result;
    }

    public boolean markAttendance(String id, LocalDate date, boolean present) {
        if (findStudentById(id) == null || date == null) return false;
        String normalizedId = findStudentById(id).getStudentId();
        attendance.computeIfAbsent(normalizedId, key -> new TreeMap<>()).put(date, present);
        recomputeConsecutiveAbsences(normalizedId);
        saveAttendance();
        saveStudents();
        return true;
    }

    private void recomputeConsecutiveAbsences(String id) {
        Map<LocalDate, Boolean> records = attendance.get(id);
        if (records == null) return;
        int streak = 0;
        // Iterate chronologically; a present record resets the consecutive-absence streak.
        for (Boolean present : records.values()) {
            if (Boolean.FALSE.equals(present)) streak++;
            else streak = 0;
        }
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(id)) {
                student.setConsecutiveAbsentDays(streak);
                break;
            }
        }
    }

    public int[] attendanceSummary(String id) {
        int presentCount = 0;
        int absentCount = 0;
        Map<LocalDate, Boolean> records = attendance.get(id);
        if (records != null) {
            for (Boolean present : records.values()) {
                if (Boolean.TRUE.equals(present)) presentCount++;
                else absentCount++;
            }
        }
        return new int[] {presentCount, absentCount, presentCount + absentCount};
    }

    public List<String[]> getAttendanceRows() {
        List<String[]> rows = new ArrayList<>();
        for (Student student : students) {
            Map<LocalDate, Boolean> records = attendance.get(student.getStudentId());
            if (records == null) continue;
            for (Map.Entry<LocalDate, Boolean> record : records.entrySet()) {
                rows.add(new String[] {student.getStudentId(), student.getName(),
                        record.getKey().toString(), record.getValue() ? "Present" : "Absent"});
            }
        }
        rows.sort(Comparator.comparing(row -> row[2]));
        return rows;
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            System.err.println("Warning: a stored text value could not be decoded; showing raw value.");
            return value;
        }
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.createDirectories(dataDirectory);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("ERROR: Could not save " + file + ". " + exception.getMessage());
        }
    }

    private void saveBooks() {
        List<String> lines = new ArrayList<>();
        for (Book book : books) {
            lines.add(book.getBookId() + "|" + encode(book.getTitle()) + "|"
                    + encode(book.getAuthor()) + "|" + book.isAvailable());
        }
        writeLines(booksFile, lines);
    }

    private void loadBooks() {
        if (!Files.exists(booksFile)) return;
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(booksFile, StandardCharsets.UTF_8)) {
                lineNumber++;
                String[] parts = line.split("\\|", -1);
                try {
                    if (parts.length != 4) throw new IllegalArgumentException("Expected four fields");
                    Book book = new Book(Integer.parseInt(parts[0]), decode(parts[1]), decode(parts[2]));
                    if (!parts[3].equalsIgnoreCase("true") && !parts[3].equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Invalid availability value");
                    }
                    book.setAvailable(Boolean.parseBoolean(parts[3]));
                    boolean duplicateId = false;
                    for (Book existingBook : books) {
                        if (existingBook.getBookId() == book.getBookId()) {
                            duplicateId = true;
                            break;
                        }
                    }
                    if (!duplicateId) books.add(book);
                    else System.err.println("Warning: duplicate book ID on data/books.txt line " + lineNumber + ". Skipped.");
                } catch (RuntimeException exception) {
                    System.err.println("Warning: invalid book record on data/books.txt line " + lineNumber + ". Skipped.");
                }
            }
        } catch (IOException exception) {
            System.err.println("ERROR: Could not load books: " + exception.getMessage());
        }
    }

    private void saveStudents() {
        List<String> lines = new ArrayList<>();
        for (Student student : students) {
            lines.add(student.getStudentId() + "|" + encode(student.getName()) + "|"
                    + student.getConsecutiveAbsentDays());
        }
        writeLines(studentsFile, lines);
    }

    private void loadStudents() {
        if (!Files.exists(studentsFile)) return;
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(studentsFile, StandardCharsets.UTF_8)) {
                lineNumber++;
                String[] parts = line.split("\\|", -1);
                try {
                    if (parts.length != 3) throw new IllegalArgumentException("Expected three fields");
                    Student student = new Student(parts[0], decode(parts[1]), Integer.parseInt(parts[2]));
                    boolean duplicateId = false;
                    for (Student existingStudent : students) {
                        if (existingStudent.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                            duplicateId = true;
                            break;
                        }
                    }
                    if (!duplicateId) students.add(student);
                    else System.err.println("Warning: duplicate student ID on data/students.txt line " + lineNumber + ". Skipped.");
                } catch (RuntimeException exception) {
                    System.err.println("Warning: invalid student record on data/students.txt line " + lineNumber + ". Skipped.");
                }
            }
        } catch (IOException exception) {
            System.err.println("ERROR: Could not load students: " + exception.getMessage());
        }
    }

    private void saveAttendance() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Map<LocalDate, Boolean>> studentRecord : attendance.entrySet()) {
            for (Map.Entry<LocalDate, Boolean> day : studentRecord.getValue().entrySet()) {
                lines.add(studentRecord.getKey() + "|" + day.getKey() + "|" + day.getValue());
            }
        }
        writeLines(attendanceFile, lines);
    }

    private void loadAttendance() {
        if (!Files.exists(attendanceFile)) return;
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(attendanceFile, StandardCharsets.UTF_8)) {
                lineNumber++;
                String[] parts = line.split("\\|", -1);
                try {
                    if (parts.length != 3) throw new IllegalArgumentException("Expected three fields");
                    String id = parts[0].trim().toUpperCase();
                    LocalDate date = LocalDate.parse(parts[1]);
                    if (!parts[2].equalsIgnoreCase("true") && !parts[2].equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Invalid attendance status");
                    }
                    boolean studentExists = false;
                    for (Student student : students) {
                        if (student.getStudentId().equalsIgnoreCase(id)) {
                            studentExists = true;
                            break;
                        }
                    }
                    if (studentExists) {
                        attendance.computeIfAbsent(id, key -> new TreeMap<>())
                                .put(date, Boolean.parseBoolean(parts[2]));
                    }
                } catch (RuntimeException exception) {
                    System.err.println("Warning: invalid attendance record on data/attendance.txt line " + lineNumber + ". Skipped.");
                }
            }
            for (String id : attendance.keySet()) recomputeConsecutiveAbsences(id);
        } catch (IOException exception) {
            System.err.println("ERROR: Could not load attendance: " + exception.getMessage());
        }
    }

    private void saveHistory() { writeLines(historyFile, new ArrayList<>(history)); }

    private void loadHistory() {
        if (!Files.exists(historyFile)) return;
        try {
            for (String line : Files.readAllLines(historyFile, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) history.add(line);
                else System.err.println("Warning: malformed issue/return history record skipped.");
            }
        } catch (IOException exception) {
            System.err.println("ERROR: Could not load issue/return history: " + exception.getMessage());
        }
    }
}
