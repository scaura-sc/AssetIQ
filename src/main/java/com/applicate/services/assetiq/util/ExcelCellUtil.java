package com.applicate.services.assetiq.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * POI's Cell API distinguishes numeric/string/blank cell types explicitly —
 * a spreadsheet author typing a plain number into what's meant to be a text
 * column (or vice versa) is common and shouldn't break parsing. Every getter
 * here tolerates that and returns null for a missing/blank cell rather than
 * throwing, so bulk-upload row parsing can treat "not provided" uniformly.
 */
public final class ExcelCellUtil {

    private ExcelCellUtil() {
    }

    public static String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        String value = cell.getCellType() == CellType.NUMERIC
                ? new BigDecimal(cell.getNumericCellValue()).stripTrailingZeros().toPlainString()
                : cell.toString().trim();
        return value.isBlank() ? null : value;
    }

    public static BigDecimal getBigDecimal(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String text = cell.toString().trim();
        return text.isBlank() ? null : new BigDecimal(text);
    }

    public static Short getShort(Row row, int col) {
        BigDecimal value = getBigDecimal(row, col);
        return value != null ? value.shortValueExact() : null;
    }

    public static LocalDate getLocalDate(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String text = cell.toString().trim();
        return text.isBlank() ? null : LocalDate.parse(text);
    }
}
