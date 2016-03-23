package com.sonalake.utah.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
   * @throws JAXBException
   */
  public Config loadConfig(URL url) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(Config.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    Config config = (Config) unmarshaller.unmarshal(url);
    validate(config);
    return config;
  }

  /**
   * Load a config from a reader
   *
   * @param reader the source of the config
   * @return the populated config
   * @throws JAXBException
   */
  public Config loadConfig(Reader reader) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(Config.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    Config config = (Config) unmarshaller.unmarshal(reader);
    validate(config);
    return config;
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
