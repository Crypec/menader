package menader.lib;

import com.google.gson.*;
import java.util.*;
import lombok.*;
import menader.model.*;
import org.dom4j.Document;
import org.iban4j.Iban;

@Data
public class Marshaller {

  public Marshaller(Document doc) {
    this.primary = new Person(doc);
    this.partner = new Person(doc);
    this.bankName = "TestBank";
  }

  private Person primary;
  private Person partner;

  private String bankName;
  private Iban iban;

  private List<Child> children = new ArrayList<>();
  private List<Security> securities = new ArrayList<>();

  public void add(Security sec) {
    this.securities.add(sec);
  }

  public void add(Child child) {
    this.children.add(child);
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }
}
