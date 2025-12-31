package com.innoura.legalEase.service;

import com.innoura.legalEase.dto.FileContainerDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class ContentExtractionService
{

    public String extractExcelAsString(FileContainerDto fileContainer) throws IOException {
        StringBuilder extractedData = new StringBuilder();

        // Get the InputStream from FileContainerDto byte array
        InputStream inputStream = new ByteArrayInputStream(fileContainer.getFileByte());

        // Create Workbook from InputStream
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Get the first sheet (assuming the Excel file contains only one sheet)
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through each row in the sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Iterate through each cell in the row
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    // Get the value of the cell based on its type
                    String cellValue = getCellValueAsString(cell);

                    // Append the cell value to the StringBuilder with pipe separator
                    extractedData.append(cellValue).append(" | ");
                }

                // Append '|||' after each row to mark the end of the row
                extractedData.append("|||").append("\n");
            }
        } catch (Exception e) {
            // Handle exceptions (e.g., corrupt file, invalid format)
            throw new IOException("Failed to read Excel file", e);
        }

        // Return the extracted data as a String
        return extractedData.toString().trim(); // Remove the last newline
    }

    // Helper method to extract value as string from cell
    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "N/A";
        }
    }

    public String extractPdfAsString(FileContainerDto fileContainer) throws IOException {
        StringBuilder extractedText = new StringBuilder();

        // Get the byte array from FileContainerDto
        byte[] fileBytes = fileContainer.getFileByte();

        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            String text = pdfTextStripper.getText(document);
            extractedText.append(text);

        } catch (Exception e) {
            throw new IOException("Failed to read PDF file", e);
        }

        return extractedText.toString().trim();
    }

}
