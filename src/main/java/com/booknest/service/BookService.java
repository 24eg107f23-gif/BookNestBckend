package com.booknest.service;

import com.booknest.dto.ApiDtos.BookInput;
import com.booknest.dto.ApiDtos.BookView;
import com.booknest.entity.Book;
import com.booknest.exception.NotFoundException;
import com.booknest.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    @Transactional(readOnly = true)
    public List<BookView> search(String title, String author, String category) {
        return books.search(clean(title), clean(author), clean(category)).stream().map(BookView::from).toList();
    }

    @Transactional(readOnly = true)
    public BookView get(Long id) {
        return BookView.from(findBook(id));
    }

    @Transactional(readOnly = true)
    public long countBooks() {
        return books.count();
    }

    @Transactional
    public BookView create(BookInput input) {
        if (books.existsByIsbnIgnoreCase(input.isbn().trim())) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }
        return BookView.from(books.save(new Book(input.title().trim(), input.author().trim(), input.category().trim(),
                input.isbn().trim(), input.description(), input.totalCopies())));
    }

    @Transactional
    public BookView update(Long id, BookInput input) {
        Book book = findBook(id);
        boolean duplicate = books.existsByIsbnIgnoreCase(input.isbn().trim())
                && !book.getIsbn().equalsIgnoreCase(input.isbn().trim());
        if (duplicate) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }
        book.updateDetails(input.title().trim(), input.author().trim(), input.category().trim(),
                input.isbn().trim(), input.description(), input.totalCopies());
        return BookView.from(book);
    }

    @Transactional
    public void delete(Long id) {
        books.delete(findBook(id));
    }

    public Book findBook(Long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("Book not found."));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
