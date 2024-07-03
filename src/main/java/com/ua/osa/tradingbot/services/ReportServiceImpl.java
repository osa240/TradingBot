package com.ua.osa.tradingbot.services;

import com.ua.osa.tradingbot.models.dto.TradingRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final TelegramBotService telegramBotService;
    private final String reportPath = "D:\\report.xlsx";

    @Override
    public void generateReportStrategyStatistic(Map<Integer, List<TradingRecord>> statisticMap) {
        String[] columns = {"Стратегия", "Время входа", "Время выхода","Цена входа", "Цена выхода", "Разница"};
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Strategy Statistic");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.RED.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Создаем заголовок
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Заполняем данными
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
                        row.createCell(1).setCellValue(tradingRecord.getTimestampOpen());
                        row.createCell(2).setCellValue(tradingRecord.getTimestampClose());
                        row.createCell(3).setCellValue(tradingRecord.getPriceOpen().doubleValue());
                        row.createCell(4).setCellValue(tradingRecord.getPriceClose().doubleValue());
                        row.createCell(5).setCellValue(tradingRecord.getDif().doubleValue());
                    }
                }
            }

            // Сохраняем файл
            Path path = Paths.get(reportPath);
            try (FileOutputStream fileOut = new FileOutputStream(path.toFile())) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        telegramBotService.sendMessageToUser("Статистика згенерирована");
    }
}
