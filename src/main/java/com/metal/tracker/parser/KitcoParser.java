package com.metal.tracker.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KitcoParser {

    public double[] parse() throws IOException {
        Document doc = Jsoup
                .connect("https://www.kitco.com/price/precious-metals")
                .get();

        // Извлечение данных для золота
        Element goldElement = doc.selectFirst(".BidAskGrid_gridifier__l1T1o span:contains(Gold)");
        String goldPrice = goldElement.nextElementSibling().nextElementSibling().nextElementSibling().text();
        double goldPriceGram = Double.parseDouble(goldPrice.replace(",", "")) / 31.103035;


        // Извлечение данных для серебра
        Element silverElement = doc.selectFirst(".BidAskGrid_gridifier__l1T1o span:contains(Silver)");
        String silverPrice = silverElement.nextElementSibling().nextElementSibling().nextElementSibling().text();
        double silverPriceGram = Double.parseDouble(silverPrice.replace(",", "")) / 31.103035;

        // Извлечение данных для платины
        Element platinumElement = doc.selectFirst(".BidAskGrid_gridifier__l1T1o span:contains(Platinum)");
        String platinumPrice = platinumElement.nextElementSibling().nextElementSibling().nextElementSibling().text();
        double platinumPriceGram = Double.parseDouble(platinumPrice.replace(",", "")) / 31.103035;

        // Извлечение данных для палладиума
        Element palladiumElement = doc.selectFirst(".BidAskGrid_gridifier__l1T1o span:contains(Palladium)");
        String palladiumPrice = palladiumElement.nextElementSibling().nextElementSibling().nextElementSibling().text();
        double palladiumPriceGram = Double.parseDouble(palladiumPrice.replace(",", "")) / 31.103035;

        // Извлечение данных для родиума
        Element rhodiumElement = doc.selectFirst(".BidAskGrid_gridifier__l1T1o span:contains(Rhodium)");
        String rhodiumPrice = rhodiumElement.nextElementSibling().nextElementSibling().nextElementSibling().text();
        double rhodiumPriceGram = Double.parseDouble(rhodiumPrice.replace(",", "")) / 31.103035;


        return new double[]{goldPriceGram, silverPriceGram, platinumPriceGram, palladiumPriceGram, rhodiumPriceGram};
    }
}
