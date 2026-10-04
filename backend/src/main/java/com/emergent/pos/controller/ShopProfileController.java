package com.emergent.pos.controller;

import com.emergent.pos.model.ShopProfile;
import com.emergent.pos.repository.ShopProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Kept for parity with the FastAPI backend's /api/shop-profile endpoints.
// Neither current client actually uses this — both read/write the shop
// profile through /api/config under the "hw_shopProfile" key instead — but
// it's here for anyone integrating directly against this REST resource.
@RestController
@RequestMapping("/api/shop-profile")
@RequiredArgsConstructor
public class ShopProfileController {

    private final ShopProfileRepository shopProfileRepository;

    @GetMapping
    public ShopProfile getShopProfile() {
        return shopProfileRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop profile not found"));
    }

    @PostMapping
    public ShopProfile createOrUpdateShopProfile(@RequestBody ShopProfile incoming) {
        ShopProfile profile = shopProfileRepository.findAll().stream().findFirst().orElseGet(ShopProfile::new);
        profile.setShopName(incoming.getShopName());
        profile.setOwnerName(incoming.getOwnerName());
        profile.setPhone(incoming.getPhone());
        profile.setEmail(incoming.getEmail());
        profile.setAddress(incoming.getAddress());
        profile.setCity(incoming.getCity());
        profile.setState(incoming.getState());
        profile.setPincode(incoming.getPincode());
        profile.setGstNumber(incoming.getGstNumber());
        profile.setPanNumber(incoming.getPanNumber());
        return shopProfileRepository.save(profile);
    }
}
