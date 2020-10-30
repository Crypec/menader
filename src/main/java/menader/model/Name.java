package menader.model;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import lombok.*;
import menader.util.*;

@Getter
@Setter
public class Name {

  public static final String FISTNAMES_DATASET_PATH = "datasets/first_names.txt";
  public static final String LASTNAMES_DATASET_PATH = "datasets/last_names.txt";

  public static List<String> FISTNAMES = new ArrayList<>();
  public static List<String> LASTNAMES = new ArrayList<>();

  private String first;
  private String last;

  static {
    try {
      FISTNAMES = Files.readAllLines(Path.of(FISTNAMES_DATASET_PATH));
      LASTNAMES = Files.readAllLines(Path.of(LASTNAMES_DATASET_PATH));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Name() {
    this.first = randomFirstName();
    this.last = randomLastName();
  }

  public static String randomFirstName() {
    int len = Name.FISTNAMES.size();
    return FISTNAMES.get(Util.randomInt(0, len - 1));
  }

  public static String randomLastName() {
    int len = Name.FISTNAMES.size();
    return FISTNAMES.get(Util.randomInt(0, len - 1));
  }

  @Override
  public String toString() {
    return String.format("%s %s", this.first, this.last);
  }
}
