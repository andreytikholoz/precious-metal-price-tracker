package com.metal.tracker.configuration;

import com.metal.tracker.parser.BankGovUaParser;
import com.metal.tracker.parser.KitcoParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ExcelConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelConfig.class);

    @Autowired
    private BankGovUaParser bankGovUaParser;

    @Autowired
    private KitcoParser kitcoParser;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    public void scheduledTask() throws Exception {
        if (netIsAvailable()) {
            recountRate();
        } else {
            LOGGER.info("Internet is not available");
            scheduleRecountRateRetry();
        }
    }

    public boolean netIsAvailable() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    public void scheduleRecountRateRetry() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> {
            try {
                scheduledTask();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 30, TimeUnit.SECONDS);
        executor.shutdown();
    }


    public void recountRate() throws Exception {
        String filePath = "C:\\Users\\Андрей\\Desktop\\prices\\prices.xlsx";

        try (FileInputStream inputStream = new FileInputStream(filePath);
             XSSFWorkbook wb = new XSSFWorkbook(inputStream)) {
            XSSFSheet ws = wb.getSheetAt(0);

            double[] prices = kitcoParser.parse();
            double usd = bankGovUaParser.parseUSD();

            LOGGER.info("Prices in dollars: {}", Arrays.toString(prices));
            LOGGER.info("USD rate: {}", usd);

            for (int i = 0; i < prices.length; i++) {
                Row row = ws.getRow(i);
                if (row == null) {
                    row = ws.createRow(i);
                }
                Cell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                double result = Math.round((prices[i] * usd) * 100.0) / 100.0;
                cell.setCellValue(result);
                LOGGER.info("Updated cell value at row {}: {}", i, result);
            }
            Row row = ws.getRow(prices.length + 1);
            if (row == null) {
                row = ws.createRow(prices.length + 1);
            }
            Cell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            LocalDateTime currentDateTime = LocalDateTime.now();
            String currentDateAndTime = currentDateTime.format(formatter);
            cell.setCellValue(currentDateAndTime);

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                wb.write(outputStream);
            } catch (IOException e) {
                LOGGER.error("Error writing to file", e);
                throw new RuntimeException("Error writing to file", e);
            }
        } catch (IOException e) {
            LOGGER.error("Error reading file", e);
            throw new RuntimeException("Error reading file", e);
        }
    }
}
