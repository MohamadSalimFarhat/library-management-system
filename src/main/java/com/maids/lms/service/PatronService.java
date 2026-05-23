package com.maids.lms.service;

import com.maids.lms.dto.PatronDto;
import com.maids.lms.entity.Patron;
import com.maids.lms.exception.BusinessException;
import com.maids.lms.exception.ResourceNotFoundException;
import com.maids.lms.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatronService {

    private final PatronRepository patronRepository;

    @Transactional(readOnly = true)
    public List<PatronDto> getAllPatrons() {
        return patronRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "patrons", key = "#id")
    public PatronDto getPatronById(Long id) {
        return patronRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + id));
    }

    @CacheEvict(value = "patrons", allEntries = true)
    public PatronDto addPatron(PatronDto patronDto) {
        if (patronRepository.existsByEmail(patronDto.getEmail())) {
            throw new BusinessException("A patron with email '" + patronDto.getEmail() + "' already exists");
        }
        Patron patron = toEntity(patronDto);
        patron.setMembershipId("MBR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return toDto(patronRepository.save(patron));
    }

    @CachePut(value = "patrons", key = "#id")
    public PatronDto updatePatron(Long id, PatronDto patronDto) {
        Patron existing = patronRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found with id: " + id));

        if (patronRepository.existsByEmailAndIdNot(patronDto.getEmail(), id)) {
            throw new BusinessException("Another patron with email '" + patronDto.getEmail() + "' already exists");
        }

        existing.setName(patronDto.getName());
        existing.setEmail(patronDto.getEmail());
        existing.setPhoneNumber(patronDto.getPhoneNumber());

        return toDto(patronRepository.save(existing));
    }

    @CacheEvict(value = "patrons", key = "#id")
    public void deletePatron(Long id) {
        if (!patronRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patron not found with id: " + id);
        }
        patronRepository.deleteById(id);
    }

    private PatronDto toDto(Patron patron) {
        return PatronDto.builder()
                .id(patron.getId())
                .name(patron.getName())
                .email(patron.getEmail())
                .phoneNumber(patron.getPhoneNumber())
                .membershipId(patron.getMembershipId())
                .build();
    }

    private Patron toEntity(PatronDto dto) {
        return Patron.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }
}
