package com.booknest.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "borrowing_transactions")
public class BorrowingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true)
    private BookRequest request;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private Instant returnedAt;

    protected BorrowingTransaction() {
    }

    public BorrowingTransaction(AppUser student, Book book, BookRequest request, int loanDays) {
        this.student = student;
        this.book = book;
        this.request = request;
        this.dueDate = LocalDate.now().plusDays(loanDays);
    }

    public void markReturned() {
        if (returnedAt != null) {
            throw new IllegalStateException("This book has already been returned.");
        }
        returnedAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getStudent() { return student; }
    public Book getBook() { return book; }
    public Instant getIssuedAt() { return issuedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public Instant getReturnedAt() { return returnedAt; }
    public boolean isOverdue() { return returnedAt == null && dueDate.isBefore(LocalDate.now()); }
}
