package com.emergent.pos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This is the real read/write path every client (web app, WebView app,
// native Android app) uses for all data — items, parties, invoices,
// settings, etc. are all saved here as JSON blobs keyed by e.g. "hw_items".
// Writes to known entity keys additionally get mirrored into the relational
// tables in this same package (see ConfigSyncService).
@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    @JsonProperty("config_key")
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    @JsonProperty("config_value")
    private String configValue;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
