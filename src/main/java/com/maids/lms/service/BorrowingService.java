package com.maids.lms.service;

import com.maids.lms.dto.BorrowingRecordDto;
import com.maids.lms.entity.Book;
import com.maids.lms.entity.BorrowingRecord;
import com.maids.lms.entity.Patron;
import com.maids.lms.exception.BusinessException;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.repository.BookRepository;
import com.maids.lms.repository.BorrowingRecordRepository;
import com.maids.lms.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;

    public BorrowingRecordDto borrowBook(Long bookId, Long patronId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + patronId));

        if (borrowingRecordRepository.isBookCurrentlyBorrowed(bookId)) {
            throw new BusinessException("Book '" + book.getTitle() + "' is currently borrowed by another patron");
        }

        BorrowingRecord record = BorrowingRecord.builder()
                .book(book)
                .patron(patron)
                .borrowDate(LocalDate.now())
                .build();

        return toDto(borrowingRecordRepository.save(record));
    }

    public BorrowingRecordDto returnBook(Long bookId, Long patronId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + patronId));

        BorrowingRecord record = borrowingRecordRepository.findActiveRecord(bookId, patronId)
                .orElseThrow(() -> new BusinessException(
                        "No active borrowing record found for book id: " + bookId + " and patron id: " + patronId));

        record.setReturnDate(LocalDate.now());
        return toDto(borrowingRecordRepository.save(record));
    }

    private BorrowingRecordDto toDto(BorrowingRecord record) {
        return BorrowingRecordDto.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .patronId(record.getPatron().getId())
                .patronName(record.getPatron().getName())
                .borrowDate(record.getBorrowDate())
                .returnDate(record.getReturnDate())
                .build();
    }
}
