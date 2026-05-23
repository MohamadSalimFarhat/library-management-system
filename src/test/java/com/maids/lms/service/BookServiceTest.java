package com.maids.lms.service;

import com.maids.lms.dto.BookDto;
import com.maids.lms.entity.Book;
import com.maids.lms.exception.BusinessException;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(2008)
                .isbn("9780132350884")
                .build();

        bookDto = BookDto.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(2008)
                .isbn("9780132350884")
                .build();
    }

    @Test
    @DisplayName("Should return all books successfully")
    void getAllBooks_ReturnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookDto> result = bookService.getAllBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).findAll();
    }

    @Test
    @DisplayName("Should return book by ID when found")
    void getBookById_WhenFound_ReturnsBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookDto result = bookService.getBookById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when book not found by ID")
    void getBookById_WhenNotFound_ThrowsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should add book successfully when ISBN is unique")
    void addBook_WithUniqueIsbn_ReturnsCreatedBook() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto result = bookService.addBook(bookDto);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getAuthor()).isEqualTo("Robert C. Martin");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when ISBN already exists")
    void addBook_WithDuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(bookDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISBN");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update book successfully")
    void updateBook_WhenExists_ReturnsUpdatedBook() {
        Book updated = Book.builder().id(1L).title("Updated Title").author("Author")
                .publicationYear(2023).isbn("9780132350884").build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(updated);

        BookDto dto = BookDto.builder().title("Updated Title").author("Author")
                .publicationYear(2023).isbn("9780132350884").build();

        BookDto result = bookService.updateBook(1L, dto);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent book")
    void updateBook_WhenNotFound_ThrowsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, bookDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete book successfully when exists")
    void deleteBook_WhenExists_DeletesSuccessfully() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        assertThatCode(() -> bookService.deleteBook(1L)).doesNotThrowAnyException();
        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent book")
    void deleteBook_WhenNotFound_ThrowsException() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).deleteById(any());
    }
}
