package menader.model;

import java.time.LocalDate;
import lombok.Data;
import menader.util.*;
import org.dom4j.Document;

@Data
public class Person {
  private Name name;
  private AHV ahv;
  private LocalDate birthDate;
  private Address addr;

  public Person(Document doc) {
    this.name = new Name();
    this.ahv = new AHV();
    this.birthDate = Util.randomDate(Constants.LOWER_DATE_BOUND, Constants.UPPER_DATE_BOUND);
    this.addr = Address.random();

    var townNode = doc.selectSingleNode(XPath.descendantOrSelf("Gemeindename"));
    if (townNode != null) {
      this.addr.town = townNode.getText();
    }
    var zipCodeNode = doc.selectSingleNode(XPath.descendantOrSelf("PLZ"));
    if (zipCodeNode != null) {
      this.addr.zipCode = Integer.parseInt(zipCodeNode.getText());
    }

    var municipalityNode = doc.selectSingleNode(XPath.descendantOrSelf("BFS_GemeindeNr"));
    if (municipalityNode != null) {
      this.addr.municipalityID = Integer.parseInt(municipalityNode.getText());
    }
  }
}
