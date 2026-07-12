package com.assetflow.repository;

import com.assetflow.model.MaintenanceRequest;
import com.assetflow.model.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepository extends JpaRepository<MaintenanceRequest, Long> {

    List<MaintenanceRequest> findByAssetId(Long assetId);

    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);

    List<MaintenanceRequest> findByRaisedById(Long raisedById);

    List<MaintenanceRequest> findByAssetIdOrderByCreatedAtDesc(Long assetId);
}
