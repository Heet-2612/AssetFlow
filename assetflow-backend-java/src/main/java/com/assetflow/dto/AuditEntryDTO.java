package com.assetflow.dto;

import com.assetflow.model.AuditResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntryDTO {
    private Long auditCycleId;
    private Long assetId;
    private Long auditorId;
    private AuditResult result;
    private String notes;
}
