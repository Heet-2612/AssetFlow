package com.assetflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCycleDTO {
    private String name;
    private Long scopeDepartmentId;
    private String scopeLocation;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> auditorIds;
}
