package com.assetflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "audit_cycles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_department_id")
    private Department scopeDepartment;

    private String scopeLocation;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
            name = "audit_cycle_auditors",
            joinColumns = @JoinColumn(name = "audit_cycle_id"),
            inverseJoinColumns = @JoinColumn(name = "auditor_id")
    )
    @Builder.Default
    private Set<User> auditors = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuditCycleStatus status = AuditCycleStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
