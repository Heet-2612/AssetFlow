package com.assetflow.dto;

import com.assetflow.model.MaintenancePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceDTO {
    private Long assetId;
    private Long raisedById;
    private String issueDescription;
    private MaintenancePriority priority;
    private String photoUrl;
}
