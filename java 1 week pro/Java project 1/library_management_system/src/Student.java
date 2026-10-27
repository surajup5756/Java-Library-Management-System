/** Represents a student and their current consecutive absence count. */
public class Student {
    private final String studentId;
    private String name;
    private int consecutiveAbsentDays;

    public Student(String studentId, String name, int consecutiveAbsentDays) {
        if (studentId == null || !studentId.trim().matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Student ID is required and may contain letters, numbers, underscores, or hyphens.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (consecutiveAbsentDays < 0) {
            throw new IllegalArgumentException("Absent days cannot be negative.");
        }
        this.studentId = studentId.trim().toUpperCase();
        this.name = name.trim();
        this.consecutiveAbsentDays = consecutiveAbsentDays;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public int getConsecutiveAbsentDays() { return consecutiveAbsentDays; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        this.name = name.trim();
    }

    public void setConsecutiveAbsentDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Absent days cannot be negative.");
        }
        this.consecutiveAbsentDays = days;
    }
}
