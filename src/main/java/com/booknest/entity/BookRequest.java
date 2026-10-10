package com.booknest.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "book_requests")
public class BookRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();

    protected BookRequest() {
    }

    public BookRequest(AppUser student, Book book) {
        this.student = student;
        this.book = book;
    }

    public void approve() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved or rejected.");
        }
        status = RequestStatus.APPROVED;
    }

    public void reject() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved or rejected.");
        }
        status = RequestStatus.REJECTED;
    }

    public Long getId() { return id; }
    public AppUser getStudent() { return student; }
    public Book getBook() { return book; }
    public RequestStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
}
