package com.booknest.dto;

import com.booknest.entity.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record BookInput(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 140) String author,
            @NotBlank @Size(max = 100) String category,
            @NotBlank @Size(max = 20) String isbn,
            @Size(max = 5000) String description,
            @Min(0) int totalCopies) {
    }

    public record IssueInput(@NotNull Long studentId, @NotNull Long bookId, @Min(1) @Max(90) int loanDays) {
        public IssueInput {
            if (loanDays == 0) {
                loanDays = 14;
            }
        }
    }

    public record EnabledInput(@NotNull Boolean enabled) {
    }

    public record UserView(Long id, String fullName, String email, Role role, boolean enabled, Instant createdAt) {
        public static UserView from(AppUser user) {
            return new UserView(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                    user.isEnabled(), user.getCreatedAt());
        }
    }

    public record AuthResponse(String token, UserView user) {
    }

    public record BookView(Long id, String title, String author, String category, String isbn, String description,
                           int totalCopies, int availableCopies, Instant createdAt) {
        public static BookView from(Book book) {
            return new BookView(book.getId(), book.getTitle(), book.getAuthor(), book.getCategory(), book.getIsbn(),
                    book.getDescription(), book.getTotalCopies(), book.getAvailableCopies(), book.getCreatedAt());
        }
    }

    public record RequestView(Long id, UserView student, BookView book, RequestStatus status, Instant requestedAt) {
        public static RequestView from(BookRequest request) {
            return new RequestView(request.getId(), UserView.from(request.getStudent()),
                    BookView.from(request.getBook()), request.getStatus(), request.getRequestedAt());
        }
    }

    public record TransactionView(Long id, UserView student, BookView book, Instant issuedAt, LocalDate dueDate,
                                  Instant returnedAt, boolean overdue) {
        public static TransactionView from(BorrowingTransaction transaction) {
            return new TransactionView(transaction.getId(), UserView.from(transaction.getStudent()),
                    BookView.from(transaction.getBook()), transaction.getIssuedAt(), transaction.getDueDate(),
                    transaction.getReturnedAt(), transaction.isOverdue());
        }
    }

    public record DashboardView(long totalBooks, long availableCopies, long issuedBooks, long returnedBooks,
                                long overdueTransactions) {
    }

    public record ErrorResponse(String message, Instant timestamp) {
        public static ErrorResponse of(String message) {
            return new ErrorResponse(message, Instant.now());
        }
    }
}
