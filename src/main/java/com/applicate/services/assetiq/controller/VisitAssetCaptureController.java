package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.visit.VisitCaptureCreateRequest;
import com.applicate.services.assetiq.dto.visit.VisitCaptureResponse;
import com.applicate.services.assetiq.service.VisitAssetCaptureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** F09 (visit capture) + F10 (purity scoring). */
@RestController
@RequestMapping("/api/visit-captures")
public class VisitAssetCaptureController {

    private final VisitAssetCaptureService visitAssetCaptureService;

    public VisitAssetCaptureController(VisitAssetCaptureService visitAssetCaptureService) {
        this.visitAssetCaptureService = visitAssetCaptureService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitCaptureResponse create(@Valid @RequestBody VisitCaptureCreateRequest request) {
        return visitAssetCaptureService.create(request);
    }

    @GetMapping("/{id}")
    public VisitCaptureResponse get(@PathVariable Long id) {
        return visitAssetCaptureService.get(id);
    }

    @GetMapping
    public List<VisitCaptureResponse> listByAsset(@RequestParam Long assetId) {
        return visitAssetCaptureService.listByAsset(assetId);
    }
}
