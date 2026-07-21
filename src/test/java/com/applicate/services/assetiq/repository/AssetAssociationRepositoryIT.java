package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetAssociation;
import com.applicate.services.assetiq.entity.enums.AssignmentType;
import com.applicate.services.assetiq.entity.enums.DepositStatus;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AssetAssociationRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AssetAssociationRepository assetAssociationRepository;

    @Test
    void persistsAssociationWithContractTerms() {
        AiqAssetAssociation association = new AiqAssetAssociation();
        association.setTenantId("tenant-1");
        association.setAssetId(1001L);
        association.setAssetNumber("AST-0001");
        association.setLocationType(LocationType.OUTLET);
        association.setLocationCode("OUT-100");
        association.setLocationName("MG Road Outlet");
        association.setAssignmentDate(LocalDate.of(2025, 2, 1));
        association.setAssignmentType(AssignmentType.PERMANENT);
        association.setHasContract(true);
        association.setContractRef("CTR-001");
        association.setDepositAmount(new BigDecimal("5000.00"));
        association.setDepositStatus(DepositStatus.PAID);
        association.setPurityClausePct(new BigDecimal("95.00"));
        association.setContractStartDate(LocalDate.of(2025, 2, 1));
        association.setContractEndDate(LocalDate.of(2026, 2, 1));
        association.setCreatedBy("system");

        AiqAssetAssociation saved = assetAssociationRepository.saveAndFlush(association);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getExclusivityFlag()).isFalse();

        Optional<AiqAssetAssociation> active =
                assetAssociationRepository.findByTenantIdAndAssetIdAndIsActiveTrue("tenant-1", 1001L);
        assertThat(active).isPresent();
        assertThat(active.get().getDepositStatus()).isEqualTo(DepositStatus.PAID);
    }

    @Test
    void findsHistoryByAssetAndByLocation() {
        AiqAssetAssociation association = new AiqAssetAssociation();
        association.setTenantId("tenant-2");
        association.setAssetId(2002L);
        association.setAssetNumber("AST-0002");
        association.setLocationType(LocationType.DISTRIBUTOR);
        association.setLocationCode("DIST-5");
        association.setAssignmentDate(LocalDate.of(2025, 3, 1));
        association.setCreatedBy("system");
        assetAssociationRepository.saveAndFlush(association);

        List<AiqAssetAssociation> history = assetAssociationRepository.findByTenantIdAndAssetId("tenant-2", 2002L);
        assertThat(history).hasSize(1);

        List<AiqAssetAssociation> atLocation =
                assetAssociationRepository.findByTenantIdAndLocationTypeAndLocationCode("tenant-2", LocationType.DISTRIBUTOR, "DIST-5");
        assertThat(atLocation).hasSize(1);
    }
}
