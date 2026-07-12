package com.assetflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** e.g. "warrantyPeriodMonths=24;requiresCalibration=true" stored as simple key=value pairs */
    @Column(length = 1000)
    private String customFields;
}
