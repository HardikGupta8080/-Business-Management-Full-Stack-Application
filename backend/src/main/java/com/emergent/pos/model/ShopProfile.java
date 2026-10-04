package com.emergent.pos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Legacy REST endpoint kept for API parity with the FastAPI backend
// (GET/POST /api/shop-profile). In practice both the web app and native app
// actually read/write the shop profile through /api/config under the
// "hw_shopProfile" key, same as every other entity — this table/endpoint is
// otherwise unused by the current clients.
@Entity
@Table(name = "shop_profile")
@Getter
@Setter
@NoArgsConstructor
public class ShopProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shop_name")
    @JsonProperty("shop_name")
    private String shopName;

    @Column(name = "owner_name")
    @JsonProperty("owner_name")
    private String ownerName;

    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String state;
    private String pincode;

    @Column(name = "gst_number")
    @JsonProperty("gst_number")
    private String gstNumber;

    @Column(name = "pan_number")
    @JsonProperty("pan_number")
    private String panNumber;

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
