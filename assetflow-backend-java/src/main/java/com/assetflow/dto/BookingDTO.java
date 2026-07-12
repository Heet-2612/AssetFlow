package com.assetflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long assetId;
    private Long bookedById;
    private Long departmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
}
