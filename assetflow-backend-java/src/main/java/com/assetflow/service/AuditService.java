package com.assetflow.service;

import com.assetflow.dto.AuditCycleDTO;
import com.assetflow.dto.AuditEntryDTO;
import com.assetflow.dto.DiscrepancyDTO;
import com.assetflow.exception.InvalidStatusTransitionException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.model.*;
import com.assetflow.repository.AssetRepository;
import com.assetflow.repository.AuditCycleRepository;
import com.assetflow.repository.AuditRepository;
import com.assetflow.repository.DepartmentRepository;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditCycleRepository auditCycleRepository;
    private final AuditRepository auditRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    /** Creates an audit cycle scoped to a department and/or location, with one or more assigned auditors. */
    public AuditCycle createAuditCycle(AuditCycleDTO dto) {
        if (dto.getEndDate() != null && dto.getStartDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }

        Department scopeDepartment = dto.getScopeDepartmentId() != null
                ? getDepartmentOrThrow(dto.getScopeDepartmentId())
                : null;

        Set<User> auditors = new HashSet<>();
        if (dto.getAuditorIds() != null) {
            for (Long auditorId : dto.getAuditorIds()) {
                auditors.add(getUserOrThrow(auditorId));
            }
        }
        if (auditors.isEmpty()) {
            throw new IllegalArgumentException("At least one auditor must be assigned to an audit cycle");
        }

        AuditCycle cycle = AuditCycle.builder()
                .name(dto.getName())
                .scopeDepartment(scopeDepartment)
                .scopeLocation(dto.getScopeLocation())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .auditors(auditors)
                .status(AuditCycleStatus.OPEN)
                .build();

        return auditCycleRepository.save(cycle);
    }

    /** Auditor marks a single asset within a cycle as VERIFIED / MISSING / DAMAGED. */
    public AuditEntry createAuditEntry(AuditEntryDTO dto) {
        AuditCycle cycle = getCycleOrThrow(dto.getAuditCycleId());
        if (cycle.getStatus() == AuditCycleStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Audit cycle " + cycle.getId() + " is closed and locked");
        }

        Asset asset = getAssetOrThrow(dto.getAssetId());
        User auditor = getUserOrThrow(dto.getAuditorId());

        if (!cycle.getAuditors().contains(auditor)) {
            throw new IllegalArgumentException("User " + auditor.getId() + " is not an assigned auditor for this cycle");
        }

        if (cycle.getStatus() == AuditCycleStatus.OPEN) {
            cycle.setStatus(AuditCycleStatus.IN_PROGRESS);
            auditCycleRepository.save(cycle);
        }

        AuditEntry entry = AuditEntry.builder()
                .auditCycle(cycle)
                .asset(asset)
                .auditor(auditor)
                .result(dto.getResult() != null ? dto.getResult() : AuditResult.PENDING)
                .notes(dto.getNotes())
                .verifiedAt(LocalDateTime.now())
                .build();

        return auditRepository.save(entry);
    }

    /**
     * Closes and locks an audit cycle. Confirmed-missing items flip the asset status to LOST;
     * damaged items are flagged for maintenance follow-up but otherwise left as-is for a human
     * to route through the maintenance workflow.
     */
    public AuditCycle closeAuditCycle(Long cycleId) {
        AuditCycle cycle = getCycleOrThrow(cycleId);

        if (cycle.getStatus() == AuditCycleStatus.CLOSED) {
            throw new InvalidStatusTransitionException("Audit cycle " + cycleId + " is already closed");
        }

        List<AuditEntry> entries = auditRepository.findByAuditCycleId(cycleId);

        for (AuditEntry entry : entries) {
            if (entry.getResult() == AuditResult.MISSING) {
                Asset asset = entry.getAsset();
                if (asset.getStatus() != AssetStatus.LOST) {
                    asset.setStatus(AssetStatus.LOST);
                    assetRepository.save(asset);
                }
            }
        }

        cycle.setStatus(AuditCycleStatus.CLOSED);
        cycle.setClosedAt(LocalDateTime.now());

        return auditCycleRepository.save(cycle);
    }

    /** Auto-generates a discrepancy report of all non-VERIFIED (MISSING/DAMAGED) entries for a cycle. */
    @Transactional(readOnly = true)
    public List<DiscrepancyDTO> generateDiscrepancyReport(Long cycleId) {
        getCycleOrThrow(cycleId);

        return auditRepository.findByAuditCycleId(cycleId).stream()
                .filter(entry -> entry.getResult() == AuditResult.MISSING || entry.getResult() == AuditResult.DAMAGED)
                .map(entry -> DiscrepancyDTO.builder()
                        .auditEntryId(entry.getId())
                        .assetId(entry.getAsset().getId())
                        .assetTag(entry.getAsset().getAssetTag())
                        .assetName(entry.getAsset().getName())
                        .result(entry.getResult())
                        .notes(entry.getNotes())
                        .auditorName(entry.getAuditor().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditEntry> getEntriesForCycle(Long cycleId) {
        return auditRepository.findByAuditCycleId(cycleId);
    }

    @Transactional(readOnly = true)
    public List<AuditEntry> getAuditHistoryForAsset(Long assetId) {
        return auditRepository.findByAssetId(assetId);
    }

    @Transactional(readOnly = true)
    public AuditCycle getCycleById(Long cycleId) {
        return getCycleOrThrow(cycleId);
    }

    private AuditCycle getCycleOrThrow(Long id) {
        return auditCycleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AuditCycle", id));
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
