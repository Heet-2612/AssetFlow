package com.assetflow.service;

import com.assetflow.dto.AllocationDTO;
import com.assetflow.dto.ReturnDTO;
import com.assetflow.dto.TransferDTO;
import com.assetflow.exception.AssetAlreadyAllocatedException;
import com.assetflow.exception.InvalidStatusTransitionException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.model.*;
import com.assetflow.repository.AllocationRepository;
import com.assetflow.repository.AssetRepository;
import com.assetflow.repository.DepartmentRepository;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetService assetService;

    /**
     * Allocates an asset to an employee and/or department.
     * Conflict rule: an asset already ACTIVE-allocated cannot be allocated again -
     * the caller is told who currently holds it and should use transferAsset() instead.
     */
    public Allocation allocateAsset(AllocationDTO dto) {
        Asset asset = getAssetOrThrow(dto.getAssetId());

        allocationRepository.findByAssetIdAndStatus(asset.getId(), AllocationStatus.ACTIVE)
                .ifPresent(existing -> {
                    String holderName = existing.getEmployee() != null
                            ? existing.getEmployee().getName()
                            : (existing.getDepartment() != null ? existing.getDepartment().getName() : "another holder");
                    throw new AssetAlreadyAllocatedException(
                            "Asset " + asset.getAssetTag() + " is currently held by " + holderName
                                    + ". Use a transfer request instead.",
                            holderName,
                            existing.getId());
                });

        assetService.assertAllocatable(asset);

        User employee = dto.getEmployeeId() != null ? getUserOrThrow(dto.getEmployeeId()) : null;
        Department department = dto.getDepartmentId() != null ? getDepartmentOrThrow(dto.getDepartmentId()) : null;

        if (employee == null && department == null) {
            throw new IllegalArgumentException("Allocation requires either an employeeId or a departmentId");
        }

        Allocation allocation = Allocation.builder()
                .asset(asset)
                .employee(employee)
                .department(department)
                .allocatedDate(LocalDate.now())
                .expectedReturnDate(dto.getExpectedReturnDate())
                .status(AllocationStatus.ACTIVE)
                .build();

        allocation = allocationRepository.save(allocation);
        assetService.updateAssetStatus(asset.getId(), AssetStatus.ALLOCATED);

        return allocation;
    }

    /**
     * Marks an allocation as returned, captures condition check-in notes,
     * and reverts the asset status back to AVAILABLE.
     */
    public Allocation returnAsset(Long allocationId, ReturnDTO dto) {
        Allocation allocation = getAllocationOrThrow(allocationId);

        if (allocation.getStatus() == AllocationStatus.RETURNED) {
            throw new InvalidStatusTransitionException("Allocation " + allocationId + " has already been returned");
        }

        allocation.setStatus(AllocationStatus.RETURNED);
        allocation.setActualReturnDate(LocalDate.now());
        String notes = dto != null ? dto.getConditionNotes() : null;
        allocation.setReturnConditionNotes(notes);
        if (dto != null && dto.getConditionOnReturn() != null) {
            allocation.getAsset().setCondition(dto.getConditionOnReturn());
        }

        allocation = allocationRepository.save(allocation);
        assetService.updateAssetStatus(allocation.getAsset().getId(), AssetStatus.AVAILABLE);

        return allocation;
    }

    /**
     * Transfer workflow: Requested -> Approved (by Asset Manager/Department Head) -> Re-allocated.
     * This single method performs the approve+re-allocate step; requestedByUserId is retained
     * for audit/notification purposes, approverId is who authorized the transfer.
     */
    public Allocation transferAsset(Long allocationId, TransferDTO dto) {
        Allocation currentAllocation = getAllocationOrThrow(allocationId);

        if (currentAllocation.getStatus() != AllocationStatus.ACTIVE
                && currentAllocation.getStatus() != AllocationStatus.TRANSFER_REQUESTED) {
            throw new InvalidStatusTransitionException(
                    "Only an ACTIVE or TRANSFER_REQUESTED allocation can be transferred (current status: "
                            + currentAllocation.getStatus() + ")");
        }

        User approver = dto.getApproverId() != null ? getUserOrThrow(dto.getApproverId()) : null;
        if (approver != null && approver.getRole() != UserRole.ASSET_MANAGER
                && approver.getRole() != UserRole.DEPARTMENT_HEAD && approver.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Transfers may only be approved by an Asset Manager, Department Head, or Admin");
        }

        // Close out the old allocation record.
        currentAllocation.setStatus(AllocationStatus.TRANSFERRED);
        currentAllocation.setActualReturnDate(LocalDate.now());
        currentAllocation.setTransferApprovedBy(approver);
        allocationRepository.save(currentAllocation);

        User newEmployee = dto.getNewEmployeeId() != null ? getUserOrThrow(dto.getNewEmployeeId()) : null;
        Department newDepartment = dto.getNewDepartmentId() != null ? getDepartmentOrThrow(dto.getNewDepartmentId()) : null;

        if (newEmployee == null && newDepartment == null) {
            throw new IllegalArgumentException("Transfer requires either a newEmployeeId or a newDepartmentId");
        }

        Allocation newAllocation = Allocation.builder()
                .asset(currentAllocation.getAsset())
                .employee(newEmployee)
                .department(newDepartment)
                .allocatedDate(LocalDate.now())
                .status(AllocationStatus.ACTIVE)
                .transferApprovedBy(approver)
                .build();

        // Asset stays ALLOCATED throughout the transfer (status doesn't need to flip through AVAILABLE).
        return allocationRepository.save(newAllocation);
    }

    /** Marks an allocation as TRANSFER_REQUESTED - the "Transfer Request" button flow. */
    public Allocation requestTransfer(Long allocationId, Long requestedByUserId) {
        Allocation allocation = getAllocationOrThrow(allocationId);
        if (allocation.getStatus() != AllocationStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only an ACTIVE allocation can have a transfer requested");
        }
        allocation.setStatus(AllocationStatus.TRANSFER_REQUESTED);
        allocation.setTransferRequestedAt(java.time.LocalDateTime.now());
        if (requestedByUserId != null) {
            allocation.setTransferRequestedTo(getUserOrThrow(requestedByUserId));
        }
        return allocationRepository.save(allocation);
    }

    @Transactional(readOnly = true)
    public List<Allocation> getOverdueAllocations() {
        return allocationRepository.findByStatusAndExpectedReturnDateBefore(AllocationStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Allocation> getAllocationsForEmployee(Long employeeId) {
        return allocationRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Allocation> getAllocationHistoryForAsset(Long assetId) {
        return allocationRepository.findByAssetId(assetId);
    }

    @Transactional(readOnly = true)
    public List<Allocation> getAllocationsForDepartment(Long departmentId) {
        return allocationRepository.findByDepartmentId(departmentId);
    }

    private Allocation getAllocationOrThrow(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Allocation", id));
    }

    private Asset getAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Asset", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }
}
