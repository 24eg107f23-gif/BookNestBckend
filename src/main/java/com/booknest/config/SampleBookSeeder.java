package com.booknest.config;

import com.booknest.entity.Book;
import com.booknest.repository.BookRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!mysql")
public class SampleBookSeeder implements ApplicationRunner {
    private final BookRepository books;

    public SampleBookSeeder(BookRepository books) {
        this.books = books;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (books.count() != 0) {
            return;
        }
        books.saveAll(List.of(
                new Book("The Midnight Library", "M. K. Rowan", "Fiction", "9780000000101",
                        "A reflective story about choices, possibility, and second chances.", 4),
                new Book("Practical Java", "A. R. Morgan", "Technology", "9780000000102",
                        "A fictional beginner-friendly guide to object-oriented programming.", 3),
                new Book("Gardens in the City", "N. L. Harper", "Nature", "9780000000103",
                        "An illustrated fictional guide to creating small urban gardens.", 2),
                new Book("The Quiet Atlas", "S. J. Ellis", "Travel", "9780000000104",
                        "A fictional collection of thoughtful journeys and places.", 5),
                new Book("Everyday Astronomy", "T. W. Brooks", "Science", "9780000000105",
                        "A fictional introduction to stars, planets, and observing the night sky.", 3)));
    }
}
