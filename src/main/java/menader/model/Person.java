package menader.model;

import java.time.LocalDate;
import lombok.Data;
import menader.util.*;

@Data
public class Person {
  private Name name;
  private AHV ahv;
  private LocalDate birthDate;
  private Address addr;

  public Person() {
    this.name = new Name();
    this.ahv = new AHV();
    this.birthDate = Util.randomDate(Constants.LOWER_DATE_BOUND, Constants.UPPER_DATE_BOUND);
    this.addr = Address.random();

    // FIXME(Simon): I could not find a decent dataset which correlates zip codes with BFS numbers
    // so I am going to use these known working values for now
    this.addr.town = "Kreuzlingen";
    this.addr.zipCode = 8280;
    this.addr.municipalityID = 4671;
  }
}
