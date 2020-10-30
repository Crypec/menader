package menader.model;

import com.google.gson.*;
import java.util.*;
import lombok.*;
import org.iban4j.*;

@Data
public class Security {

  private Stock stock;
  private Iban iban;

  public Security(String countryCode) {
    // FIXME(Simon): Iban4j uses country codes from the 3166-1 ISO Standart.
    // FIXME(Simon): But the taxation files use ISO 3166-2.
    // FIXME(Simon): ISO 3166-1 expects: USA
    // FIXME(Simon): ISO 3166-2 expects: US
    // FIXME(Simon): This is just a quickfix to get things working. We need a better solution for
    // the long term.
    this.stock = new Stock();

    String[] unsupportedCountries = {
      "US", "TW", "JP", "CA", "AU",
    };
    boolean containsUnsupported = Arrays.stream(unsupportedCountries).anyMatch(countryCode::equals);
    if (containsUnsupported) {
      this.iban = Iban.random(CountryCode.CH);
      return;
    }
    var cc = CountryCode.getByCode(countryCode);
    this.iban = Iban.random(cc);
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }
}
