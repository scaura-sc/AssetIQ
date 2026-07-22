package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** F14 happy path: complaint registration with warranty computation and repeat-complaint detection. */
class ServiceEventIT extends AbstractIntegrationTest {

    private static final String TENANT = "tenant-service-event-it";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetCatalogRepository assetCatalogRepository;

    @Test
    void complaintRegistrationComputesWarrantyAndDetectsRepeat() throws Exception {
        seedCatalog();
        long assetId = createAssetWithWarranty("2025-01-01", "2027-01-01");

        // First complaint of this type on this asset — not a repeat.
        JsonNode first = objectMapper.readTree(mockMvc.perform(post("/api/service-events/complaints")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetId":%d,"outletCode":"OUT-IT","priority":"HIGH","description":"cooling issue",
                                 "raisedByUserCode":"it-tester","complaintType":"COOLING_ISSUE","raisedAt":"2026-07-01T09:00:00"}
                                """.formatted(assetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isUnderWarranty").value(true))
                .andExpect(jsonPath("$.isRepeated").value(false))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString());
        assertThat(first.get("eventType").asText()).isEqualTo("COMPLAINT");

        // Same asset, same complaint type, 14 days later — within the 30-day repeat window.
        mockMvc.perform(post("/api/service-events/complaints")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetId":%d,"outletCode":"OUT-IT","priority":"HIGH","description":"cooling issue again",
                                 "raisedByUserCode":"it-tester","complaintType":"COOLING_ISSUE","raisedAt":"2026-07-15T09:00:00"}
                                """.formatted(assetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isRepeated").value(true));

        // Different complaint type, same asset — not a repeat of a different issue.
        mockMvc.perform(post("/api/service-events/complaints")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetId":%d,"outletCode":"OUT-IT","priority":"LOW","description":"noise",
                                 "raisedByUserCode":"it-tester","complaintType":"NOISE","raisedAt":"2026-07-16T09:00:00"}
                                """.formatted(assetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isRepeated").value(false));

        // Outside the warranty window entirely.
        mockMvc.perform(post("/api/service-events/complaints")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetId":%d,"outletCode":"OUT-IT","priority":"LOW","description":"post-warranty issue",
                                 "raisedByUserCode":"it-tester","complaintType":"GAS_REFILL","raisedAt":"2027-06-01T09:00:00"}
                                """.formatted(assetId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isUnderWarranty").value(false));
    }

    private void seedCatalog() {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId(TENANT);
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("SE-COOLER");
        category.setName("SE Cooler");
        assetCatalogRepository.save(category);

        AiqAssetCatalog type = new AiqAssetCatalog();
        type.setTenantId(TENANT);
        type.setLevel(CatalogLevel.TYPE);
        type.setCode("SE-VISI");
        type.setName("SE Visi Cooler");
        type.setParentCode("SE-COOLER");
        assetCatalogRepository.save(type);

        AiqAssetCatalog model = new AiqAssetCatalog();
        model.setTenantId(TENANT);
        model.setLevel(CatalogLevel.MODEL);
        model.setCode("SE-VC300");
        model.setName("SE VisiCooler 300L");
        model.setParentCode("SE-VISI");
        assetCatalogRepository.save(model);
    }

    private long createAssetWithWarranty(String warrantyStart, String warrantyEnd) throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"SN-SE-1","assetName":"SE Test Asset","categoryCode":"SE-COOLER",
                                 "typeCode":"SE-VISI","modelCode":"SE-VC300","purchaseDate":"2025-01-01",
                                 "purchasePrice":10000.00,"warrantyStartDate":"%s","warrantyEndDate":"%s",
                                 "createdBy":"it-tester"}
                                """.formatted(warrantyStart, warrantyEnd)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
