package menader.model;

import com.google.gson.*;
import java.time.LocalDate;
import java.util.*;
import lombok.*;
import menader.util.*;

@Getter
@Setter
@AllArgsConstructor
public class Child {

  private Name name;
  private String schoolName;
  private LocalDate birthDate;
  private LocalDate endOfSchool;

  public Child(LocalDate birthDate, LocalDate endOfSchool, String schoolName) {
    this.name = new Name();
    this.birthDate = birthDate;
    this.endOfSchool = endOfSchool;
  }

  public static Date generateDateInRange(Date start, Date end) {
    long startMillis = start.getTime();
    long endMillis = end.getTime();
    long randomMillisSinceEpoch = Util.randomLong(startMillis, endMillis);

    return new Date(randomMillisSinceEpoch);
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }
}
