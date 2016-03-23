package com.sonalake.utah.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * A config for a name/value pair
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NameValue {

  /**
   * The name of the id
   */
  @XmlAttribute
  private String id;

  /**
   * The value of the mapping
   */
  @XmlValue
  private String value;

  public String getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

  /**
   * Default constructor (needed for jaxb)
   */
  public NameValue() {

  }

  /**
   * @param id    The name of the id
   * @param value The value of the mapping
   */
  NameValue(String id, String value) {
    this();
    this.id = id;
    this.value = value;
  }

  @Override
  public String toString() {
    return String.format("'%s' => '%s'", id, value);
  }

}
