package menader.model;

import lombok.*;
import menader.util.*;

@Data
public class AHV {

  private long nr;
  public static final long CH_COUNTRY_CODE = 756;

  public AHV() {
    this.nr = generateRandomAHV(CH_COUNTRY_CODE);
  }

  public AHV(long cc) {
    this.nr = generateRandomAHV(cc);
  }

  public static long generateRandomAHV(long cc) {
    val personID = Util.randomLong(100000000, 999999999);
    String idStr = Long.toString(personID);
    String csStr = Long.toString(calculateAHVChecksum(cc, personID));
    String ccStr = Long.toString(cc);
    return Long.parseLong(ccStr + idStr + csStr);
  }

  public static int calculateAHVChecksum(long countryCode, long personID) {
    val idStr = Long.toString(personID);
    int acc = 0;
    int multiplier = 3;

    for (int i = idStr.length() - 1; i > 0; i--) {
      val val = Character.getNumericValue(idStr.charAt(i));
      acc += val * multiplier;
      multiplier = multiplier == 3 ? 1 : 3;
    }

    val ccStr = Long.toString(countryCode);

    acc += Character.getNumericValue(ccStr.charAt(0));
    acc += Character.getNumericValue(ccStr.charAt(1)) * 3;
    acc += Character.getNumericValue(ccStr.charAt(2));

    val csStr = Long.toString(acc);
    int lastIndex = csStr.length() - 1;
    return Character.getNumericValue(csStr.charAt(lastIndex));
  }

  @Override
  public String toString() {
    return Long.toString(this.nr);
  }

  public String formatWithDots() {
    var digits = Long.toString(this.nr);
    return String.format(
        "%s.%s.%s.%s",
        digits.substring(0, 3),
        digits.substring(3, 7),
        digits.substring(7, 11),
        digits.substring(11, 13));
  }

  public String formatedWithoutDots() {
    return Long.toString(this.nr);
  }
}
