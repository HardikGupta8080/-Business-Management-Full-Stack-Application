package com.emergent.pos.controller;

import com.emergent.pos.dto.ConfigRequest;
import com.emergent.pos.model.AppConfig;
import com.emergent.pos.repository.AppConfigRepository;
import com.emergent.pos.service.ConfigSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// This is the real read/write path every client (web app, WebView app,
// native Android app) uses for all data — items, parties, invoices,
// settings, etc. are all saved here as JSON blobs keyed by e.g. "hw_items".
// Writes to known entity keys additionally get mirrored into the relational
// tables (see ConfigSyncService). Exact same contract as the FastAPI
// backend's /api/config endpoints.
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AppConfigRepository appConfigRepository;
    private final ConfigSyncService configSyncService;

    @GetMapping("/{configKey}")
    public AppConfig getConfig(@PathVariable String configKey) {
        return appConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found"));
    }

    @PostMapping
    @Transactional
    public AppConfig createOrUpdateConfig(@Valid @RequestBody ConfigRequest request) {
        AppConfig config = appConfigRepository.findByConfigKey(request.getConfigKey())
                .orElseGet(AppConfig::new);
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        AppConfig saved = appConfigRepository.save(config);
        configSyncService.sync(request.getConfigKey(), request.getConfigValue());
        return saved;
    }
}
