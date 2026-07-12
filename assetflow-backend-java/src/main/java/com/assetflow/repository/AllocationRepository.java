package com.assetflow.repository;

import com.assetflow.model.Allocation;
import com.assetflow.model.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByEmployeeId(Long employeeId);

    List<Allocation> findByAssetId(Long assetId);

    List<Allocation> findByStatus(AllocationStatus status);

    Optional<Allocation> findByAssetIdAndStatus(Long assetId, AllocationStatus status);

    List<Allocation> findByDepartmentId(Long departmentId);

    List<Allocation> findByStatusAndExpectedReturnDateBefore(AllocationStatus status, LocalDate date);
}
