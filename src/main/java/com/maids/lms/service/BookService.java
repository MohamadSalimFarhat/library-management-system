package com.maids.lms.service;

import com.maids.lms.dto.BookDto;
import com.maids.lms.entity.Book;
import com.maids.lms.exception.BusinessException;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id")
    public BookDto getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @CacheEvict(value = "books", allEntries = true)
    public BookDto addBook(BookDto bookDto) {
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            throw new BusinessException("A book with ISBN '" + bookDto.getIsbn() + "' already exists");
        }
        Book book = toEntity(bookDto);
        return toDto(bookRepository.save(book));
    }

    @CachePut(value = "books", key = "#id")
    public BookDto updateBook(Long id, BookDto bookDto) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (bookRepository.existsByIsbnAndIdNot(bookDto.getIsbn(), id)) {
            throw new BusinessException("Another book with ISBN '" + bookDto.getIsbn() + "' already exists");
        }

        existing.setTitle(bookDto.getTitle());
        existing.setAuthor(bookDto.getAuthor());
        existing.setPublicationYear(bookDto.getPublicationYear());
        existing.setIsbn(bookDto.getIsbn());

        return toDto(bookRepository.save(existing));
    }

    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private BookDto toDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(book.getPublicationYear())
                .isbn(book.getIsbn())
                .build();
    }

    private Book toEntity(BookDto dto) {
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publicationYear(dto.getPublicationYear())
                .isbn(dto.getIsbn())
                .build();
    }
}
