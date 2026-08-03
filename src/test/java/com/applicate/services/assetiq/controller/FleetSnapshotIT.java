package com.applicate.services.assetiq.controller;

import com.applicate.services.assetiq.entity.AiqAssetCatalog;
import com.applicate.services.assetiq.entity.AiqRoleConfig;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.RoleCode;
import com.applicate.services.assetiq.repository.AssetCatalogRepository;
import com.applicate.services.assetiq.repository.RoleConfigRepository;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/assets/fleet-snapshot — pre-joined, paginated, filterable fleet view.
 * See docs/backend-requirements-fleet-snapshot.md (AssetIQ-Dashboard, commit a86f460).
 */
class FleetSnapshotIT extends AbstractIntegrationTest {

    private static final String TENANT = "tenant-fleet-snapshot-it";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetCatalogRepository assetCatalogRepository;

    @Autowired
    private RoleConfigRepository roleConfigRepository;

    @Test
    void fleetSnapshotJoinsAndFiltersAndPaginates() throws Exception {
        seedCatalog();
        seedRole();

        long asset1Id = createAsset("SN-FS-1", "Fleet Snapshot Asset One", "FS-MODEL-A", "WH-1", "T1");
        long asset2Id = createAsset("SN-FS-2", "Fleet Snapshot Asset Two", "FS-MODEL-B", "WH-2", "T2");
        long asset3Id = createAsset("SN-FS-3", "Fleet Snapshot Asset Three", "FS-MODEL-A", "WH-1", "T1");

        deploy(asset3Id, "OUT-FS-1", "Outlet FS One", "T1");
        createCapture(asset3Id, "OUT-FS-1", "T1", "2026-07-01T09:00:00", "70.00", "QR", false, null);
        createCapture(asset3Id, "OUT-FS-1", "T1", "2026-07-10T09:00:00", "85.00", "BARCODE", true, "Pepsi");

        // --- totalElements matches GET /assets, unfiltered ---
        JsonNode allAssets = objectMapper.readTree(mockMvc.perform(get("/api/assets").header("X-Tenant-Id", TENANT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        JsonNode unfiltered = fleetSnapshot("size=50");
        assertThat(unfiltered.get("page").get("totalElements").asLong()).isEqualTo(allAssets.size());
        assertThat(unfiltered.get("items")).hasSize(allAssets.size());

        // --- territoryCode filter ---
        JsonNode byTerritory = fleetSnapshot("territoryCode=T1&size=50");
        assertThat(byTerritory.get("page").get("totalElements").asLong()).isEqualTo(2);

        // --- modelCode filter ---
        JsonNode byModel = fleetSnapshot("modelCode=FS-MODEL-B&size=50");
        assertThat(byModel.get("page").get("totalElements").asLong()).isEqualTo(1);
        assertThat(byModel.get("items").get(0).get("asset").get("id").asText()).isEqualTo(String.valueOf(asset2Id));

        // --- search filter (assetName, case-insensitive) ---
        JsonNode bySearch = fleetSnapshot("search=asset two&size=50");
        assertThat(bySearch.get("page").get("totalElements").asLong()).isEqualTo(1);
        assertThat(bySearch.get("items").get(0).get("asset").get("id").asText()).isEqualTo(String.valueOf(asset2Id));

        // --- assetStatus filter + currentAssociation/latestCapture join correctness ---
        JsonNode byStatus = fleetSnapshot("assetStatus=DEPLOYED&size=50");
        assertThat(byStatus.get("page").get("totalElements").asLong()).isEqualTo(1);
        JsonNode deployedItem = byStatus.get("items").get(0);
        assertThat(deployedItem.get("asset").get("id").asText()).isEqualTo(String.valueOf(asset3Id));

        JsonNode association = deployedItem.get("currentAssociation");
        assertThat(association.get("locationName").asText()).isEqualTo("Outlet FS One");
        assertThat(association.get("hasContract").asBoolean()).isTrue();
        assertThat(association.get("exclusivityFlag").asBoolean()).isTrue();
        assertThat(association.get("purityClausePct").asDouble()).isEqualTo(95.5);
        assertThat(association.get("assignmentDate").asText()).isEqualTo("2026-07-01");

        // latestCapture must reflect the LATER of the two captures, not the first one written.
        JsonNode capture = deployedItem.get("latestCapture");
        assertThat(capture.get("scanMethod").asText()).isEqualTo("BARCODE");
        assertThat(capture.get("purityPct").asDouble()).isEqualTo(85.0);
        assertThat(capture.get("competitorPresent").asBoolean()).isTrue();
        assertThat(capture.get("competitorBrand").asText()).isEqualTo("Pepsi");
        assertThat(capture.get("capturedAt").asText()).startsWith("2026-07-10T09:00");

        // --- no association / no capture -> both null, not omitted ---
        JsonNode bareItem = findItemByAssetId(unfiltered, asset1Id);
        assertThat(bareItem.get("currentAssociation").isNull()).isTrue();
        assertThat(bareItem.get("latestCapture").isNull()).isTrue();

        // --- pagination: size=1 pages cover all 3 assets exactly once, no overlap/gap ---
        java.util.Set<String> idsAcrossPages = new java.util.HashSet<>();
        for (int page = 0; page < 3; page++) {
            JsonNode onePage = fleetSnapshot("size=1&page=" + page);
            assertThat(onePage.get("items")).hasSize(1);
            assertThat(onePage.get("page").get("totalPages").asInt()).isEqualTo(3);
            idsAcrossPages.add(onePage.get("items").get(0).get("asset").get("id").asText());
        }
        assertThat(idsAcrossPages).containsExactlyInAnyOrder(
                String.valueOf(asset1Id), String.valueOf(asset2Id), String.valueOf(asset3Id));
    }

    private JsonNode fleetSnapshot(String queryString) throws Exception {
        return objectMapper.readTree(mockMvc.perform(
                        get("/api/assets/fleet-snapshot?" + queryString).header("X-Tenant-Id", TENANT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode findItemByAssetId(JsonNode snapshot, long assetId) {
        for (JsonNode item : snapshot.get("items")) {
            if (item.get("asset").get("id").asText().equals(String.valueOf(assetId))) {
                return item;
            }
        }
        throw new AssertionError("No fleet-snapshot item for asset id " + assetId);
    }

    private void seedCatalog() {
        AiqAssetCatalog category = new AiqAssetCatalog();
        category.setTenantId(TENANT);
        category.setLevel(CatalogLevel.CATEGORY);
        category.setCode("FS-COOLER");
        category.setName("FS Cooler");
        assetCatalogRepository.save(category);

        AiqAssetCatalog type = new AiqAssetCatalog();
        type.setTenantId(TENANT);
        type.setLevel(CatalogLevel.TYPE);
        type.setCode("FS-VISI");
        type.setName("FS Visi Cooler");
        type.setParentCode("FS-COOLER");
        assetCatalogRepository.save(type);

        AiqAssetCatalog modelA = new AiqAssetCatalog();
        modelA.setTenantId(TENANT);
        modelA.setLevel(CatalogLevel.MODEL);
        modelA.setCode("FS-MODEL-A");
        modelA.setName("FS Model A");
        modelA.setParentCode("FS-VISI");
        assetCatalogRepository.save(modelA);

        AiqAssetCatalog modelB = new AiqAssetCatalog();
        modelB.setTenantId(TENANT);
        modelB.setLevel(CatalogLevel.MODEL);
        modelB.setCode("FS-MODEL-B");
        modelB.setName("FS Model B");
        modelB.setParentCode("FS-VISI");
        assetCatalogRepository.save(modelB);
    }

    private void seedRole() {
        AiqRoleConfig role = new AiqRoleConfig();
        role.setTenantId(TENANT);
        role.setRoleCode(RoleCode.SALESMAN);
        role.setRoleName("Salesman");
        role.setAssetCaptureEligible(true);
        roleConfigRepository.save(role);
    }

    private long createAsset(String serialNumber, String assetName, String modelCode,
                              String warehouseCode, String territoryCode) throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"%s","assetName":"%s","categoryCode":"FS-COOLER",
                                 "typeCode":"FS-VISI","modelCode":"%s","purchaseDate":"2026-01-01",
                                 "purchasePrice":10000.00,"warehouseCode":"%s","territoryCode":"%s",
                                 "createdBy":"it-tester"}
                                """.formatted(serialNumber, assetName, modelCode, warehouseCode, territoryCode)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void deploy(long assetId, String outletCode, String outletName, String territoryCode) throws Exception {
        mockMvc.perform(post("/api/assets/" + assetId + "/deploy")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"outletCode":"%s","outletName":"%s","territoryCode":"%s","custodianName":"Custodian A",
                                 "assignmentDate":"2026-07-01","movedByUserCode":"it-tester","reason":"fleet snapshot IT setup",
                                 "hasContract":true,"exclusivityFlag":true,"purityClausePct":95.5}
                                """.formatted(outletCode, outletName, territoryCode)))
                .andExpect(status().isOk());
    }

    private void createCapture(long assetId, String outletCode, String territoryCode, String capturedAt,
                                String purityPct, String scanMethod, boolean competitorPresent, String competitorBrand) throws Exception {
        String competitorBrandJson = competitorBrand != null ? "\"" + competitorBrand + "\"" : "null";
        mockMvc.perform(post("/api/visit-captures")
                        .header("X-Tenant-Id", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"visitId":"VISIT-%s-%s","visitDate":"2026-07-01","outletCode":"%s","territoryCode":"%s",
                                 "salesmanCode":"SM-FS","assetId":%d,"roleCode":"SALESMAN","presenceStatus":"PRESENT",
                                 "scanMethod":"%s","purityPct":%s,"competitorPresent":%s,"competitorBrand":%s,
                                 "capturedAt":"%s"}
                                """.formatted(assetId, capturedAt, outletCode, territoryCode, assetId,
                                scanMethod, purityPct, competitorPresent, competitorBrandJson, capturedAt)))
                .andExpect(status().isCreated());
    }
}
