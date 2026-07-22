package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.repository.AssetMovementLogRepository;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full happy path for F05 (deploy), F06 (transfer), F07 (swap) through the real REST layer. */
class AssetDeploymentIT extends AbstractIntegrationTest {

    private static final String TENANT = "tenant-deploy-it";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetCatalogRepository assetCatalogRepository;

    @Autowired
    private AssetMovementLogRepository assetMovementLogRepository;

    @Test
    void deployTransferAndSwapHappyPath() throws Exception {
        seedCatalog();

        long asset1Id = createAsset("SN-DEP-1");
        long asset2Id = createAsset("SN-DEP-2");
        long asset3Id = createAsset("SN-DEP-3");

        // --- F05 deploy ---
        JsonNode deployResult = objectMapper.readTree(mockMvc.perform(post("/api/assets/" + asset1Id + "/deploy")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"outletCode":"OUT-IT-1","outletName":"IT Outlet",
                                 "assignmentDate":"2026-07-01","movedByUserCode":"it-tester","reason":"initial deployment"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asset.assetStatus").value("DEPLOYED"))
                .andExpect(jsonPath("$.association.locationCode").value("OUT-IT-1"))
                .andExpect(jsonPath("$.movementLog.movementType").value("ASSIGN"))
                .andReturn().getResponse().getContentAsString());
        assertThat(deployResult.get("asset").get("workingStatus").asText()).isEqualTo("WORKING");

        // F06's interim "marked for retrieval" rule needs a prior TRANSFER/SWAP_OUT row moving the
        // asset out of the outlet — there's no dedicated retrieval-request entity yet (see
        // AssetMovementValidator javadoc), so this simulates that step having already happened.
        AiqAssetMovementLog retrievalMarker = new AiqAssetMovementLog();
        retrievalMarker.setTenantId(TENANT);
        retrievalMarker.setAssetId(asset1Id);
        retrievalMarker.setAssetNumber(deployResult.get("asset").get("assetNumber").asText());
        retrievalMarker.setMovementType(MovementType.TRANSFER);
        retrievalMarker.setToLocationType(LocationType.WAREHOUSE);
        retrievalMarker.setToLocationCode("WH-STAGING");
        retrievalMarker.setMovedByUserCode("supervisor");
        retrievalMarker.setReason("marked for retrieval (simulated interim step)");
        retrievalMarker.setMovedAt(LocalDateTime.now());
        assetMovementLogRepository.save(retrievalMarker);

        // --- F06 transfer ---
        mockMvc.perform(post("/api/assets/" + asset1Id + "/transfer")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toLocationType":"WAREHOUSE","toLocationCode":"WH-IT-1","targetAssetStatus":"STOCK",
                                 "assignmentDate":"2026-07-15","movedByUserCode":"it-tester","reason":"relocation"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asset.assetStatus").value("STOCK"))
                .andExpect(jsonPath("$.asset.locationCode").value("WH-IT-1"))
                .andExpect(jsonPath("$.movementLog.movementType").value("TRANSFER"))
                .andExpect(jsonPath("$.movementLog.fromLocationType").value("OUTLET"));

        // --- F07 swap: deploy asset3, then swap it out for stocked asset2 ---
        mockMvc.perform(post("/api/assets/" + asset3Id + "/deploy")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"outletCode":"OUT-IT-2","outletName":"IT Outlet 2",
                                 "assignmentDate":"2026-07-01","movedByUserCode":"it-tester","reason":"initial deployment"}
                                """))
                .andExpect(status().isOk());

        JsonNode swapResult = objectMapper.readTree(mockMvc.perform(post("/api/assets/swap")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldAssetId\":" + asset3Id + ",\"newAssetId\":" + asset2Id
                                + ",\"movedByUserCode\":\"it-tester\",\"reason\":\"unit malfunction\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oldAsset.assetStatus").value("UNDER_REPAIR"))
                .andExpect(jsonPath("$.newAsset.assetStatus").value("DEPLOYED"))
                .andExpect(jsonPath("$.newAsset.locationCode").value("OUT-IT-2"))
                .andExpect(jsonPath("$.swapOutMovement.movementType").value("SWAP_OUT"))
                .andExpect(jsonPath("$.swapInMovement.movementType").value("SWAP_IN"))
                .andReturn().getResponse().getContentAsString());

        // Both movement log rows must share one operation reference (approval_ref, per the
        // documented choice in SwapRequest) so the pair is traceable as a single swap.
        assertThat(swapResult.get("swapOutMovement").get("approvalRef").asText())
                .isEqualTo(swapResult.get("swapInMovement").get("approvalRef").asText())
                .isEqualTo(swapResult.get("swapReference").asText());
    }

    private void seedCatalog() {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId(TENANT);
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("IT-COOLER");
        category.setName("IT Cooler");
        assetCatalogRepository.save(category);

        AiqAssetCatalog type = new AiqAssetCatalog();
        type.setTenantId(TENANT);
        type.setLevel(CatalogLevel.TYPE);
        type.setCode("IT-VISI");
        type.setName("IT Visi Cooler");
        type.setParentCode("IT-COOLER");
        assetCatalogRepository.save(type);

        AiqAssetCatalog model = new AiqAssetCatalog();
        model.setTenantId(TENANT);
        model.setLevel(CatalogLevel.MODEL);
        model.setCode("IT-VC300");
        model.setName("IT VisiCooler 300L");
        model.setParentCode("IT-VISI");
        assetCatalogRepository.save(model);
    }

    private long createAsset(String serialNumber) throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"%s","assetName":"IT Test Asset","categoryCode":"IT-COOLER",
                                 "typeCode":"IT-VISI","modelCode":"IT-VC300","purchaseDate":"2026-01-01",
                                 "purchasePrice":10000.00,"createdBy":"it-tester"}
                                """.formatted(serialNumber)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
