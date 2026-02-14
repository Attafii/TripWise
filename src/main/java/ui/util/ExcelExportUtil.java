package ui.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExcelExportUtil - Export TableView data to Excel
 * Uses Apache POI for Excel generation
 */
public class ExcelExportUtil {

    /**
     * Export TableView to Excel
     * @param tableView The TableView to export
     * @param filePath Output Excel file path
     * @param sheetName Sheet name
     * @return true if export successful
     */
    public static <T> boolean exportToExcel(TableView<T> tableView, String filePath, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // Create alternate row style
            CellStyle alternateRowStyle = workbook.createCellStyle();
            alternateRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            alternateRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Get columns
            ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).getText());
                cell.setCellStyle(headerStyle);
            }
            
            // Add data rows
            ObservableList<T> items = tableView.getItems();
            for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                T item = items.get(rowIndex);
                
                for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object cellData = columns.get(colIndex).getCellData(item);
                    
                    if (cellData != null) {
                        if (cellData instanceof Number) {
                            cell.setCellValue(((Number) cellData).doubleValue());
                        } else {
                            cell.setCellValue(cellData.toString());
                        }
                    }
                    
                    // Alternate row coloring
                    if (rowIndex % 2 == 1) {
                        cell.setCellStyle(alternateRowStyle);
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            System.out.println("✅ Excel exported successfully: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error exporting to Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export TableView to Excel with auto-generated filename
     */
    public static <T> boolean exportToExcel(TableView<T> tableView, String sheetName) {
        String filename = ExportUtil.generateFilename(sheetName.replaceAll("\\s+", "_"), "xlsx");
        String filePath = ExportUtil.getDefaultExportDir() + java.io.File.separator + filename;
        
        return exportToExcel(tableView, filePath, sheetName);
    }
}
