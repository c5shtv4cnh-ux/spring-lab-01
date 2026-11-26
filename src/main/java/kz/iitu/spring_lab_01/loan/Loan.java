package kz.iitu.spring_lab_01.loan;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Lab 13, task 3: a book loan; the nightly job marks overdue ones. */
@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(name = "book_title", nullable = false, length = 200)
    private String bookTitle;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean returned;

    @Column(nullable = false)
    private boolean overdue;

    protected Loan() { }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
    public boolean isOverdue() { return overdue; }
}
