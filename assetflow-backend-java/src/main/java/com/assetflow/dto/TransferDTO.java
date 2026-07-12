package com.assetflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDTO {
    private Long requestedByUserId;
    private Long newEmployeeId;
    private Long newDepartmentId;
    private Long approverId;
}
