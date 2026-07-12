package com.assetflow.service;

import com.assetflow.dto.AssetDTO;
import com.assetflow.exception.DuplicateResourceException;
import com.assetflow.exception.InvalidStatusTransitionException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.model.*;
import com.assetflow.repository.AssetCategoryRepository;
import com.assetflow.repository.AssetRepository;
import com.assetflow.repository.DepartmentRepository;
import com.assetflow.util.AssetTagGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Legal lifecycle transitions. Anything not listed here is rejected by updateAssetStatus.
     * AVAILABLE <-> UNDER_MAINTENANCE, ALLOCATED -> AVAILABLE, etc. are modeled explicitly
     * per the problem statement's flexible-lifecycle requirement.
     */
    private static final Map<AssetStatus, Set<AssetStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(AssetStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(AssetStatus.AVAILABLE, EnumSet.of(
                AssetStatus.ALLOCATED, AssetStatus.RESERVED, AssetStatus.UNDER_MAINTENANCE,
                AssetStatus.LOST, AssetStatus.RETIRED));
        ALLOWED_TRANSITIONS.put(AssetStatus.ALLOCATED, EnumSet.of(
                AssetStatus.AVAILABLE, AssetStatus.UNDER_MAINTENANCE, AssetStatus.LOST));
        ALLOWED_TRANSITIONS.put(AssetStatus.RESERVED, EnumSet.of(
                AssetStatus.AVAILABLE, AssetStatus.ALLOCATED, AssetStatus.UNDER_MAINTENANCE));
        ALLOWED_TRANSITIONS.put(AssetStatus.UNDER_MAINTENANCE, EnumSet.of(
                AssetStatus.AVAILABLE, AssetStatus.LOST, AssetStatus.RETIRED));
        ALLOWED_TRANSITIONS.put(AssetStatus.LOST, EnumSet.of(
                AssetStatus.AVAILABLE, AssetStatus.RETIRED));
        ALLOWED_TRANSITIONS.put(AssetStatus.RETIRED, EnumSet.of(AssetStatus.DISPOSED));
        ALLOWED_TRANSITIONS.put(AssetStatus.DISPOSED, EnumSet.noneOf(AssetStatus.class));
    }

    public Asset registerAsset(AssetDTO dto) {
        AssetCategory category = assetCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("AssetCategory", dto.getCategoryId()));

        if (dto.getSerialNumber() != null && !dto.getSerialNumber().isBlank()
                && assetRepository.existsBySerialNumber(dto.getSerialNumber())) {
            throw new DuplicateResourceException(
                    "An asset with serial number '" + dto.getSerialNumber() + "' already exists");
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Department", dto.getDepartmentId()));
        }

        Asset asset = Asset.builder()
                .name(dto.getName())
                .category(category)
                .assetTag(generateAssetTag())
                .serialNumber(dto.getSerialNumber())
                .acquisitionDate(dto.getAcquisitionDate())
                .acquisitionCost(dto.getAcquisitionCost())
                .condition(dto.getCondition())
                .location(dto.getLocation())
                .department(department)
                .sharedBookable(dto.isSharedBookable())
                .photoUrl(dto.getPhotoUrl())
                .status(AssetStatus.AVAILABLE)
                .build();

        return assetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public Asset getAssetById(Long id) {
        return getAssetOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Asset getAssetByTag(String assetTag) {
        return assetRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with tag: " + assetTag));
    }

    /**
     * Filters assets by any combination of status / category name / department id.
     * Any parameter left null/blank is ignored.
     */
    @Transactional(readOnly = true)
    public List<Asset> getAssets(String status, String category, String department) {
        List<Asset> assets = assetRepository.findAll();

        if (status != null && !status.isBlank()) {
            AssetStatus statusEnum;
            try {
                statusEnum = AssetStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown asset status: " + status);
            }
            assets = assets.stream().filter(a -> a.getStatus() == statusEnum).toList();
        }

        if (category != null && !category.isBlank()) {
            assets = assets.stream()
                    .filter(a -> a.getCategory() != null
                            && category.equalsIgnoreCase(a.getCategory().getName()))
                    .toList();
        }

        if (department != null && !department.isBlank()) {
            assets = assets.stream()
                    .filter(a -> a.getDepartment() != null
                            && department.equalsIgnoreCase(a.getDepartment().getName()))
                    .toList();
        }

        return assets;
    }

    @Transactional(readOnly = true)
    public List<Asset> searchAssets(String query) {
        if (query == null || query.isBlank()) {
            return assetRepository.findAll();
        }
        String q = query.trim();
        return assetRepository.findAll().stream()
                .filter(a -> matches(a.getAssetTag(), q)
                        || matches(a.getSerialNumber(), q)
                        || matches(a.getLocation(), q)
                        || (a.getCategory() != null && matches(a.getCategory().getName(), q)))
                .toList();
    }

    private boolean matches(String field, String query) {
        return field != null && field.toLowerCase().contains(query.toLowerCase());
    }

    public Asset updateAssetStatus(Long id, AssetStatus newStatus) {
        Asset asset = getAssetOrThrow(id);
        AssetStatus current = asset.getStatus();

        if (current == newStatus) {
            return asset;
        }

        Set<AssetStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(AssetStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition asset " + asset.getAssetTag() + " from " + current + " to " + newStatus);
        }

        asset.setStatus(newStatus);
        return assetRepository.save(asset);
    }

    /** Throws if the asset cannot currently be allocated (must be AVAILABLE). */
    @Transactional(readOnly = true)
    public void assertAllocatable(Asset asset) {
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new InvalidStatusTransitionException(
                    "Asset " + asset.getAssetTag() + " is not available for allocation (current status: "
                            + asset.getStatus() + ")");
        }
    }

    /** Auto-generates the next sequential asset tag, e.g. AF-0001, AF-0002. */
    public String generateAssetTag() {
        long nextNumber = assetRepository.count() + 1;
        String candidate = AssetTagGenerator.generate(nextNumber);
        // Guard against gaps from deleted rows causing a collision.
        while (assetRepository.existsByAssetTag(candidate)) {
            nextNumber++;
            candidate = AssetTagGenerator.generate(nextNumber);
        }
        return candidate;
    }

    Asset getAssetOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Asset", id));
    }
}
