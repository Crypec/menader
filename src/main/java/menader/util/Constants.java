package menader.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;

public class Constants {
    public static final LocalDate UPPER_DATE_BOUND = LocalDate.of(2000, Month.JANUARY, 1);
    public static final LocalDate LOWER_DATE_BOUND = LocalDate.of(1940, Month.JANUARY, 1);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
