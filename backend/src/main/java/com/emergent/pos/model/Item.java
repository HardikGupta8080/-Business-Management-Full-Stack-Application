package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Read-friendly relational mirror of the "hw_items" JSON blob stored in
// app_config. Column names intentionally match the apps' camelCase JSON
// field names 1:1 (rather than usual snake_case DB convention), same as the
// FastAPI backend's models.py — backtick-quoted names below preserve exact
// case so this interoperates with a database the Python backend also uses.
// Never written to directly — fully rebuilt from app_config by
// ConfigSyncService on every relevant /api/config write.
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @Column(length = 50)
    private String id;

    private String name;
    private String sku;
    private String category;
    private String unit;

    @Column(name = "`retailPrice`")
    private Double retailPrice;

    @Column(name = "`wholesalePrice`")
    private Double wholesalePrice;

    @Column(name = "`costPrice`")
    private Double costPrice;

    private Double stock;

    @Column(name = "`reorderLevel`")
    private Double reorderLevel;

    @Column(name = "`hsnCode`")
    private String hsnCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String barcode;
}
