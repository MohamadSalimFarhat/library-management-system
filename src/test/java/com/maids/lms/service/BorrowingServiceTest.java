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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private PatronRepository patronRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private Book book;
    private Patron patron;
    private BorrowingRecord record;

    @BeforeEach
    void setUp() {
        book = Book.builder().id(1L).title("Clean Code").author("Martin")
                .publicationYear(2008).isbn("9780132350884").build();

        patron = Patron.builder().id(1L).name("John").email("john@test.com")
                .phoneNumber("+9611234567").build();

        record = BorrowingRecord.builder()
                .id(1L).book(book).patron(patron)
                .borrowDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Should borrow book successfully when available")
    void borrowBook_WhenAvailable_Succeeds() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRecordRepository.isBookCurrentlyBorrowed(1L)).thenReturn(false);
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(record);

        BorrowingRecordDto result = borrowingService.borrowBook(1L, 1L);

        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getPatronId()).isEqualTo(1L);
        assertThat(result.getBorrowDate()).isEqualTo(LocalDate.now());
        assertThat(result.getReturnDate()).isNull();
    }

    @Test
    @DisplayName("Should throw BusinessException when book already borrowed")
    void borrowBook_WhenAlreadyBorrowed_Throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRecordRepository.isBookCurrentlyBorrowed(1L)).thenReturn(true);

        assertThatThrownBy(() -> borrowingService.borrowBook(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("currently borrowed");

        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid book on borrow")
    void borrowBook_WhenBookNotFound_Throws() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for invalid patron on borrow")
    void borrowBook_WhenPatronNotFound_Throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.borrowBook(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Patron");
    }

    @Test
    @DisplayName("Should return book successfully when active record exists")
    void returnBook_WhenActiveRecordExists_Succeeds() {
        BorrowingRecord returned = BorrowingRecord.builder()
                .id(1L).book(book).patron(patron)
                .borrowDate(LocalDate.now().minusDays(5))
                .returnDate(LocalDate.now()).build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRecordRepository.findActiveRecord(1L, 1L)).thenReturn(Optional.of(record));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(returned);

        BorrowingRecordDto result = borrowingService.returnBook(1L, 1L);

        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should throw BusinessException when no active borrowing record on return")
    void returnBook_WhenNoActiveRecord_Throws() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRecordRepository.findActiveRecord(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowingService.returnBook(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No active borrowing record");
    }
}
