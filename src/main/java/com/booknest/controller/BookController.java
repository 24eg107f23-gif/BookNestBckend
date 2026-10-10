package com.booknest.controller;

import com.booknest.dto.ApiDtos.BookInput;
import com.booknest.dto.ApiDtos.BookView;
import com.booknest.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    @GetMapping
    public List<BookView> search(@RequestParam(defaultValue = "") String title,
                                 @RequestParam(defaultValue = "") String author,
                                 @RequestParam(defaultValue = "") String category) {
        return books.search(title, author, category);
    }

    @GetMapping("/{id}")
    public BookView get(@PathVariable Long id) {
        return books.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIBRARIAN')")
    public BookView create(@Valid @RequestBody BookInput input) {
        return books.create(input);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public BookView update(@PathVariable Long id, @Valid @RequestBody BookInput input) {
        return books.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void delete(@PathVariable Long id) {
        books.delete(id);
    }
}
