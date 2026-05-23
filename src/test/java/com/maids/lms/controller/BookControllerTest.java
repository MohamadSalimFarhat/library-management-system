package com.maids.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maids.lms.dto.BookDto;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(com.maids.lms.config.SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    // Required by SecurityConfig
    @MockBean
    private com.maids.lms.repository.UserRepository userRepository;
    @MockBean
    private com.maids.lms.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private com.maids.lms.security.JwtService jwtService;

    private BookDto bookDto;

    @BeforeEach
    void setUp() {
        bookDto = BookDto.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(2008)
                .isbn("9780132350884")
                .build();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("GET /api/books should return list of books")
    void getAllBooks_ReturnsBookList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(bookDto));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"))
                .andExpect(jsonPath("$[0].isbn").value("9780132350884"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("GET /api/books/{id} should return book when found")
    void getBookById_WhenFound_ReturnsBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookDto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("GET /api/books/{id} should return 404 when not found")
    void getBookById_WhenNotFound_Returns404() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("POST /api/books should create and return book")
    void addBook_WithValidData_Returns201() throws Exception {
        BookDto input = BookDto.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .publicationYear(2008)
                .isbn("9780132350884")
                .build();

        when(bookService.addBook(any(BookDto.class))).thenReturn(bookDto);

        mockMvc.perform(post("/api/books").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("POST /api/books should return 400 for invalid data")
    void addBook_WithInvalidData_Returns400() throws Exception {
        BookDto invalid = BookDto.builder().title("").build();

        mockMvc.perform(post("/api/books").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    @DisplayName("DELETE /api/books/{id} should return 204")
    void deleteBook_Returns204() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Unauthenticated request should return 401 or 403")
    void unauthenticatedRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().is4xxClientError());
    }
}
