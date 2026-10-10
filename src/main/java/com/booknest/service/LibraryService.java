package com.booknest.service;

import com.booknest.dto.ApiDtos.*;
import com.booknest.entity.*;
import com.booknest.exception.NotFoundException;
import com.booknest.repository.BookRequestRepository;
import com.booknest.repository.BorrowingTransactionRepository;
import com.booknest.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {
    private static final int DEFAULT_LOAN_DAYS = 14;
    private final UserRepository users;
    private final BookService bookService;
    private final BookRequestRepository requests;
    private final BorrowingTransactionRepository transactions;

    public LibraryService(UserRepository users, BookService bookService, BookRequestRepository requests,
                          BorrowingTransactionRepository transactions) {
        this.users = users;
        this.bookService = bookService;
        this.requests = requests;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public List<RequestView> listRequests(String email, boolean librarian) {
        List<BookRequest> result = librarian ? requests.findAllByOrderByRequestedAtDesc()
                : requests.findAllByStudentIdOrderByRequestedAtDesc(currentUser(email).getId());
        return result.stream().map(RequestView::from).toList();
    }

    @Transactional
    public RequestView createRequest(String email, Long bookId) {
        AppUser student = currentUser(email);
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student accounts can request books.");
        }
        Book book = bookService.findBook(bookId);
        return RequestView.from(requests.save(new BookRequest(student, book)));
    }

    @Transactional
    public TransactionView approveRequest(Long requestId) {
        BookRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Book request not found."));
        if (!request.getStudent().isEnabled()) {
            throw new IllegalArgumentException("This student's account is disabled.");
        }
        request.approve();
        Book book = bookService.findBook(request.getBook().getId());
        book.issueCopy();
        return TransactionView.from(transactions.save(
                new BorrowingTransaction(request.getStudent(), book, request, DEFAULT_LOAN_DAYS)));
    }

    @Transactional
    public RequestView rejectRequest(Long requestId) {
        BookRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Book request not found."));
        request.reject();
        return RequestView.from(request);
    }

    @Transactional(readOnly = true)
    public List<TransactionView> listTransactions(String email, boolean librarian) {
        List<BorrowingTransaction> result = librarian ? transactions.findAllByOrderByIssuedAtDesc()
                : transactions.findAllByStudentIdOrderByIssuedAtDesc(currentUser(email).getId());
        return result.stream().map(TransactionView::from).toList();
    }

    @Transactional
    public TransactionView issue(IssueInput input) {
        AppUser student = users.findById(input.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found."));
        if (student.getRole() != Role.STUDENT || !student.isEnabled()) {
            throw new IllegalArgumentException("The selected account is not an enabled student.");
        }
        Book book = bookService.findBook(input.bookId());
        book.issueCopy();
        int loanDays = input.loanDays() == 0 ? DEFAULT_LOAN_DAYS : input.loanDays();
        return TransactionView.from(transactions.save(new BorrowingTransaction(student, book, null, loanDays)));
    }

    @Transactional
    public TransactionView returnBook(Long transactionId) {
        BorrowingTransaction transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Borrowing transaction not found."));
        transaction.markReturned();
        bookService.findBook(transaction.getBook().getId()).returnCopy();
        return TransactionView.from(transaction);
    }

    @Transactional(readOnly = true)
    public List<UserView> listStudents() {
        return users.findAllByRoleOrderByFullNameAsc(Role.STUDENT).stream().map(UserView::from).toList();
    }

    @Transactional
    public UserView setStudentEnabled(Long id, boolean enabled) {
        AppUser student = users.findById(id).orElseThrow(() -> new NotFoundException("Student not found."));
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student accounts can be managed here.");
        }
        student.setEnabled(enabled);
        return UserView.from(student);
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard() {
        List<BookView> allBooks = bookService.search("", "", "");
        long overdue = transactions.findAllByOrderByIssuedAtDesc().stream()
                .filter(BorrowingTransaction::isOverdue).count();
        return new DashboardView(bookService.countBooks(),
                allBooks.stream().mapToLong(BookView::availableCopies).sum(),
                transactions.countByReturnedAtIsNull(), transactions.countByReturnedAtIsNotNull(), overdue);
    }

    private AppUser currentUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Account not found."));
    }
}
