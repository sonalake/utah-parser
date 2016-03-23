package com.sonalake.utah.config;

import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A header delimiter - used to identify the header where there are values in the header of the file that are to be
 * added to every record.
 */
public class HeaderDelimiter {

  /**
   * Raw delimiter string from the config file
   */
  @XmlValue
  protected String delimiter;

  /**
   * The compiled pattern, this is the one used at runtime
   */
  private Pattern compiledPattern;

  public boolean matches(String candidate) {
    return compiledPattern.matcher(candidate).matches();
  }

  /**
   * Compile the delimiter based on the searches
   *
   * @param searches the searches, processed in this order
   */
  void compile(List<NameValue> searches) {
    String valueText = SearchHelper.translate(delimiter, searches);
    compiledPattern = Pattern.compile(".*?" + valueText + ".*?");

  }
}
