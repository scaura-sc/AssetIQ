package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.repository.AssetMovementLogRepository;
import com.applicate.services.assetiq.repository.RoleConfigRepository;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/assets/movements/search and GET /api/visit-captures/search — the
 * date-bounded activity feeds that stay separate from the fleet-snapshot's
 * current-state join. See docs/backend-requirements-fleet-snapshot.md
 * (AssetIQ-Dashboard, commit a86f460). Each test method uses its own tenant
 * id so the two independently-seeded catalogs never collide.
 */
class AssetActivitySearchIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetCatalogRepository assetCatalogRepository;

    @Autowired
    private RoleConfigRepository roleConfigRepository;

    @Autowired
    private AssetMovementLogRepository assetMovementLogRepository;

    @Test
    void movementsSearchFiltersByAssetTerritoryAndDateRange() throws Exception {
        String tenant = "tenant-activity-search-movements-it";
        seedCatalog(tenant);
        long asset1Id = createAsset(tenant, "SN-AS-1", "T1");
        long asset2Id = createAsset(tenant, "SN-AS-2", "T2");

        saveMovement(tenant, asset1Id, LocalDateTime.of(2026, 1, 1, 9, 0));
        saveMovement(tenant, asset1Id, LocalDateTime.of(2026, 2, 1, 9, 0));
        saveMovement(tenant, asset2Id, LocalDateTime.of(2026, 1, 15, 9, 0));

        assertThat(searchMovements(tenant, "assetId=" + asset1Id)).hasSize(2);
        assertThat(searchMovements(tenant, "territoryCode=T1")).hasSize(2);
        assertThat(searchMovements(tenant, "territoryCode=T2")).hasSize(1);
        assertThat(searchMovements(tenant, "from=2026-01-10T00:00:00&to=2026-01-31T23:59:59")).hasSize(1)
                .allSatisfy(m -> assertThat(m.get("assetId").asText()).isEqualTo(String.valueOf(asset2Id)));
        assertThat(searchMovements(tenant, "")).hasSize(3);
    }

    @Test
    void visitCapturesSearchFiltersByAssetTerritoryAndDateRange() throws Exception {
        String tenant = "tenant-activity-search-captures-it";
        seedCatalog(tenant);
        seedRole(tenant);
        long asset1Id = createAsset(tenant, "SN-AS-3", "T1");
        long asset2Id = createAsset(tenant, "SN-AS-4", "T2");

        createCapture(tenant, asset1Id, "T1", "2026-01-01T09:00:00");
        createCapture(tenant, asset1Id, "T1", "2026-02-01T09:00:00");
        createCapture(tenant, asset2Id, "T2", "2026-01-15T09:00:00");

        assertThat(searchCaptures(tenant, "assetId=" + asset1Id)).hasSize(2);
        assertThat(searchCaptures(tenant, "territoryCode=T1")).hasSize(2);
        assertThat(searchCaptures(tenant, "territoryCode=T2")).hasSize(1);
        assertThat(searchCaptures(tenant, "from=2026-01-10T00:00:00&to=2026-01-31T23:59:59")).hasSize(1)
                .allSatisfy(c -> assertThat(c.get("assetId").asText()).isEqualTo(String.valueOf(asset2Id)));
        assertThat(searchCaptures(tenant, "")).hasSize(3);
    }

    private List<JsonNode> searchMovements(String tenant, String queryString) throws Exception {
        String path = "/api/assets/movements/search" + (queryString.isEmpty() ? "" : "?" + queryString);
        JsonNode result = objectMapper.readTree(mockMvc.perform(get(path).header("X-Tenant-Id", tenant))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        return toList(result);
    }

    private List<JsonNode> searchCaptures(String tenant, String queryString) throws Exception {
        String path = "/api/visit-captures/search" + (queryString.isEmpty() ? "" : "?" + queryString);
        JsonNode result = objectMapper.readTree(mockMvc.perform(get(path).header("X-Tenant-Id", tenant))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        return toList(result);
    }

    private List<JsonNode> toList(JsonNode arrayNode) {
        return objectMapper.convertValue(arrayNode, new com.fasterxml.jackson.core.type.TypeReference<List<JsonNode>>() {
        });
    }

    private void saveMovement(String tenant, long assetId, LocalDateTime movedAt) {
        AiqAssetMovementLog movement = new AiqAssetMovementLog();
        movement.setTenantId(tenant);
        movement.setAssetId(assetId);
        movement.setAssetNumber("AST-" + assetId);
        movement.setMovementType(MovementType.ASSIGN);
        movement.setToLocationType(LocationType.OUTLET);
        movement.setToLocationCode("OUT-AS");
        movement.setMovedByUserCode("it-tester");
        movement.setMovedAt(movedAt);
        assetMovementLogRepository.saveAndFlush(movement);
    }

    private void seedCatalog(String tenant) {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId(tenant);
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("AS-COOLER");
        category.setName("AS Cooler");
        assetCatalogRepository.save(category);

        AiqAssetCatalog type = new AiqAssetCatalog();
        type.setTenantId(tenant);
        type.setLevel(CatalogLevel.TYPE);
        type.setCode("AS-VISI");
        type.setName("AS Visi Cooler");
        type.setParentCode("AS-COOLER");
        assetCatalogRepository.save(type);

        AiqAssetCatalog model = new AiqAssetCatalog();
        model.setTenantId(tenant);
        model.setLevel(CatalogLevel.MODEL);
        model.setCode("AS-VC300");
        model.setName("AS VisiCooler 300L");
        model.setParentCode("AS-VISI");
        assetCatalogRepository.save(model);
    }

    private void seedRole(String tenant) {
        AiqRoleConfig role = new AiqRoleConfig();
        role.setTenantId(tenant);
        role.setRoleCode(RoleCode.SALESMAN);
        role.setRoleName("Salesman");
        role.setAssetCaptureEligible(true);
        roleConfigRepository.save(role);
    }

    private long createAsset(String tenant, String serialNumber, String territoryCode) throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("X-Tenant-Id", tenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"%s","assetName":"AS Test Asset","categoryCode":"AS-COOLER",
                                 "typeCode":"AS-VISI","modelCode":"AS-VC300","purchaseDate":"2026-01-01",
                                 "purchasePrice":10000.00,"warehouseCode":"WH-AS","territoryCode":"%s",
                                 "createdBy":"it-tester"}
                                """.formatted(serialNumber, territoryCode)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void createCapture(String tenant, long assetId, String territoryCode, String capturedAt) throws Exception {
        mockMvc.perform(post("/api/visit-captures")
                        .header("X-Tenant-Id", tenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visitId":"VISIT-%d-%s","visitDate":"2026-01-01","outletCode":"OUT-AS",
                                 "territoryCode":"%s","salesmanCode":"SM-AS","assetId":%d,"roleCode":"SALESMAN",
                                 "presenceStatus":"PRESENT","capturedAt":"%s"}
                                """.formatted(assetId, capturedAt, territoryCode, assetId, capturedAt)))
                .andExpect(status().isCreated());
    }
}
