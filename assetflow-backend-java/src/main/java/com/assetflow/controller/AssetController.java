package com.assetflow.controller;

import com.assetflow.dto.AssetDTO;
import com.assetflow.model.Asset;
import com.assetflow.model.AssetStatus;
import com.assetflow.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    // Register a new asset
    @PostMapping
    public Asset registerAsset(@RequestBody AssetDTO dto) {
        return assetService.registerAsset(dto);
    }

    // Get asset by ID
    @GetMapping("/{id}")
    public Asset getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }

    // Get asset by Asset Tag
    @GetMapping("/tag/{assetTag}")
    public Asset getAssetByTag(@PathVariable String assetTag) {
        return assetService.getAssetByTag(assetTag);
    }

    // Get all assets with optional filters
    @GetMapping
    public List<Asset> getAssets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String department) {

        return assetService.getAssets(status, category, department);
    }

    // Search assets
    @GetMapping("/search")
    public List<Asset> searchAssets(@RequestParam String query) {
        return assetService.searchAssets(query);
    }

    // Update asset status
    @PatchMapping("/{id}/status")
    public Asset updateStatus(
            @PathVariable Long id,
            @RequestParam AssetStatus status) {

        return assetService.updateAssetStatus(id, status);
    }
}