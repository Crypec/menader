package menader.util;

import com.google.gson.*;
import java.io.Serializable;
import java.util.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

@Getter
@Setter
public abstract class Snippet implements Serializable {

  public static final int LOWER_BOUND = 999;

  private StringBuilder lexeme = new StringBuilder();

  public abstract String shuffle();

  public abstract String toIR();

  public void add(char c) {
    this.lexeme.append(c);
  }

  @Override
  public String toString() {
    return this.getClass()
        + new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Text extends Snippet {
    @Override
    public String shuffle() {
      return RandomStringUtils.randomAlphabetic(super.lexeme.length());
    }

    @Override
    public String toIR() {
      return "T(" + super.getLexeme().length() + ")";
    }
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Num extends Snippet {
    @Override
    public String shuffle() {
      long num = Long.parseLong(super.lexeme.toString());
      long lower = (num - LOWER_BOUND);
      long min = (lower > 0) ? lower : 1;
      long rand = randIntRange(min, num);

      int orgLen = super.getLexeme().length();
      String strNum = Long.toString(rand);

      for (int i = 0; i < (orgLen - strNum.length()); i++) {
        strNum = '0' + strNum;
      }
      return strNum;
    }

    private static long randIntRange(long min, long max) {
      return min + (long) (Math.random() * (max - min));
    }

    @Override
    public String toIR() {
      return "N(" + super.getLexeme().length() + ")";
    }
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Filler extends Snippet {
    @Override
    public String shuffle() {
      return super.getLexeme().toString();
    }

    @Override
    public String toIR() {
      return "F(" + super.getLexeme() + ")";
    }
  }
}
