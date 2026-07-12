package com.assetflow.controller;

import com.assetflow.dto.AuditCycleDTO;
import com.assetflow.dto.AuditEntryDTO;
import com.assetflow.dto.DiscrepancyDTO;
import com.assetflow.model.AuditCycle;
import com.assetflow.model.AuditEntry;
import com.assetflow.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditService auditService;

    // Create Audit Cycle
    @PostMapping("/cycles")
    public AuditCycle createAuditCycle(@RequestBody AuditCycleDTO dto) {
        return auditService.createAuditCycle(dto);
    }

    // Create Audit Entry
    @PostMapping("/entries")
    public AuditEntry createAuditEntry(@RequestBody AuditEntryDTO dto) {
        return auditService.createAuditEntry(dto);
    }

    // Close Audit Cycle
    @PatchMapping("/cycles/{cycleId}/close")
    public AuditCycle closeAuditCycle(@PathVariable Long cycleId) {
        return auditService.closeAuditCycle(cycleId);
    }

    // Discrepancy Report
    @GetMapping("/cycles/{cycleId}/discrepancies")
    public List<DiscrepancyDTO> generateDiscrepancyReport(@PathVariable Long cycleId) {
        return auditService.generateDiscrepancyReport(cycleId);
    }

    // Entries for Audit Cycle
    @GetMapping("/cycles/{cycleId}/entries")
    public List<AuditEntry> getEntriesForCycle(@PathVariable Long cycleId) {
        return auditService.getEntriesForCycle(cycleId);
    }

    // Audit History for Asset
    @GetMapping("/asset/{assetId}")
    public List<AuditEntry> getAuditHistoryForAsset(@PathVariable Long assetId) {
        return auditService.getAuditHistoryForAsset(assetId);
    }

    // Get Audit Cycle
    @GetMapping("/cycles/{cycleId}")
    public AuditCycle getCycle(@PathVariable Long cycleId) {
        return auditService.getCycleById(cycleId);
    }
}