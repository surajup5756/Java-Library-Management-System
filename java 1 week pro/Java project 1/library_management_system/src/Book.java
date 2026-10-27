/** Represents a book managed by the library. */
public class Book {
    private final int bookId;
    private String title;
    private String author;
    private boolean available;

    public Book(int bookId, String title, String author) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than zero.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required.");
        }
        this.bookId = bookId;
        this.title = title.trim();
        this.author = author.trim();
        this.available = true;
    }

    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required.");
        }
        this.title = title.trim();
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required.");
        }
        this.author = author.trim();
    }

    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("%-8d %-30s %-25s %-12s", bookId, title, author,
                available ? "Available" : "Issued");
    }
}
