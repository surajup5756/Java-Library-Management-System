/** Lightweight no-dependency checks for model validation. Run with assertions enabled. */
public class ModelValidationTest {
    public static void main(String[] args) {
        expectIllegalArgument(() -> new Book(0, "Title", "Author"), "non-positive book ID");
        expectIllegalArgument(() -> new Book(1, "  ", "Author"), "blank book title");
        expectIllegalArgument(() -> new Book(1, "Title", "  "), "blank author");
        expectIllegalArgument(() -> new Student("S1", "Student", -1), "negative absence count");
        expectIllegalArgument(() -> new Student("S1", "  ", 0), "blank student name");

        Book book = new Book(1, " Clean Code ", " Robert Martin ");
        assert "Clean Code".equals(book.getTitle());
        assert "Robert Martin".equals(book.getAuthor());
        Student student = new Student("s101", " Student Name ", 0);
        assert "S101".equals(student.getStudentId());
        assert "Student Name".equals(student.getName());
        System.out.println("All model validation checks passed.");
    }

    private static void expectIllegalArgument(Runnable action, String scenario) {
        try {
            action.run();
            throw new AssertionError("Expected IllegalArgumentException for " + scenario);
        } catch (IllegalArgumentException expected) {
            // Expected validation result.
        }
    }
}
