package com.metal.tracker.parser;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class BankGovUaParser {
    public double parseUSD() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String currentDate = dateFormat.format(new Date());
        String apiUrl = "https://bank.gov.ua/NBU_Exchange/exchange?date=" + currentDate;

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(connection.getInputStream());


        NodeList rows = doc.getElementsByTagName("ROW");

        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);
            String currencyCodeL = row.getElementsByTagName("CurrencyCodeL").item(0).getTextContent();
            if (currencyCodeL.equals("USD")) {
                String amount = row.getElementsByTagName("Amount").item(0).getTextContent();
                return Double.parseDouble(amount);
            }

        }
        throw new IllegalArgumentException("USD rate couldn't find");
    }
}
