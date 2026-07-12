package com.assetflow.controller;

import com.assetflow.dto.DepartmentDTO;
import com.assetflow.model.Department;
import com.assetflow.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    // Create Department
    @PostMapping
    public Department createDepartment(@RequestBody DepartmentDTO dto) {
        return departmentService.createDepartment(dto);
    }

    // Update Department
    @PutMapping("/{id}")
    public Department updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO dto) {
        return departmentService.updateDepartment(id, dto);
    }

    // Assign Department Head
    @PatchMapping("/{deptId}/head/{userId}")
    public Department assignHead(
            @PathVariable Long deptId,
            @PathVariable Long userId) {
        return departmentService.assignHead(deptId, userId);
    }

    // Get Department Hierarchy
    @GetMapping("/hierarchy")
    public List<Department> getDepartmentHierarchy() {
        return departmentService.getDepartmentHierarchy();
    }

    // Get Child Departments
    @GetMapping("/{parentId}/children")
    public List<Department> getChildDepartments(@PathVariable Long parentId) {
        return departmentService.getChildDepartments(parentId);
    }

    // Deactivate Department
    @PatchMapping("/{id}/deactivate")
    public String deactivateDepartment(@PathVariable Long id) {
        departmentService.deactivateDepartment(id);
        return "Department deactivated successfully.";
    }

    // Activate Department
    @PatchMapping("/{id}/activate")
    public String activateDepartment(@PathVariable Long id) {
        departmentService.activateDepartment(id);
        return "Department activated successfully.";
    }

    // Get Department by ID
    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    // Get All Departments
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}