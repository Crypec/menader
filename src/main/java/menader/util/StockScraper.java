package menader.util;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import lombok.*;
import menader.model.*;

public class StockScraper {
  private static final String URL = "http://www.isinlei.com/ISIN/Prefix/recent?page=";

  private static final String ISIN_XPATH = "//table[@class='PopupTable highlight']//td[1]//text()";
  private static final String ISSUERS_XPATH =
      "//table[@class='PopupTable highlight']//td[2]//text()";
  private static final String TYPES_XPATH = "//table[@class='PopupTable highlight']//td[3]//text()";

  private static final int NUM_STOCK_PAGES = 100;

  public static List<Stock> getStockData() throws Exception {
    val stocks = new ArrayList<Stock>();
    for (int i = 0; i < NUM_STOCK_PAGES; i++) {
      stocks.addAll(getPageData(i));
    }
    return stocks;
  }

  public static List<Stock> getPageData(int pageIndex) throws Exception {
    val client = new WebClient();
    client.getOptions().setCssEnabled(false);
    client.getOptions().setJavaScriptEnabled(false);

    HtmlPage page = client.getPage(URL + pageIndex);

    var rawIsins = page.getByXPath(ISIN_XPATH);
    var rawIssuers = page.getByXPath(ISSUERS_XPATH);
    var rawSecTypes = page.getByXPath(TYPES_XPATH);

    var cleanISINS = removeEmpty(rawIsins);
    var cleanIssuers = removeEmpty(rawIssuers);
    var cleanSecTypes = removeEmpty(rawSecTypes);

    // NOTE(Simon): remove row with navigation buttons
    cleanISINS.remove(cleanISINS.size() - 1);
    cleanISINS.remove(cleanISINS.size() - 1);
    cleanISINS.remove(cleanISINS.size() - 1);

    cleanIssuers.remove(cleanIssuers.size() - 1);

    // NOTE(Simon): For some reason the length of the individual columns differs of the src page
    // NOTE(Simon): differs. This means we just use the smallest possible length and disregard any
    // NOTE(Simon): data which is not aligned by the rest of the table.
    int len = Math.min(Math.min(cleanISINS.size(), cleanIssuers.size()), cleanSecTypes.size());

    val stocks = new ArrayList<Stock>();
    for (int i = 0; i < len; i++) {
      String isin = cleanISINS.get(i);
      String issuer = cleanIssuers.get(i);
      String secType = cleanSecTypes.get(i);
      stocks.add(new Stock(isin, issuer, secType));
    }
    return stocks;
  }

  public static <T> List<String> removeEmpty(List<T> arr) {
    var out = new ArrayList<String>();
    for (int i = 0; i < arr.size(); i++) {
      String elem = arr.get(i).toString();
      if (!elem.isBlank() && !elem.isBlank()) {
        out.add(elem);
      }
    }
    return out;
  }

  public static void writeToFile(String path) throws Exception {

    new File(path).createNewFile();
    var sb = new StringBuilder();
    val stocks = StockScraper.getStockData();
    for (val stock : stocks) {
      sb.append(stock.toCSV());
    }
    Files.write(Path.of(path), sb.toString().getBytes());
  }
}
