package com.assetflow.controller;

import com.assetflow.dto.AllocationDTO;
import com.assetflow.dto.ReturnDTO;
import com.assetflow.dto.TransferDTO;
import com.assetflow.model.Allocation;
import com.assetflow.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AllocationController {

    private final AllocationService allocationService;

    // Allocate Asset
    @PostMapping
    public Allocation allocateAsset(@RequestBody AllocationDTO dto) {
        return allocationService.allocateAsset(dto);
    }

    // Return Asset
    @PatchMapping("/{id}/return")
    public Allocation returnAsset(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnDTO dto) {
        return allocationService.returnAsset(id, dto);
    }

    // Transfer Asset
    @PatchMapping("/{id}/transfer")
    public Allocation transferAsset(
            @PathVariable Long id,
            @RequestBody TransferDTO dto) {
        return allocationService.transferAsset(id, dto);
    }

    // Request Transfer
    @PatchMapping("/{id}/request-transfer")
    public Allocation requestTransfer(
            @PathVariable Long id,
            @RequestParam(required = false) Long requestedByUserId) {
        return allocationService.requestTransfer(id, requestedByUserId);
    }

    // Overdue Allocations
    @GetMapping("/overdue")
    public List<Allocation> getOverdueAllocations() {
        return allocationService.getOverdueAllocations();
    }

    // Employee Allocations
    @GetMapping("/employee/{employeeId}")
    public List<Allocation> getAllocationsForEmployee(@PathVariable Long employeeId) {
        return allocationService.getAllocationsForEmployee(employeeId);
    }

    // Asset Allocation History
    @GetMapping("/asset/{assetId}")
    public List<Allocation> getAllocationHistory(@PathVariable Long assetId) {
        return allocationService.getAllocationHistoryForAsset(assetId);
    }

    // Department Allocations
    @GetMapping("/department/{departmentId}")
    public List<Allocation> getAllocationsForDepartment(@PathVariable Long departmentId) {
        return allocationService.getAllocationsForDepartment(departmentId);
    }
}