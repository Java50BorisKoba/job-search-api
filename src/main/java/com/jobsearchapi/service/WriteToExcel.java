package com.jobsearchapi.service;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class WriteToExcel {
    public static final String FILE_PATH = "C:\\Users\\97253\\Desktop\\jobLink.xlsx";
    private static Workbook workbook = null;
    private static Sheet sheet = null;

    public static void writeToExcel(Map<String, List<String>> jobDetails, String websiteName) {
        fileOperations();
        writingToFile(jobDetails, websiteName);
        closeResources();
    }

    private static void writingToFile(Map<String, List<String>> jobDetails, String websiteName) {
        int rowIndex = sheet.getLastRowNum() + 1;

        for (Map.Entry<String, List<String>> entry : jobDetails.entrySet()) {
            String url = entry.getKey();
            List<String> details = entry.getValue();

            if (details.size() >= 4) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(""); // Blank column
                row.createCell(1).setCellValue(getCurrentDate());
                row.createCell(2).setCellValue(websiteName);
                row.createCell(3).setCellValue(details.get(0)); // Title
                row.createCell(4).setCellValue(details.get(2)); // Company
                row.createCell(5).setCellValue(details.get(3)); // City

                // Create hyperlink for the URL
                Cell linkCell = row.createCell(6);
                Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
                hyperlink.setAddress(url);
                linkCell.setHyperlink(hyperlink);
                linkCell.setCellValue(url);

                row.createCell(7).setCellValue(details.get(1)); // Description
                row.createCell(8).setCellValue(" ");
            } else {
                System.err.println("Insufficient data for URL: " + url);
            }
        }
        writeChangesToFile();
    }

    private static void writeChangesToFile() {
        try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fileOperations() {
        try {
            if (Files.exists(Paths.get(FILE_PATH))) {
                // Проверяем, что файл не пустой
                if (Files.size(Paths.get(FILE_PATH)) > 0) {
                    // Файл существует и не пуст, читаем его
                    try (FileInputStream fis = new FileInputStream(FILE_PATH)) {
                        workbook = new XSSFWorkbook(fis);
                        sheet = workbook.getSheetAt(0);
                    }
                } else {
                    // Файл пустой, пересоздаем его
                    createFile();
                    writeChangesToFile(); // Записываем начальную структуру в файл
                }
            } else {
                // Файл не существует, создаем новый
                createFile();
                writeChangesToFile(); // Записываем начальную структуру в файл
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFile() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Positions");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue(""); // Blank column
        headerRow.createCell(1).setCellValue("Date");
        headerRow.createCell(2).setCellValue("Source");
        headerRow.createCell(3).setCellValue("Position");
        headerRow.createCell(4).setCellValue("Company");
        headerRow.createCell(5).setCellValue("Location");
        headerRow.createCell(6).setCellValue("Link");
        headerRow.createCell(7).setCellValue("Description");

        // Set column widths
        sheet.setColumnWidth(0, 1000);
        sheet.setColumnWidth(3, 12000);
        sheet.setColumnWidth(4, 10000);
        sheet.setColumnWidth(5, 10000);
        sheet.setColumnWidth(6, 10000);
        sheet.setColumnWidth(7, 20000);
    }

    public static String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.now().format(formatter);
    }

    private static void closeResources() {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}