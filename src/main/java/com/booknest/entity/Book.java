package com.booknest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "books", uniqueConstraints = @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn"))
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @NotBlank
    @Size(max = 140)
    @Column(nullable = false, length = 140)
    private String author;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String category;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Min(0)
    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Book() {
    }

    public Book(String title, String author, String category, String isbn, String description, int totalCopies) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public void updateDetails(String title, String author, String category, String isbn, String description, int totalCopies) {
        int issuedCopies = this.totalCopies - this.availableCopies;
        if (totalCopies < issuedCopies) {
            throw new IllegalArgumentException("Total copies cannot be less than the number currently issued.");
        }
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies - issuedCopies;
    }

    public void issueCopy() {
        if (availableCopies < 1) {
            throw new IllegalStateException("No copies of this book are currently available.");
        }
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("All copies are already marked available.");
        }
        availableCopies++;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public Instant getCreatedAt() { return createdAt; }
}
