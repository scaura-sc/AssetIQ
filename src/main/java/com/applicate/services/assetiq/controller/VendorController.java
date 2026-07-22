package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.dto.vendor.VendorCreateRequest;
import com.applicate.services.assetiq.dto.vendor.VendorResponse;
import com.applicate.services.assetiq.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse create(@Valid @RequestBody VendorCreateRequest request) {
        return vendorService.create(request);
    }

    @GetMapping("/{id}")
    public VendorResponse get(@PathVariable Long id) {
        return vendorService.get(id);
    }

    @GetMapping
    public List<VendorResponse> listActive() {
        return vendorService.listActive();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        vendorService.deactivate(id);
    }
}
