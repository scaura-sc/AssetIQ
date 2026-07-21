package com.applicate.services.assetiq.repository;

import com.applicate.services.assetiq.entity.AiqAssetMovementLog;
import com.applicate.services.assetiq.entity.enums.LocationType;
import com.applicate.services.assetiq.entity.enums.MovementType;
import com.applicate.services.assetiq.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssetMovementLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AssetMovementLogRepository assetMovementLogRepository;

    @Test
    void persistsMovementWithNullFromLocationForFirstAssignment() {
        AiqAssetMovementLog movement = new AiqAssetMovementLog();
        movement.setTenantId("tenant-1");
        movement.setAssetId(1001L);
        movement.setAssetNumber("AST-0001");
        movement.setMovementType(MovementType.ASSIGN);
        movement.setToLocationType(LocationType.OUTLET);
        movement.setToLocationCode("OUT-100");
        movement.setMovedByUserCode("user-1");
        movement.setMovedAt(LocalDateTime.of(2025, 2, 1, 10, 0));

        AiqAssetMovementLog saved = assetMovementLogRepository.saveAndFlush(movement);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFromLocationType()).isNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findsHistoryByAssetNewestFirstAndByMovementType() {
        AiqAssetMovementLog first = new AiqAssetMovementLog();
        first.setTenantId("tenant-2");
        first.setAssetId(2002L);
        first.setAssetNumber("AST-0002");
        first.setMovementType(MovementType.ASSIGN);
        first.setToLocationType(LocationType.OUTLET);
        first.setToLocationCode("OUT-1");
        first.setMovedByUserCode("user-1");
        first.setMovedAt(LocalDateTime.of(2025, 1, 1, 9, 0));
        assetMovementLogRepository.saveAndFlush(first);

        AiqAssetMovementLog second = new AiqAssetMovementLog();
        second.setTenantId("tenant-2");
        second.setAssetId(2002L);
        second.setAssetNumber("AST-0002");
        second.setMovementType(MovementType.TRANSFER);
        second.setFromLocationType(LocationType.OUTLET);
        second.setFromLocationCode("OUT-1");
        second.setToLocationType(LocationType.OUTLET);
        second.setToLocationCode("OUT-2");
        second.setMovedByUserCode("user-2");
        second.setMovedAt(LocalDateTime.of(2025, 2, 1, 9, 0));
        assetMovementLogRepository.saveAndFlush(second);

        List<AiqAssetMovementLog> history =
                assetMovementLogRepository.findByTenantIdAndAssetIdOrderByMovedAtDesc("tenant-2", 2002L);
        assertThat(history).extracting(AiqAssetMovementLog::getMovementType)
                .containsExactly(MovementType.TRANSFER, MovementType.ASSIGN);

        List<AiqAssetMovementLog> transfers =
                assetMovementLogRepository.findByTenantIdAndMovementType("tenant-2", MovementType.TRANSFER);
        assertThat(transfers).hasSize(1);
    }
}
