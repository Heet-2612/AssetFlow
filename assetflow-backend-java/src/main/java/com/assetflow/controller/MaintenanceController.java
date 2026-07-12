package com.assetflow.controller;

import com.assetflow.dto.MaintenanceDTO;
import com.assetflow.model.MaintenanceRequest;
import com.assetflow.model.MaintenanceStatus;
import com.assetflow.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // Raise Maintenance Request
    @PostMapping
    public MaintenanceRequest raiseRequest(@RequestBody MaintenanceDTO dto) {
        return maintenanceService.raiseRequest(dto);
    }

    // Approve Request
    @PatchMapping("/{id}/approve")
    public MaintenanceRequest approveRequest(
            @PathVariable Long id,
            @RequestParam Long approverId) {

        return maintenanceService.approveRequest(id, approverId);
    }

    // Reject Request
    @PatchMapping("/{id}/reject")
    public MaintenanceRequest rejectRequest(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam String reason) {

        return maintenanceService.rejectRequest(id, approverId, reason);
    }

    // Assign Technician
    @PatchMapping("/{id}/assign")
    public MaintenanceRequest assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId) {

        return maintenanceService.assignTechnician(id, technicianId);
    }

    // Start Work
    @PatchMapping("/{id}/start")
    public MaintenanceRequest startWork(@PathVariable Long id) {
        return maintenanceService.startWork(id);
    }

    // Resolve Request
    @PatchMapping("/{id}/resolve")
    public MaintenanceRequest resolveRequest(
            @PathVariable Long id,
            @RequestParam String resolutionNotes) {

        return maintenanceService.resolveRequest(id, resolutionNotes);
    }

    // Maintenance History for Asset
    @GetMapping("/asset/{assetId}")
    public List<MaintenanceRequest> getHistory(@PathVariable Long assetId) {
        return maintenanceService.getMaintenanceHistoryForAsset(assetId);
    }

    // Requests by Status
    @GetMapping("/status/{status}")
    public List<MaintenanceRequest> getByStatus(@PathVariable MaintenanceStatus status) {
        return maintenanceService.getRequestsByStatus(status);
    }

    // Requests Raised by User
    @GetMapping("/user/{userId}")
    public List<MaintenanceRequest> getRaisedBy(@PathVariable Long userId) {
        return maintenanceService.getRequestsRaisedBy(userId);
    }
}