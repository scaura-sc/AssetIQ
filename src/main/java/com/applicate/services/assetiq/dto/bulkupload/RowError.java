package com.applicate.services.assetiq.dto.bulkupload;

/** rowNumber is 1-based and counts the header row, matching what a spreadsheet
 * user sees on-screen (row 1 = header, so the first data row is 2). */
public record RowError(int rowNumber, String message) {
}
