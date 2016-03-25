package com.sonalake.utah.config;

import com.sonalake.utah.Parser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * A test of the parser
 */
public class ParserTest {
  private Config config;

  @Before
  public void setup() {
    config = new Config();
    config.delimiters = new ArrayList<Delimiter>();
    config.searches = new ArrayList<NameValue>();
    config.values = new ArrayList<ValueRegex>();
    config.headers = new ArrayList<ValueRegex>();
  }

  /**
   * Test the parser works when we have multiple, multiple-line records
   */
  @Test
  public void testPerRecordParser() {
    addPerRecordDelimiter("DELIM");
    addValue("field", "field: (\\d*)");

    FileBuilder file = new FileBuilder();
    file.addLine("a line")
      .addLine("another line")
      .addLine("field: 1234")
      .addLine("DELIM")
      .addLine("hmmm line")
      .addLine("field: 4567");

    List<Map<String, String>> results = loadFile(file);

    List<Map<String, String>> expectedValues = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("field", "1234");
      }});
      add(new TreeMap<String, String>() {{
        put("field", "4567");
      }});
    }};

    assertEquals(expectedValues, results);
  }

  /**
   * Test we can do per line parsing
   */
  @Test
  public void testPerLineParser() {
    addPerLineDelimiter();
    addSearch("text", "(\\S*)");
    addValue("entry1", 1, "a line with some {text} and {text}");
    addValue("entry2", 2, "a line with some {text} and {text}");


    FileBuilder file = new FileBuilder();
    file.addLine("a line with some cats and dogs")
      .addLine("a line with some argy and bargy");

    List<Map<String, String>> results = loadFile(file);

    List<Map<String, String>> expectedValues = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("entry1", "cats");
        put("entry2", "dogs");
      }});
      add(new TreeMap<String, String>() {{
        put("entry1", "argy");
        put("entry2", "bargy");
      }});
    }};

    assertEquals(expectedValues, results);
  }

  /**
   * Test we can do per line parsing, wher we pull values from a header, and insert them into each entry we find
   */
  @Test
  public void testPerLineParserWithHeader() {
    addPerLineDelimiter();
    addSearch("text", "(\\S*)");
    addHeaderDelimiter("^THE HEADER DELIM.*");
    addHeaderValue("header", "header value: {text}");
    addValue("entry1", 1, "a line with some {text} and {text}");
    addValue("entry2", 2, "a line with some {text} and {text}");


    FileBuilder file = new FileBuilder();
    file
      .addLine("header value: ahoy")
      .addLine("THE HEADER DELIM")
      .addLine("a line with some cats and dogs")
      .addLine("a line with some argy and bargy");

    List<Map<String, String>> results = loadFile(file);

    List<Map<String, String>> expectedValues = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("header", "ahoy");
        put("entry1", "cats");
        put("entry2", "dogs");
      }});
      add(new TreeMap<String, String>() {{
        put("header", "ahoy");
        put("entry1", "argy");
        put("entry2", "bargy");
      }});
    }};

    assertEquals(expectedValues, results);
  }

  /**
   * Test a case where we have multiple delimiters. This is useful in cases where the structure of the record is
   * different
   */
  @Test
  public void testMultipleDelimiters() {
    addSearch("text", "(\\S*)");
    addSearch("EOL", "[\\n|\\r]");

    addPerRecordDelimiter("a value {text} {text}");
    addRetainedPerRecordDelimiter("another value {text}");

    // here we have multiple values using the same key, so we use the last one that matches a value
    addValue("entry1", "a value {text}");
    addValue("entry2", 2, "a value {text} {text}");
    addValue("entry2", 1, "another value {text}");

    FileBuilder file = new FileBuilder();
    file
      .addLine("a value inlineA inlineB")
      .addLine("a value multilineA")
      .addLine("another value multilineB");

    List<Map<String, String>> results = loadFile(file);

    List<Map<String, String>> expectedValues = new ArrayList<Map<String, String>>() {{
      add(new TreeMap<String, String>() {{
        put("entry1", "inlineA");
        put("entry2", "inlineB");
      }});
      add(new TreeMap<String, String>() {{
        put("entry1", "multilineA");
        put("entry2", "multilineB");
      }});
    }};

    assertEquals(expectedValues, results);
  }

  private void addHeaderDelimiter(String s) {
    config.headerDelimiter = new HeaderDelimiter();
    config.headerDelimiter.delimiter = s;
  }

  private List<Map<String, String>> loadFile(FileBuilder file) {
    config.compilePatterns();
    Parser parser = Parser.parse(config, file.buildReader());

    List<Map<String, String>> results = new ArrayList<Map<String, String>>();

    while (true) {
      Map<String, String> record = parser.next();
      if (null == record) {
        break;
      } else {
        results.add(record);
      }
    }
    return results;
  }

  private void addSearch(String id, String pattern) {
    config.searches.add(new NameValue(id, pattern));
  }

  private void addValue(String id, int group, String pattern) {
    ValueRegex regex = new ValueRegex(id, pattern);
    regex.group = group;
    config.values.add(regex);
  }

  private void addValue(String id, String pattern) {
    addValue(id, 1, pattern);
  }

  private void addHeaderValue(String id, String pattern) {
    ValueRegex regex = new ValueRegex(id, pattern);
    config.headers.add(regex);
  }

  private Delimiter addPerRecordDelimiter(String delim) {
    Delimiter delimiter = new Delimiter();
    delimiter.delimiter = delim;
    config.delimiters.add(delimiter);
    return delimiter;
  }

  private void addRetainedPerRecordDelimiter(String delim) {
    Delimiter delimiter = addPerRecordDelimiter(delim);
    delimiter.isRetainDelim = true;
  }

  private void addPerLineDelimiter() {
    Delimiter delimiter = new Delimiter();
    delimiter.isPerLine = true;
    config.delimiters.add(delimiter);
  }

}
