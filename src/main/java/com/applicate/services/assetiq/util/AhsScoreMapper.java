package com.applicate.services.assetiq.util;

import com.applicate.services.assetiq.entity.enums.ConditionGrade;
import com.applicate.services.assetiq.entity.enums.PresenceStatus;
import com.applicate.services.assetiq.entity.enums.WorkingStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Maps the categorical signals a visit capture reports (presence, condition,
 * working status) onto the same 0-100 scale purity_pct already uses, so
 * AhsCalculationService can combine all 4 AHS components with one formula.
 */
public final class AhsScoreMapper {

    private static final Map<PresenceStatus, BigDecimal> PRESENCE_SCALE = Map.of(
            PresenceStatus.PRESENT, new BigDecimal("100.00"),
            PresenceStatus.PARTIAL, new BigDecimal("50.00"),
            PresenceStatus.NOT_FOUND, BigDecimal.ZERO);

    private static final Map<ConditionGrade, BigDecimal> CONDITION_SCALE = Map.of(
            ConditionGrade.EXCELLENT, new BigDecimal("100.00"),
            ConditionGrade.GOOD, new BigDecimal("75.00"),
            ConditionGrade.FAIR, new BigDecimal("50.00"),
            ConditionGrade.POOR, new BigDecimal("25.00"),
            ConditionGrade.SCRAP, BigDecimal.ZERO);

    private static final Map<WorkingStatus, BigDecimal> WORKING_SCALE = Map.of(
            WorkingStatus.WORKING, new BigDecimal("100.00"),
            WorkingStatus.PARTIAL, new BigDecimal("50.00"),
            WorkingStatus.NOT_WORKING, BigDecimal.ZERO);

    private AhsScoreMapper() {
    }

    public static BigDecimal mapPresence(PresenceStatus status) {
        return PRESENCE_SCALE.get(status);
    }

    public static BigDecimal mapCondition(ConditionGrade grade) {
        return CONDITION_SCALE.get(grade);
    }

    public static BigDecimal mapWorking(WorkingStatus status) {
        return WORKING_SCALE.get(status);
    }
}
