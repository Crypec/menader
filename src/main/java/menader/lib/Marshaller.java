package menader.lib;

import menader.model.*;
import com.google.gson.*;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import menader.util.Constants;
import menader.util.Util;
import org.checkerframework.checker.units.qual.A;
import org.iban4j.Iban;

@Data
public class Marshaller {

	public Marshaller() {
		this.name = new Name();
		this.birthDate = Util.randomDate(1940, 2001);
		this.ahv = new AHV();
		this.partnerAHV = new AHV();
		this.bankName = "TestBank INC.";
		this.iban = Iban.random();
		this.address = Address.random();
	}


  private Name name;
  private LocalDate birthDate;

  private Address address;

  private String bankName;
  private Iban iban;

  // FIXME(Simon): we need to store more than one ahv number.
  private AHV ahv;
  private AHV partnerAHV;
  private List<Name> names = new ArrayList<>();

  private List<Child> children = new ArrayList<>();
  private List<Security> securities = new ArrayList<>();

  public void add(Security sec) {
	this.securities.add(sec);
  }

  public void add(Child child) {
	this.children.add(child);
  }

  public void add(Name name) {
	this.names.add(name);
  }

  @Override
  public String toString() {
	return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }
}