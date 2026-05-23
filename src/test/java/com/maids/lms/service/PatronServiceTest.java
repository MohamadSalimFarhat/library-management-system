package com.maids.lms.service;

import com.maids.lms.dto.PatronDto;
import com.maids.lms.entity.Patron;
import com.maids.lms.exception.BusinessException;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.repository.PatronRepository;
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
class PatronServiceTest {

    @Mock
    private PatronRepository patronRepository;

    @InjectMocks
    private PatronService patronService;

    private Patron patron;
    private PatronDto patronDto;

    @BeforeEach
    void setUp() {
        patron = Patron.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+9611234567")
                .membershipId("MBR-ABC12345")
                .build();

        patronDto = PatronDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("+9611234567")
                .build();
    }

    @Test
    @DisplayName("Should return all patrons")
    void getAllPatrons_ReturnsAll() {
        when(patronRepository.findAll()).thenReturn(List.of(patron));

        List<PatronDto> result = patronService.getAllPatrons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return patron by ID")
    void getPatronById_WhenFound_ReturnsPatron() {
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));

        PatronDto result = patronService.getPatronById(1L);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should throw when patron not found")
    void getPatronById_WhenNotFound_Throws() {
        when(patronRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patronService.getPatronById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should add patron successfully")
    void addPatron_WithUniqueEmail_Succeeds() {
        when(patronRepository.existsByEmail(anyString())).thenReturn(false);
        when(patronRepository.save(any(Patron.class))).thenReturn(patron);

        PatronDto result = patronService.addPatron(patronDto);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(patronRepository).save(any(Patron.class));
    }

    @Test
    @DisplayName("Should throw when duplicate email on add")
    void addPatron_WithDuplicateEmail_Throws() {
        when(patronRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> patronService.addPatron(patronDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(patronRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete patron successfully")
    void deletePatron_WhenExists_Deletes() {
        when(patronRepository.existsById(1L)).thenReturn(true);
        doNothing().when(patronRepository).deleteById(1L);

        assertThatCode(() -> patronService.deletePatron(1L)).doesNotThrowAnyException();
        verify(patronRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent patron")
    void deletePatron_WhenNotFound_Throws() {
        when(patronRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> patronService.deletePatron(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
