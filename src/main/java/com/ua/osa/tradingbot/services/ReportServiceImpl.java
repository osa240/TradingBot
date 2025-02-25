package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.TradingRecord;
import com.ua.osa.tradingbot.models.dto.TradingRecordOrderBook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final TelegramBotService telegramBotService;
    private final String reportPath = "D:\\report.xlsx";
    private final String reportPathOrderBook = "D:\\reportOB.xlsx";

    @Override
    public void generateReportStrategyStatistic(Map<Integer, List<TradingRecord>> statisticMap) {
        String[] columns = {"Стратегія",
                "Час входу",
                "Час виходу",
                "Ціна входу",
                "Ціна виходу",
                "Різниця"};
        try (Workbook workbook = new XSSFWorkbook()) {
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Sheet sheet = workbook.createSheet("Strategy Statistic");

            // Створюємо заголовок
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Заповнюємо данними
            int rowIdx = 1;

            for (int i = 0; i < statisticMap.size(); i++) {
                List<TradingRecord> tradingRecords = statisticMap.get(i);
                if (CollectionUtils.isNotEmpty(tradingRecords)) {
                    if (tradingRecords.getLast().getPriceClose() == null) {
                        tradingRecords.remove(tradingRecords.size() - 1);
                    }
                    for (TradingRecord tradingRecord : tradingRecords) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(tradingRecord.getIndicator().name());
                        row.createCell(1).setCellValue(DateFormatUtils.format(
                                tradingRecord.getTimestampOpen(), "HH:mm:ss dd:MM:YYYY")
                        );
                        row.createCell(2).setCellValue(DateFormatUtils.format(
                                tradingRecord.getTimestampClose(), "HH:mm:ss dd:MM:YYYY")
                        );
                        row.createCell(3).setCellValue(tradingRecord.getPriceOpen().doubleValue());
                        row.createCell(4).setCellValue(
                                tradingRecord.getPriceClose().doubleValue()
                        );
                        row.createCell(5).setCellValue(tradingRecord.getDif().doubleValue());
                    }
                }
            }

            // Зберігаємо файл
            Path path = Paths.get(reportPath);
            try (FileOutputStream fileOut = new FileOutputStream(path.toFile())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        telegramBotService.sendFileToUser(new File(reportPath), "Статистика згенерована");
    }

    @Override
    public void generateReportStrategyStatistic(List<TradingRecordOrderBook> statistic) {
        String[] columns = {"Час входу",
                "Ціна входу",
                "Об'єм BID при купівлі",
                "Об'єм ASK при продажу",
                "Час виходу",
                "Ціна виходу",
                "Об'єм BID при продажу",
                "Об'єм ASK при продажу",
                "Різниця"};
        try (Workbook workbook = new XSSFWorkbook()) {
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Sheet sheet = workbook.createSheet("OrderBook Statistic");

            // Створюємо заголовок
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Заповнюємо данними
            int rowIdx = 1;

            for (TradingRecordOrderBook tradingRecord : statistic) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(DateFormatUtils.format(
                        tradingRecord.getTimestampOpen(), "HH:mm:ss dd:MM:YYYY")
                );
                row.createCell(1).setCellValue(tradingRecord.getPriceOpen().doubleValue());
                row.createCell(2).setCellValue(tradingRecord.getBidsAmountOpen().doubleValue());
                row.createCell(3).setCellValue(tradingRecord.getAsksAmountOpen().doubleValue());

                row.createCell(4).setCellValue(DateFormatUtils.format(
                        tradingRecord.getTimestampClose(), "HH:mm:ss dd:MM:YYYY")
                );
                row.createCell(5).setCellValue(tradingRecord.getPriceClose().doubleValue());
                row.createCell(6).setCellValue(tradingRecord.getBidsAmountClose().doubleValue());
                row.createCell(7).setCellValue(tradingRecord.getAsksAmountClose().doubleValue());

                row.createCell(8).setCellValue(tradingRecord.getDif().doubleValue());
            }

            // Зберігаємо файл
            Path path = Paths.get(reportPathOrderBook);
            try (FileOutputStream fileOut = new FileOutputStream(path.toFile())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        telegramBotService.sendFileToUser(new File(reportPathOrderBook), "Статистика згенерована");
    }
}
