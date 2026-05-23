package com.maids.lms.controller;

import com.maids.lms.dto.BorrowingRecordDto;
import com.maids.lms.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping("/borrow/{bookId}/patron/{patronId}")
    public ResponseEntity<BorrowingRecordDto> borrowBook(
            @PathVariable Long bookId,
            @PathVariable Long patronId) {
        return ResponseEntity.ok(borrowingService.borrowBook(bookId, patronId));
    }

    @PutMapping("/return/{bookId}/patron/{patronId}")
    public ResponseEntity<BorrowingRecordDto> returnBook(
            @PathVariable Long bookId,
            @PathVariable Long patronId) {
        return ResponseEntity.ok(borrowingService.returnBook(bookId, patronId));
    }
}
