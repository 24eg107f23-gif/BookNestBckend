package com.booknest.repository;

import com.booknest.entity.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbnIgnoreCase(String isbn);

    @Query("""
            select b from Book b
            where (:title = '' or lower(b.title) like lower(concat('%', :title, '%')))
              and (:author = '' or lower(b.author) like lower(concat('%', :author, '%')))
              and (:category = '' or lower(b.category) like lower(concat('%', :category, '%')))
            order by b.title
            """)
    List<Book> search(@Param("title") String title, @Param("author") String author, @Param("category") String category);
}
