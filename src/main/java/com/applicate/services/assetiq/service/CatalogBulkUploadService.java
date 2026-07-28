package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.dto.bulkupload.BulkUploadResult;
import com.applicate.services.assetiq.dto.bulkupload.RowError;
import com.applicate.services.assetiq.dto.catalog.CatalogCreateRequest;
import com.applicate.services.assetiq.entity.enums.CatalogLevel;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.exception.BadRequestException;
import com.applicate.services.assetiq.util.ExcelCellUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parses an Asset Catalog bulk-upload sheet and creates each row via the
 * existing {@link AssetCatalogService#create}, one row at a time — not
 * bundled in one transaction here (this class isn't @Transactional), so each
 * create() call runs in its own transaction via Spring's proxy and a bad row
 * doesn't roll back the rows around it. A row that fails to even parse (bad
 * enum text, non-numeric number cell, etc.) is likewise recorded as a
 * per-row error and skipped, never allowed to abort the rest of the batch.
 */
@Service
public class CatalogBulkUploadService {

    private static final String[] HEADERS = {
            "level", "code", "name", "parentCode", "description", "manufacturerName",
            "manufacturerCountry", "manufacturerContactEmail", "manufacturerContactPhone",
            "defaultWarrantyMonths", "defaultUsefulLifeYears", "defaultDepreciationMethod",
            "defaultPmFrequencyDays", "defaultPurityClausePct", "capacity", "capacityUnit"
    };

    private final AssetCatalogService assetCatalogService;

    public CatalogBulkUploadService(AssetCatalogService assetCatalogService) {
        this.assetCatalogService = assetCatalogService;
    }

    public BulkUploadResult upload(MultipartFile file) {
        List<Row> rows = readDataRows(file);
        List<RowError> errors = new ArrayList<>();
        List<ParsedRow> parsed = new ArrayList<>();

        for (Row row : rows) {
            int rowNumber = row.getRowNum() + 1;
            try {
                parsed.add(new ParsedRow(rowNumber, toRequest(row)));
            } catch (RuntimeException e) {
                errors.add(new RowError(rowNumber, e.getMessage()));
            }
        }

        // CATEGORY before TYPE before MODEL, regardless of the sheet's own row order,
        // so a child row's parentCode always resolves to an already-created parent.
        parsed.sort(Comparator.comparingInt(p -> p.request.level().ordinal()));

        int successCount = 0;
        for (ParsedRow row : parsed) {
            try {
                assetCatalogService.create(row.request);
                successCount++;
            } catch (RuntimeException e) {
                errors.add(new RowError(row.rowNumber, e.getMessage()));
            }
        }

        return new BulkUploadResult(rows.size(), successCount, errors.size(), errors);
    }

    public byte[] buildTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Asset Catalog");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build template", e);
        }
    }

    private List<Row> readDataRows(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Row> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row != null && ExcelCellUtil.getString(row, 0) != null) {
                    rows.add(row);
                }
            }
            return rows;
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file as an Excel spreadsheet: " + e.getMessage());
        }
    }

    private CatalogCreateRequest toRequest(Row row) {
        String levelText = ExcelCellUtil.getString(row, 0);
        String depreciationText = ExcelCellUtil.getString(row, 11);
        return new CatalogCreateRequest(
                levelText != null ? CatalogLevel.valueOf(levelText.trim().toUpperCase()) : null,
                ExcelCellUtil.getString(row, 1),
                ExcelCellUtil.getString(row, 2),
                ExcelCellUtil.getString(row, 3),
                ExcelCellUtil.getString(row, 4),
                ExcelCellUtil.getString(row, 5),
                ExcelCellUtil.getString(row, 6),
                ExcelCellUtil.getString(row, 7),
                ExcelCellUtil.getString(row, 8),
                ExcelCellUtil.getShort(row, 9),
                ExcelCellUtil.getShort(row, 10),
                depreciationText != null ? DepreciationMethod.valueOf(depreciationText.trim().toUpperCase()) : null,
                ExcelCellUtil.getShort(row, 12),
                ExcelCellUtil.getBigDecimal(row, 13),
                ExcelCellUtil.getBigDecimal(row, 14),
                ExcelCellUtil.getString(row, 15));
    }

    private record ParsedRow(int rowNumber, CatalogCreateRequest request) {
    }
}
