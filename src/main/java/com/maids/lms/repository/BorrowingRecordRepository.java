package com.maids.lms.repository;

import com.maids.lms.entity.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    @Query("SELECT br FROM BorrowingRecord br WHERE br.book.id = :bookId AND br.patron.id = :patronId AND br.returnDate IS NULL")
    Optional<BorrowingRecord> findActiveRecord(Long bookId, Long patronId);

    @Query("SELECT COUNT(br) > 0 FROM BorrowingRecord br WHERE br.book.id = :bookId AND br.returnDate IS NULL")
    boolean isBookCurrentlyBorrowed(Long bookId);
}
