package com.assetflow.repository;

import com.assetflow.model.Asset;
import com.assetflow.model.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetTag(String assetTag);

    Optional<Asset> findBySerialNumber(String serialNumber);

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByDepartmentId(Long departmentId);

    List<Asset> findByCategoryId(Long categoryId);

    List<Asset> findBySharedBookableTrue();

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByAssetTag(String assetTag);

    long countByStatus(AssetStatus status);
}
