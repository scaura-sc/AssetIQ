package com.applicate.services.assetiq.dto.serviceevent;

import java.time.LocalDateTime;

/**
 * F17 — wraps a service event with SLA fields derived at read time (never persisted):
 * CRITICAL=4h, HIGH=24h, MEDIUM=72h, LOW=7 days from raised_at, compared against
 * resolved_at if already resolved, otherwise now().
 */
public record ServiceEventDashboardItem(ServiceEventResponse event, LocalDateTime slaDueAt, boolean slaBreached) {
}
