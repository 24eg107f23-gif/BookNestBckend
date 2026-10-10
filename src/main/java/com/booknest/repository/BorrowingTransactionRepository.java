package com.booknest.repository;

import com.booknest.entity.BorrowingTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Long> {
    List<BorrowingTransaction> findAllByStudentIdOrderByIssuedAtDesc(Long studentId);
    List<BorrowingTransaction> findAllByOrderByIssuedAtDesc();
    long countByReturnedAtIsNull();
    long countByReturnedAtIsNotNull();
}
