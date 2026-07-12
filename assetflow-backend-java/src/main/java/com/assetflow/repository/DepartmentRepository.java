package com.assetflow.repository;

import com.assetflow.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    List<Department> findByParentDepartmentId(Long parentDepartmentId);

    List<Department> findByParentDepartmentIsNull();

    boolean existsByName(String name);
}
