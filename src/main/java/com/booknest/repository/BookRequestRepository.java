package com.booknest.repository;

import com.booknest.entity.BookRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {
    List<BookRequest> findAllByStudentIdOrderByRequestedAtDesc(Long studentId);
    List<BookRequest> findAllByOrderByRequestedAtDesc();
}
