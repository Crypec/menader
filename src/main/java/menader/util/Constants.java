package menader.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

public class Constants {
  public static final LocalDate UPPER_DATE_BOUND = LocalDate.of(2000, Month.JANUARY, 1);
  public static final LocalDate LOWER_DATE_BOUND = LocalDate.of(1940, Month.JANUARY, 1);
  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd");
}
