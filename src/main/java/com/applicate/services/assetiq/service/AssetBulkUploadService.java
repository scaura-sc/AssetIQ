package com.applicate.services.assetiq.service;

import com.applicate.services.assetiq.dto.asset.AssetCreateRequest;
import com.applicate.services.assetiq.dto.bulkupload.BulkUploadResult;
import com.applicate.services.assetiq.dto.bulkupload.RowError;
import com.applicate.services.assetiq.entity.enums.DepreciationMethod;
import com.applicate.services.assetiq.entity.enums.WarrantyType;
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
import java.util.List;

/**
 * Parses an Assets bulk-upload sheet and registers each row via the existing
 * {@link AssetService#create}, one row at a time from this non-@Transactional
 * class — each create() call gets its own transaction via Spring's proxy, so
 * a bad row doesn't roll back the rows around it. Rows are independent (no
 * hierarchy/ordering concern, unlike the catalog upload).
 */
@Service
public class AssetBulkUploadService {

    private static final String[] HEADERS = {
            "serialNumber", "assetName", "categoryCode", "typeCode", "modelCode", "vendorCode",
            "brandCode", "divisionCode", "companyCode", "capacity", "capacityUnit", "colour",
            "purchaseDate", "purchasePrice", "purchaseOrderRef", "invoiceRef", "manufacturingDate",
            "warrantyStartDate", "warrantyEndDate", "warrantyType", "amcStartDate", "amcEndDate",
            "amcVendorCode", "depreciationMethod", "usefulLifeYears", "residualValue",
            "warehouseCode", "territoryCode"
    };

    private final AssetService assetService;

    public AssetBulkUploadService(AssetService assetService) {
        this.assetService = assetService;
    }

    public BulkUploadResult upload(MultipartFile file, String createdBy) {
        List<Row> rows = readDataRows(file);
        List<RowError> errors = new ArrayList<>();
        int successCount = 0;

        for (Row row : rows) {
            int rowNumber = row.getRowNum() + 1;
            try {
                assetService.create(toRequest(row, createdBy));
                successCount++;
            } catch (RuntimeException e) {
                errors.add(new RowError(rowNumber, e.getMessage()));
            }
        }

        return new BulkUploadResult(rows.size(), successCount, errors.size(), errors);
    }

    public byte[] buildTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Assets");
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

    private AssetCreateRequest toRequest(Row row, String createdBy) {
        String warrantyTypeText = ExcelCellUtil.getString(row, 19);
        String depreciationText = ExcelCellUtil.getString(row, 23);
        return new AssetCreateRequest(
                ExcelCellUtil.getString(row, 0),
                ExcelCellUtil.getString(row, 1),
                ExcelCellUtil.getString(row, 2),
                ExcelCellUtil.getString(row, 3),
                ExcelCellUtil.getString(row, 4),
                ExcelCellUtil.getString(row, 5),
                ExcelCellUtil.getString(row, 6),
                ExcelCellUtil.getString(row, 7),
                ExcelCellUtil.getString(row, 8),
                ExcelCellUtil.getBigDecimal(row, 9),
                ExcelCellUtil.getString(row, 10),
                ExcelCellUtil.getString(row, 11),
                ExcelCellUtil.getLocalDate(row, 12),
                ExcelCellUtil.getBigDecimal(row, 13),
                ExcelCellUtil.getString(row, 14),
                ExcelCellUtil.getString(row, 15),
                ExcelCellUtil.getLocalDate(row, 16),
                ExcelCellUtil.getLocalDate(row, 17),
                ExcelCellUtil.getLocalDate(row, 18),
                warrantyTypeText != null ? WarrantyType.valueOf(warrantyTypeText.trim().toUpperCase()) : null,
                ExcelCellUtil.getLocalDate(row, 20),
                ExcelCellUtil.getLocalDate(row, 21),
                ExcelCellUtil.getString(row, 22),
                depreciationText != null ? DepreciationMethod.valueOf(depreciationText.trim().toUpperCase()) : null,
                ExcelCellUtil.getShort(row, 24),
                ExcelCellUtil.getBigDecimal(row, 25),
                ExcelCellUtil.getString(row, 26),
                ExcelCellUtil.getString(row, 27),
                createdBy);
    }
}
