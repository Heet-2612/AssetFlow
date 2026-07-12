package com.assetflow.service;

import com.assetflow.dto.DepartmentDTO;
import com.assetflow.exception.DuplicateResourceException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.model.AccountStatus;
import com.assetflow.model.Department;
import com.assetflow.model.User;
import com.assetflow.repository.DepartmentRepository;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public Department createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("A department named '" + dto.getName() + "' already exists");
        }

        Department.DepartmentBuilder builder = Department.builder()
                .name(dto.getName())
                .status(AccountStatus.ACTIVE);

        if (dto.getHeadUserId() != null) {
            builder.head(getUserOrThrow(dto.getHeadUserId()));
        }
        if (dto.getParentDepartmentId() != null) {
            builder.parentDepartment(getDepartmentOrThrow(dto.getParentDepartmentId()));
        }

        return departmentRepository.save(builder.build());
    }

    public Department updateDepartment(Long id, DepartmentDTO dto) {
        Department department = getDepartmentOrThrow(id);

        if (dto.getName() != null && !dto.getName().equals(department.getName())) {
            if (departmentRepository.existsByName(dto.getName())) {
                throw new DuplicateResourceException("A department named '" + dto.getName() + "' already exists");
            }
            department.setName(dto.getName());
        }

        if (dto.getHeadUserId() != null) {
            department.setHead(getUserOrThrow(dto.getHeadUserId()));
        }

        if (dto.getParentDepartmentId() != null) {
            if (dto.getParentDepartmentId().equals(id)) {
                throw new IllegalArgumentException("A department cannot be its own parent");
            }
            department.setParentDepartment(getDepartmentOrThrow(dto.getParentDepartmentId()));
        }

        return departmentRepository.save(department);
    }

    public Department assignHead(Long deptId, Long userId) {
        Department department = getDepartmentOrThrow(deptId);
        User user = getUserOrThrow(userId);
        department.setHead(user);
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentHierarchy() {
        // Return root departments; each Department carries its own parentDepartment reference,
        // so callers can walk/build the tree from this flat list, or filter by parent as needed.
        return departmentRepository.findByParentDepartmentIsNull();
    }

    @Transactional(readOnly = true)
    public List<Department> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentDepartmentId(parentId);
    }

    public void deactivateDepartment(Long id) {
        Department department = getDepartmentOrThrow(id);
        department.setStatus(AccountStatus.INACTIVE);
        departmentRepository.save(department);
    }

    public void activateDepartment(Long id) {
        Department department = getDepartmentOrThrow(id);
        department.setStatus(AccountStatus.ACTIVE);
        departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return getDepartmentOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    private Department getDepartmentOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
