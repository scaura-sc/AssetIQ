package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.scoringconfig.ScoringConfigRequest;
import com.applicate.services.assetiq.dto.scoringconfig.ScoringConfigResponse;
import com.applicate.services.assetiq.service.ScoringConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Tenant-wide AHS weightage config — a singleton resource per tenant (X-Tenant-Id), not a list. */
@RestController
@RequestMapping("/api/scoring-config")
public class ScoringConfigController {

    private final ScoringConfigService scoringConfigService;

    public ScoringConfigController(ScoringConfigService scoringConfigService) {
        this.scoringConfigService = scoringConfigService;
    }

    @GetMapping
    public ScoringConfigResponse get() {
        return scoringConfigService.get();
    }

    @PutMapping
    public ScoringConfigResponse upsert(@Valid @RequestBody ScoringConfigRequest request) {
        return scoringConfigService.upsert(request);
    }
}
