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
public class DiscrepancyDTO {
    private Long auditEntryId;
    private Long assetId;
    private String assetTag;
    private String assetName;
    private AuditResult result;
    private String notes;
    private String auditorName;
}
