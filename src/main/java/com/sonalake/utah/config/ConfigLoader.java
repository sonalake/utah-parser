package com.sonalake.utah.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Load a config from a source file
 */
public class ConfigLoader {

  /**
   * Load a config from a URL
   *
   * @param url the location of the config
   * @return the populated config
   * @throws IOException should the file fail to load or be parseable
   */
  public Config loadConfig(URL url) throws IOException {
    try (Reader reader = new InputStreamReader(url.openStream())) {
      return loadConfig(reader);
    }
  }

  /**
   * Load a config from a reader
   *
   * @param reader the source of the config
   * @return the populated config
   * @throws IOException should the file fail to load or be parseable
   */
  public Config loadConfig(Reader reader) throws IOException {
    Config config = buildReader().readValue(reader, Config.class);;
    validate(config);
    return config;
  }

  private XmlMapper buildReader() {
    XmlMapper mapper = new XmlMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false);
    return mapper;
  }
  /**
   * Validate the config is ok
   *
   * @param config
   */
  void validate(Config config) {
    if (!config.isDelimiterValid()) {
      throw new IllegalArgumentException(String.format("No delimited defined for config: %s ", config));
    }
    config.compilePatterns();
  }

}
