package com.assetflow.repository;

import com.assetflow.model.AuditCycle;
import com.assetflow.model.AuditCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditCycleRepository extends JpaRepository<AuditCycle, Long> {

    List<AuditCycle> findByStatus(AuditCycleStatus status);

    List<AuditCycle> findByScopeDepartmentId(Long departmentId);
}
