package menader.util;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.dom4j.*;
import org.dom4j.io.*;

public class Util {

  public static Document readDocument(Path path) throws DocumentException, IOException {
	return new SAXReader().read(path.toString());
  }

  public static Document readDocument(String path) throws DocumentException, IOException {
	var reader = new SAXReader();
	reader.setEncoding("UTF-8");
	return reader.read(path);
  }

  public static void writeDocument(Document doc, String path) throws IOException {
		var format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		XMLWriter writer = new XMLWriter(new FileWriter(path), format);
		writer.write(doc);
		writer.close();
  }

  public static long randomLong(long min, long max) {
	return ThreadLocalRandom.current().nextLong(min, max);
  }

  public static int randomInt(int min, int max) {
	return ThreadLocalRandom.current().nextInt(min, max);
  }

  public static LocalDate randomDate(LocalDate lower, LocalDate upper) {
	long startEpochDay = lower.toEpochDay();
	long endEpochDay = upper.toEpochDay();
	long randomDay = randomLong(startEpochDay, endEpochDay);
	return LocalDate.ofEpochDay(randomDay);
  }

  public static LocalDate randomDate(int startYear, int endYear) {
	int day = randomInt(1, 28);
	int month = randomInt(1, 12);
	int year = randomInt(startYear, endYear);
	return LocalDate.of(year, month, day);
  }

  public static void replaceWithSnippet(Node n) {
	var snippet =
		new SnippetParser(n.getText())
			.parse().stream().map(Snippet::shuffle).collect(Collectors.joining());
	n.setText(snippet);
  }
}
