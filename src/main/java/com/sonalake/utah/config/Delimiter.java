package com.sonalake.utah.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Used to split up files into jobs
 */
public class Delimiter {

  /**
   * If true, then the entire file is considered to be one record
   */
  @JacksonXmlProperty(isAttribute = true, localName = "one-record")
  boolean isOneRecord;

  /**
   * If true, then the records are split per line
   */
  @JacksonXmlProperty(isAttribute = true, localName = "per-line")
  boolean isPerLine;

  /**
   * If true, then the delimiter is considered part of the record
   */
  @JacksonXmlProperty(isAttribute = true, localName = "retain")
  boolean isRetainDelim;

  /**
   * If true, then the delimiter is at the start of the record (by default, it's at the end)
   */
  @JacksonXmlProperty(isAttribute = true, localName = "at-start")
  boolean isDelimAtStartOfRecord;

  /**
   * The text value of the delimiter
   */
  @JacksonXmlText
  String delimiter;

  /**
   * This is compiled once so we can reuse it.
   */
  private Pattern compiledDelimiter;

  /**
   * Compile the pattern, based on the configured searches
   *
   * @param searches the searches
   */
  void compile(List<NameValue> searches) {
    if (null != delimiter && null == compiledDelimiter) {
      String valueText = SearchHelper.translate(delimiter, searches);
      compiledDelimiter = Pattern.compile(valueText);
    }
  }

  /**
   * Check if the candidate matches the rules for the delimiter.
   *
   * @param candidate The candidate line
   * @return true, if it matches the candidate
   */
  public boolean matches(String candidate) {
    // if the file is one record, then we want the entire file,
    // if it's per line, then we match each line
    // otherwise, we check the delimiter
    if (isOneRecord) {
      return false;
    } else if (isPerLine) {
      return StringUtils.isNotBlank(candidate);
    } else {
      return StringUtils.isNotBlank(candidate) && compiledDelimiter.matcher(candidate).matches();
    }
  }

  /**
   * This is true if the delimiter text value is required
   *
   * @return false if the entire file is one record, or if the processing is per line
   */
  boolean isDelimRequired() {
    if (isOneRecord || isPerLine) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * In some cases the record delimiter contains the record data, in which case we need to retain it when parsing.
   *
   * @return true if we need to retain the delimiter, false otherwise.
   */
  public boolean isRetainDelim() {
    return isRetainDelim || isDelimAtStartOfRecord();
  }


  public boolean isDelimAtStartOfRecord() {
    return isDelimAtStartOfRecord;
  }

}
