package com.applicate.services.assetiq.dto.fleet;

import java.util.List;

public record FleetSnapshotResponse(List<FleetSnapshotItem> items, PageMeta page) {
}
