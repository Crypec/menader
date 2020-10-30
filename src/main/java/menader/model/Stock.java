package menader.model;

import com.google.gson.*;
import java.io.FileReader;
import java.util.*;
import lombok.*;
import menader.util.*;
import org.apache.commons.csv.*;

@Data
@AllArgsConstructor
public class Stock {
  private String ISIN;
  private String issuer;
  private String type;

  private static final List<Stock> STOCKS = new ArrayList<>();
  private static final String STOCK_DATASET_PATH = "./datasets/stock_data.csv";

  static {
    try {
      var in = new FileReader(STOCK_DATASET_PATH);
      for (CSVRecord record : CSVFormat.DEFAULT.parse(in)) {
        val stock = new Stock(record.get(0), record.get(1), record.get(2));
        STOCKS.add(stock);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Stock() {
    var stock = STOCKS.get(Util.randomInt(0, STOCKS.size() - 1));
    this.ISIN = stock.getISIN();
    this.issuer = stock.getIssuer();
    this.type = stock.getType();
  }

  public String toCSV() {
    return String.format("%s,%s,%s%n", this.ISIN, this.issuer, this.type);
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }
}
