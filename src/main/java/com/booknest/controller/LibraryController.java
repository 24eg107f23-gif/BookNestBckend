package com.booknest.controller;

import com.booknest.dto.ApiDtos.*;
import com.booknest.service.LibraryService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LibraryController {
    private final LibraryService library;

    public LibraryController(LibraryService library) {
        this.library = library;
    }

    @GetMapping("/requests")
    public List<RequestView> requests(Principal principal,
                                     @RequestParam(defaultValue = "false") boolean all) {
        return library.listRequests(principal.getName(), all && isLibrarian(principal));
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public RequestView request(@RequestParam Long bookId, Principal principal) {
        return library.createRequest(principal.getName(), bookId);
    }

    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public TransactionView approve(@PathVariable Long id) {
        return library.approveRequest(id);
    }

    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public RequestView reject(@PathVariable Long id) {
        return library.rejectRequest(id);
    }

    @GetMapping("/transactions")
    public List<TransactionView> transactions(Principal principal) {
        return library.listTransactions(principal.getName(), isLibrarian(principal));
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN')")
    public TransactionView issue(@Valid @RequestBody IssueInput input) {
        return library.issue(input);
    }

    @PostMapping("/transactions/{id}/return")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public TransactionView returnBook(@PathVariable Long id) {
        return library.returnBook(id);
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public List<UserView> students() {
        return library.listStudents();
    }

    @PatchMapping("/students/{id}/enabled")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public UserView setStudentEnabled(@PathVariable Long id, @Valid @RequestBody EnabledInput input) {
        return library.setStudentEnabled(id, input.enabled());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public DashboardView dashboard() {
        return library.dashboard();
    }

    private boolean isLibrarian(Principal principal) {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_LIBRARIAN"));
    }
}
