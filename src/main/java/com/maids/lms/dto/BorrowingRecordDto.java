package com.maids.lms.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BorrowingRecordDto {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long patronId;
    private String patronName;
    private LocalDate borrowDate;
    private LocalDate returnDate;
}
