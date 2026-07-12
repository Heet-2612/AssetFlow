package com.assetflow.repository;

import com.assetflow.model.AuditCycle;
import com.assetflow.model.AuditEntry;
import com.assetflow.model.AuditResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findByAuditCycleId(Long auditCycleId);

    List<AuditEntry> findByAssetId(Long assetId);

    List<AuditEntry> findByAuditCycleIdAndResult(Long auditCycleId, AuditResult result);
}
