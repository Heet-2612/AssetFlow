package com.assetflow.service;

import com.assetflow.dto.MaintenanceDTO;
import com.assetflow.exception.InvalidStatusTransitionException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.exception.UnauthorizedActionException;
import com.assetflow.model.*;
import com.assetflow.repository.AssetRepository;
import com.assetflow.repository.MaintenanceRepository;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;

    /** Workflow: Pending -> Approved/Rejected -> Technician Assigned -> In Progress -> Resolved. */
    public MaintenanceRequest raiseRequest(MaintenanceDTO dto) {
        Asset asset = getAssetOrThrow(dto.getAssetId());
        User raisedBy = getUserOrThrow(dto.getRaisedById());

        MaintenanceRequest request = MaintenanceRequest.builder()
                .asset(asset)
                .raisedBy(raisedBy)
                .issueDescription(dto.getIssueDescription())
                .priority(dto.getPriority() != null ? dto.getPriority() : MaintenancePriority.MEDIUM)
                .photoUrl(dto.getPhotoUrl())
                .status(MaintenanceStatus.PENDING)
                .build();

        return maintenanceRepository.save(request);
    }

    /** Only an Asset Manager (or Admin) may approve. Approving flips the asset to UNDER_MAINTENANCE. */
    public MaintenanceRequest approveRequest(Long requestId, Long approverId) {
        MaintenanceRequest request = getRequestOrThrow(requestId);
        User approver = getUserOrThrow(approverId);
        assertCanApprove(approver);

        if (request.getStatus() != MaintenanceStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING request can be approved (current status: " + request.getStatus() + ")");
        }

        request.setStatus(MaintenanceStatus.APPROVED);
        request.setApprovedBy(approver);
        request = maintenanceRepository.save(request);

        assetService.updateAssetStatus(request.getAsset().getId(), AssetStatus.UNDER_MAINTENANCE);

        return request;
    }

    public MaintenanceRequest rejectRequest(Long requestId, Long approverId, String reason) {
        MaintenanceRequest request = getRequestOrThrow(requestId);
        User approver = getUserOrThrow(approverId);
        assertCanApprove(approver);

        if (request.getStatus() != MaintenanceStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING request can be rejected (current status: " + request.getStatus() + ")");
        }

        request.setStatus(MaintenanceStatus.REJECTED);
        request.setApprovedBy(approver);
        request.setRejectionReason(reason);

        return maintenanceRepository.save(request);
    }

    public MaintenanceRequest assignTechnician(Long requestId, Long technicianId) {
        MaintenanceRequest request = getRequestOrThrow(requestId);

        if (request.getStatus() != MaintenanceStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "A technician can only be assigned to an APPROVED request (current status: "
                            + request.getStatus() + ")");
        }

        request.setTechnician(getUserOrThrow(technicianId));
        request.setStatus(MaintenanceStatus.TECHNICIAN_ASSIGNED);

        return maintenanceRepository.save(request);
    }

    /** Moves a TECHNICIAN_ASSIGNED request into IN_PROGRESS once repair work actually starts. */
    public MaintenanceRequest startWork(Long requestId) {
        MaintenanceRequest request = getRequestOrThrow(requestId);
        if (request.getStatus() != MaintenanceStatus.TECHNICIAN_ASSIGNED) {
            throw new InvalidStatusTransitionException(
                    "Work can only start once a technician is assigned (current status: " + request.getStatus() + ")");
        }
        request.setStatus(MaintenanceStatus.IN_PROGRESS);
        return maintenanceRepository.save(request);
    }

    /** Resolving flips the asset back to AVAILABLE and retains the maintenance record in asset history. */
    public MaintenanceRequest resolveRequest(Long requestId, String resolutionNotes) {
        MaintenanceRequest request = getRequestOrThrow(requestId);

        if (request.getStatus() != MaintenanceStatus.IN_PROGRESS
                && request.getStatus() != MaintenanceStatus.TECHNICIAN_ASSIGNED) {
            throw new InvalidStatusTransitionException(
                    "Only a request that is IN_PROGRESS or TECHNICIAN_ASSIGNED can be resolved (current status: "
                            + request.getStatus() + ")");
        }

        request.setStatus(MaintenanceStatus.RESOLVED);
        request.setResolutionNotes(resolutionNotes);
        request.setResolvedAt(LocalDateTime.now());
        request = maintenanceRepository.save(request);

        assetService.updateAssetStatus(request.getAsset().getId(), AssetStatus.AVAILABLE);

        return request;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getMaintenanceHistoryForAsset(Long assetId) {
        return maintenanceRepository.findByAssetIdOrderByCreatedAtDesc(assetId);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getRequestsByStatus(MaintenanceStatus status) {
        return maintenanceRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getRequestsRaisedBy(Long userId) {
        return maintenanceRepository.findByRaisedById(userId);
    }

    private void assertCanApprove(User approver) {
        if (approver.getRole() != UserRole.ASSET_MANAGER && approver.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "Only an Asset Manager may approve or reject maintenance requests");
        }
    }

    private MaintenanceRequest getRequestOrThrow(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("MaintenanceRequest", id));
    }

    private Asset getAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Asset", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
