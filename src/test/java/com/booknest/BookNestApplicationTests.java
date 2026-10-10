package com.booknest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.booknest.config.JwtService;
import com.booknest.dto.ApiDtos.IssueInput;
import com.booknest.dto.ApiDtos.LoginRequest;
import com.booknest.dto.ApiDtos.RegisterRequest;
import com.booknest.entity.AppUser;
import com.booknest.entity.Book;
import com.booknest.entity.Role;
import com.booknest.repository.BookRepository;
import com.booknest.repository.UserRepository;
import com.booknest.service.AuthService;
import com.booknest.service.LibraryService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookNestApplicationTests {
    @Autowired private BookRepository books;
    @Autowired private UserRepository users;
    @Autowired private LibraryService library;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthService authService;
    @Autowired private JwtService jwtService;
    @Autowired private MockMvc mockMvc;

    @Test
    void issuingAndReturningBookUpdatesInventoryExactlyOnce() {
        Book book = books.save(testBook("Test Book", 1));
        AppUser student = users.save(testStudent());

        var loan = library.issue(new IssueInput(student.getId(), book.getId(), 14));
        assertThat(books.findById(book.getId()).orElseThrow().getAvailableCopies()).isZero();

        library.returnBook(loan.id());
        assertThat(books.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
        assertThatThrownBy(() -> library.returnBook(loan.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been returned");
    }

    @Test
    void cannotIssueWhenNoCopiesAreAvailable() {
        Book book = books.save(testBook("Unavailable Book", 0));
        AppUser student = users.save(testStudent());

        assertThatThrownBy(() -> library.issue(new IssueInput(student.getId(), book.getId(), 14)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No copies");
    }

    @Test
    void registrationCreatesStudentAndStoresOnlyHashedPassword() {
        String email = uniqueEmail();
        var response = authService.register(new RegisterRequest("New Reader", email, "safe-test-password"));

        AppUser account = users.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(account.getRole()).isEqualTo(Role.STUDENT);
        assertThat(account.getPasswordHash()).isNotEqualTo("safe-test-password");
        assertThat(passwordEncoder.matches("safe-test-password", account.getPasswordHash())).isTrue();
        assertThat(response.token()).isNotBlank();
        assertThat(authService.login(new LoginRequest(email, "safe-test-password")).user().email()).isEqualTo(email);
    }

    @Test
    void studentCannotCreateBooksButLibrarianCan() throws Exception {
        String isbn = testIsbn();
        String body = """
                {"title":"Authorized Book","author":"Test Author","category":"Testing",
                 "isbn":"%s","description":"Role authorization test","totalCopies":1}
                """.formatted(isbn);
        AppUser student = users.save(testStudent());
        AppUser librarian = users.save(new AppUser("Test Librarian", uniqueEmail(),
                passwordEncoder.encode("safe-test-password"), Role.LIBRARIAN));

        mockMvc.perform(post("/api/books")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(student))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/books")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(librarian))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void disabledStudentCannotUsePreviouslyIssuedToken() throws Exception {
        AppUser student = users.save(testStudent());
        String token = jwtService.generateToken(student);
        student.setEnabled(false);
        users.saveAndFlush(student);

        mockMvc.perform(get("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void approvalIssuesOneCopyAndCannotBeRepeated() {
        Book book = books.save(testBook("Requested Book", 1));
        AppUser student = users.save(testStudent());
        var request = library.createRequest(student.getEmail(), book.getId());

        var loan = library.approveRequest(request.id());

        assertThat(loan.book().availableCopies()).isZero();
        assertThatThrownBy(() -> library.approveRequest(request.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pending requests");
    }

    private Book testBook(String title, int copies) {
        return new Book(title, "Test Author", "Testing", testIsbn(), "Test record.", copies);
    }

    private String testIsbn() {
        return "9780000" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private AppUser testStudent() {
        return new AppUser("Test Student", uniqueEmail(), passwordEncoder.encode("safe-test-password"), Role.STUDENT);
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.test";
    }
}
