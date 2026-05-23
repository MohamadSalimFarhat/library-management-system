package com.maids.lms.controller;

import com.maids.lms.dto.PatronDto;
import com.maids.lms.service.PatronService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patrons")
@RequiredArgsConstructor
public class PatronController {

    private final PatronService patronService;

    @GetMapping
    public ResponseEntity<List<PatronDto>> getAllPatrons() {
        return ResponseEntity.ok(patronService.getAllPatrons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatronDto> getPatronById(@PathVariable Long id) {
        return ResponseEntity.ok(patronService.getPatronById(id));
    }

    @PostMapping
    public ResponseEntity<PatronDto> addPatron(@Valid @RequestBody PatronDto patronDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patronService.addPatron(patronDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatronDto> updatePatron(@PathVariable Long id, @Valid @RequestBody PatronDto patronDto) {
        return ResponseEntity.ok(patronService.updatePatron(id, patronDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatron(@PathVariable Long id) {
        patronService.deletePatron(id);
        return ResponseEntity.noContent().build();
    }
}
