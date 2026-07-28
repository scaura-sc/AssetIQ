package com.applicate.services.assetiq.dto.bulkupload;

import java.util.List;

/** Always returned with 200, whether every row succeeded or some failed —
 * partial success is the point of a bulk upload, never an all-or-nothing rollback. */
public record BulkUploadResult(int totalRows, int successCount, int failureCount, List<RowError> errors) {
}
