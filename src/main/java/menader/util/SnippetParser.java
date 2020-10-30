package menader.util;

import java.util.*;

public class SnippetParser {
  private String buffer = "";
  private int cursor = 0;

  public SnippetParser(String buffer) {
    this.buffer = buffer;
  }

  public List<Snippet> parse() {
    if (this.buffer == null) {
      return new ArrayList<>();
    }
    var snippets = new ArrayList<Snippet>();
    while (this.hasNext()) {
      snippets.add(this.parseSnippet());
    }
    return snippets;
  }

  private Snippet parseSnippet() {
    char c = this.peek();
    if (Character.isLetter(c)) {
      return this.parseTextSnippet();
    } else if (Character.isDigit(c)) {
      return this.parseNumSnippet();
    } else {
      return this.parseFillerSnippet();
    }
  }

  private Snippet.Text parseTextSnippet() {
    var snippet = new Snippet.Text();
    do {
      snippet.add(this.next());
    } while (Character.isLetter(this.peek()));
    return snippet;
  }

  private Snippet parseNumSnippet() {
    var snippet = new Snippet.Num();
    do {
      snippet.add(this.next());
    } while (this.isDigit());
    return snippet;
  }

  private Snippet parseFillerSnippet() {
    var snippet = new Snippet.Filler();
    do {
      snippet.add(this.next());
    } while (!this.isDigit() && !this.isLetter());
    return snippet;
  }

  private boolean hasNext() {
    return this.cursor < this.buffer.length();
  }

  private char next() {
    return this.buffer.charAt(this.cursor++);
  }

  private boolean isDigit() {
    return Character.isDigit(this.peek());
  }

  private boolean isLetter() {
    return Character.isLetter(this.peek());
  }

  private char peek() {
    if (this.hasNext()) return this.buffer.charAt(this.cursor);
    return 0;
  }
}
