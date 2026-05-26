package com.bijlipay.util;

import com.bijlipay.model.dto.AxisQrTransactionDto;
import com.bijlipay.model.dto.TransactionReportDto;
import com.bijlipay.model.dto.TransactionsDto;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

public class ReportUtils {
    private static final Logger log = LoggerFactory.getLogger(ReportUtils.class);

    public static Workbook createReport(String sheetName, String[] headers, List<List<Object>> rows) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        // Create header
        Row headerRow = sheet.createRow(0);
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        // Insert rows
        int i = 0;
        for (List<Object> rowItem : rows) {
            // We have already used first row for header
            Row row = sheet.createRow(i + 1);
            // Create cell and put values
            int j = 0;
            for (Object valueObject : rowItem) {
                Cell cell = row.createCell(j++);
                if (valueObject == null) {
                    cell.setCellValue("NA");
                } else if (valueObject instanceof Double) {
                    cell.setCellValue((Double) valueObject);
                } else if (valueObject instanceof BigDecimal) {
                    cell.setCellValue(((BigDecimal) valueObject).doubleValue());
                } else if (valueObject instanceof Integer) {
                    cell.setCellValue((Integer) valueObject);
                } else if (valueObject instanceof BigInteger) {
                    cell.setCellValue(((BigInteger) valueObject).intValue());
                } else if (valueObject instanceof Boolean) {
                    cell.setCellValue((Boolean) valueObject);
                } else if (valueObject instanceof Date) {
                    CellStyle cellStyle = workbook.createCellStyle();
                    CreationHelper createHelper = workbook.getCreationHelper();
                    cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue((Date) valueObject);
                } else if (valueObject instanceof Long) {
                    cell.setCellValue((Long) valueObject);
                } else {
                    cell.setCellValue(valueObject.toString());
                }
            }
            i++;
        }
        log.info("Final row count on Excel report creation : ----- : {}", i);
        // Auto format cell width
        autoSizeColumns(sheet);
        return workbook;
    }

    @Async("asyncExecutor")
    public static Workbook prepReport(List<HashMap<String, String>> mapDetailList, String sheetName, String[] headers, String[] columns) {
        log.info("===================CREATE EXCEL REPORT===================");
        Workbook workBook = null;
        try {
            workBook = new XSSFWorkbook();
            Sheet sheet = populateColumnNames(sheetName, headers, workBook);
            // Auto format cell width
            autoSizeColumns(sheet);
            int startingRowIndex = 1;
            for (HashMap<String, String> mapDetails : mapDetailList) {
                Row valueRow = sheet.createRow(startingRowIndex++);
                for (int i = 0; i < columns.length; i++) {
                    valueRow.createCell(i).setCellValue(mapDetails.get(columns[i]));
                }
            }
            log.info("===================END EXCEL REPORT===================");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workBook;
    }

    @Async("asyncExecutor")
    public static Workbook createExcel(List<TransactionsDto> transactionHistories, String sheetName, String[] columns) {
        log.info("===================CREATE EXCEL REPORT===================");
        Workbook workBook = null;
        try {
            workBook = new XSSFWorkbook();
            Sheet sheet = populateColumnNames(sheetName, columns, workBook);
            // Auto format cell width
            autoSizeColumns(sheet);
            int startingRowIndex = 1;
            for (TransactionsDto transactionsDto : transactionHistories) {
                Row valueRow = sheet.createRow(startingRowIndex++);
                int col = 0;
                valueRow.createCell(col++).setCellValue(transactionsDto.getMid());
                valueRow.createCell(col++).setCellValue(transactionsDto.getTid());
                valueRow.createCell(col++).setCellValue(transactionsDto.getTxnDate());
                valueRow.createCell(col++).setCellValue(transactionsDto.getPaymentDate());
                valueRow.createCell(col++).setCellValue(transactionsDto.getBankName());
                valueRow.createCell(col++).setCellValue(transactionsDto.getCardNo());
                valueRow.createCell(col++).setCellValue(transactionsDto.getRrn());
                valueRow.createCell(col++).setCellValue(transactionsDto.getCardType());
                valueRow.createCell(col++).setCellValue(transactionsDto.getMerchantName());
                valueRow.createCell(col++).setCellValue(transactionsDto.getTxnAmount());
                valueRow.createCell(col++).setCellValue(transactionsDto.getNetAmount());
            }
            log.info("===================END EXCEL REPORT===================");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workBook;
    }

    private static Sheet populateColumnNames(String sheetName, String[] columns, Workbook workBook) {
        Sheet sheet = workBook.createSheet(sheetName);
        Font headerFont = workBook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(headerFont);
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < columns.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(columns[col]);
            cell.setCellStyle(cellStyle);
        }
        return sheet;
    }

    private static void autoSizeColumns(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() > 0) {
            Row row = sheet.getRow(sheet.getFirstRowNum());
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                sheet.autoSizeColumn(columnIndex);
            }
        }
    }

    private static SXSSFWorkbook prepSSFBook() {
        SXSSFWorkbook wb = new SXSSFWorkbook(100); // turn off auto-flushing and accumulate all rows in memory
        wb.setCompressTempFiles(true);
        return wb;
    }

    private static Sheet prepColumnNames(String sheetName, List<String> columns, SXSSFWorkbook workBook) {
        Sheet sheet = workBook.createSheet(sheetName);
        Font headerFont = workBook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setFont(headerFont);
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < columns.size(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(columns.get(col));
            cell.setCellStyle(cellStyle);
        }
        return sheet;
    }

    public static SXSSFWorkbook transactionExcelReport(List<TransactionReportDto> qrTransactionReportDtoList, List<String> headers, String sheetName) throws IOException {
        SXSSFWorkbook wb = prepSSFBook();
        Sheet sh = prepColumnNames(sheetName, headers, wb);
        Integer rowCount = qrTransactionReportDtoList.size();
        int startRow = 1;
        log.info("The total size of the qr txn details : -- : {}", rowCount);
        for (TransactionReportDto transactionList : qrTransactionReportDtoList) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transactionList.getAsList();
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        return wb;
    }

    public static InputStreamResource posTxnExcelReport(List<TransactionReportDto> qrTransactionReportDtoList, List<String> headers, String sheetName,String filePath) throws IOException {
        SXSSFWorkbook wb = prepSSFBook();
        Sheet sh = prepColumnNames(sheetName, headers, wb);
        Integer rowCount = qrTransactionReportDtoList.size();
        int startRow = 1;
        log.info("The total size of the qr txn details : -- : {}", rowCount);
        for (TransactionReportDto transactionList : qrTransactionReportDtoList) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transactionList.getAsList();
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
            wb.write(fileOut);
            fileOut.flush();
            try (FileOutputStream file = new FileOutputStream(filePath)) {
                file.write(fileOut.toByteArray());
            }
            ByteArrayInputStream input = new ByteArrayInputStream(fileOut.toByteArray());
            InputStreamResource file = new InputStreamResource(input);
            System.out.println("Excel file written successfully to: " + filePath);
            return file;
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
            return null;
        }
//        return wb;
    }

    public static SXSSFWorkbook QrTransactionExcelReport(List<AxisQrTransactionDto> result, List<String> headers, String sheetName) {
        SXSSFWorkbook wb = prepSSFBook();
        Sheet sh = prepColumnNames(sheetName, headers, wb);
        Integer rowCount = result.size();
        int startRow = 1;
        log.info("The total size of the Axis Qr Txn details : -- : {}", rowCount);
        for (AxisQrTransactionDto transactionList : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transactionList.getAsList();
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        return wb;
    }

    public static InputStreamResource QrTxnReport(List<AxisQrTransactionDto> result, List<String> headers, String sheetName, String filePath) {
        SXSSFWorkbook wb = prepSSFBook();
        Sheet sh = prepColumnNames(sheetName, headers, wb);
        Integer rowCount = result.size();
        int startRow = 1;
        log.info("The total size of the Axis Qr Txn details : -- : {}", rowCount);
        for (AxisQrTransactionDto transactionList : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transactionList.getAsList();
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
            wb.write(fileOut);
            fileOut.flush();
            try (FileOutputStream file = new FileOutputStream(filePath)) {
                file.write(fileOut.toByteArray());
            }
            ByteArrayInputStream input = new ByteArrayInputStream(fileOut.toByteArray());
            InputStreamResource file = new InputStreamResource(input);
            System.out.println("Excel file written successfully to: " + filePath);
            return file;
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
            return null;
        }
//        return wb;
    }

    @Async
    public static void writeFile(XSSFWorkbook wb, String filePath){
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            wb.write(fileOut);
            fileOut.flush();
            System.out.println("Excel file written successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        }
    }

    public static void generateMissingAxisDataInCSVHeader(String[] staticTxnHeader, String updatedFile, String updatedSheet) {
        log.info("Creating CSV File -----{}", updatedSheet);
        try {
            //creating csv file
            CSVWriter csvWriter = new CSVWriter(new FileWriter(updatedFile, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
            csvWriter.writeNext(staticTxnHeader);
            csvWriter.close();
            log.info("CSV File Generated Successfully----!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateMissingAxisDataInCSV(AxisQrTransactionDto qrTransactionDto, String updatedFile, String updatedSheet) {
        log.info("Creating CSV File -----{}", updatedSheet);
        try {
            //creating csv file
            CSVWriter csvWriter = new CSVWriter(new FileWriter(updatedFile, true), ',', CSVWriter.NO_QUOTE_CHARACTER);
            List<String[]> finalOut = new ArrayList<>();
            String[] out = null;
            String customerVpa = qrTransactionDto.getCustomerVpa();
            String creditVpa = qrTransactionDto.getCreditVpa();
            String merchantId = qrTransactionDto.getMerchantId();
            String merchantChannelId = qrTransactionDto.getMerchantChannelId();
            String merchantTransactionId = qrTransactionDto.getMerchantTransactionId();
            String transactionTimestamp = qrTransactionDto.getTransactionTimestamp();
            String transactionAmount = qrTransactionDto.getTransactionAmount();
            String gatewayTransactionId = qrTransactionDto.getGatewayTransactionId();
            String gatewayResponseCode = qrTransactionDto.getGatewayResponseCode();
            String gatewayResponseMessage = qrTransactionDto.getGatewayResponseMessage();
            String rrn = qrTransactionDto.getRrn();
            String checksum = qrTransactionDto.getChecksum();
            String payeractype = qrTransactionDto.getPayeractype();
            String purposeCode = qrTransactionDto.getPurposeCode();
            String initMode = qrTransactionDto.getInitMode();
            out = Arrays.asList(customerVpa, creditVpa, merchantId, merchantChannelId, merchantTransactionId, transactionTimestamp, transactionAmount, gatewayTransactionId, gatewayResponseCode, gatewayResponseMessage, rrn, checksum, payeractype, purposeCode, initMode).toArray(new String[0]);
            finalOut.add(out);
            csvWriter.writeAll(finalOut);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

