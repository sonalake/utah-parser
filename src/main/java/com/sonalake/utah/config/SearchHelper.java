package com.sonalake.utah.config;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A helper to deal with searches
 */
public class SearchHelper {

  /**
   * Apply the search translations to the candidate string
   *
   * @param candidate the candidate
   * @param searches
   * @return the fully translated text
   */
  static String translate(String candidate, List<NameValue> searches) {
    String valueText = candidate;
    if (null != searches) {
      for (NameValue search : searches) {
        String formattedId = String.format("{%s}", search.getId());
        valueText = StringUtils.replace(valueText, formattedId, search.getValue());
      }
    }
    return valueText;
  }

}
