package com.sonalake.utah.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test of the configuration classes
 */
public class ConfigTests {

  /**
   * We build up an XML doc in this and then generate it into a string before
   * parsing it with the config
   */
  private Document document;

  @Before
  public void setup() throws ParserConfigurationException {
    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
  }

  /**
   * If the config has no delimiter, then fail out
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConfigHasNoDelimiter() throws TransformerException, IOException {
    createEmptyDocument();
    new ConfigLoader().loadConfig(buildDocReader());
  }

  /**
   * If the config has values with no groups, then fail out
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConfigMappingHasNoGroup() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addSearch("number", "\\d*");
    addValue("value", "{number}");
    new ConfigLoader().loadConfig(buildDocReader());

  }

  /**
   * If the config has values with no groups, then fail out
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConfigMappingWhenInvalidRegex() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addSearch("number", "(\\d*)");
    addValue("value", "{number}(");
    new ConfigLoader().loadConfig(buildDocReader());
  }

  /**
   * Create a valid document that can handle values and then confirm it with parsing
   */
  @Test
  public void testValidValueConfigOk() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addSearch("number", "(\\d*)");
    addValue("value", "a value {number}");
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Map<String, String> record = config.buildRecord("this is a value 123 hello");
    assertEquals("123", record.get("value"));
  }

  /**
   * Create a valid document that can handle values and then confirm it with parsing
   */
  @Test
  public void testValidHeaderValueConfigOk() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addSearch("number", "(\\d*)");
    addHeaderValue("value", "a value {number}");
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Map<String, String> record = config.buildHeader("this is a value 123 hello");
    assertEquals("123", record.get("value"));
  }

  /**
   * Create a valid document that can handle values and then confirm it with parsing
   */
  @Test
  public void testHeaderDelimiterValueConfigOk() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addHeaderDelimiter("HEADER-DELIM");
    Config config = new ConfigLoader().loadConfig(buildDocReader());
    assertTrue(config.matchesHeaderDelim("HEADER-DELIM"));
    assertFalse(config.matchesHeaderDelim("something else"));

  }

  @Test
  public void testPerLine() throws TransformerException, IOException {
    createEmptyDocument();
    addPerLineDelimiter();
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Delimiter delimiter = config.delimiters.get(0);
    Assert.assertNotNull(delimiter);
    assertTrue(delimiter.isPerLine);
    assertTrue(delimiter.matches("a line"));
    assertFalse(delimiter.matches(""));
  }

  @Test
  public void testRetainDelim() throws TransformerException, IOException {
    createEmptyDocument();
    Element delimiterNode = addDelimiter("a line");
    delimiterNode.setAttribute("retain", "true");
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Delimiter delimiter = config.delimiters.get(0);
    Assert.assertNotNull(delimiter);
    assertTrue(delimiter.isRetainDelim());
    assertTrue(delimiter.isRetainDelim);
  }

  @Test
  public void testDelimAtStart() throws TransformerException, IOException {
    createEmptyDocument();
    Element delimiterNode = addDelimiter("a line");
    delimiterNode.setAttribute("at-start", "true");
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Delimiter delimiter = config.delimiters.get(0);
    Assert.assertNotNull(delimiter);
    assertTrue(delimiter.isDelimAtStartOfRecord());

    // here we check if the retain delim  field is false
    // but the start of record is true, that the retain delim
    // operation will still return true
    assertTrue(delimiter.isRetainDelim());
    assertFalse(delimiter.isRetainDelim);
  }

  /**
   * Create a valid document that can handle headers and then confirm it with parsing
   */
  @Test
  public void testValidHeaderConfigOk() throws TransformerException, IOException {
    createEmptyDocument();
    addDelimiter("DELIM");
    addSearch("number", "(\\d*)");
    addHeaderValue("header", "a header {number}");
    Config config = new ConfigLoader().loadConfig(buildDocReader());

    Map<String, String> header = config.buildHeader("this is a header 999y hello");
    assertEquals("999", header.get("header"));
  }

  /**
   * Add a search to the config
   * @param id the id
   * @param regex the regex
   */
  private void addSearch(String id, String regex) {
    Element groupNode = findGroupNode("searches");
    createElementInGroup(groupNode, "search", id, regex);
  }

  /**
   * Add a value to the config
   * @param id the id
   * @param regex the regex
   */
  private void addValue(String id, String regex) {
    Element groupNode = findGroupNode("values");
    createElementInGroup(groupNode, "value", id, regex);
  }

  /**
   * Add a header value to the config
   * @param id the id
   * @param regex the regex
   */
  private void addHeaderValue(String id, String regex) {
    Element groupNode = findGroupNode("header");
    createElementInGroup(groupNode, "value", id, regex);
  }

  /**
   * Create and add an element in the group
   * @param groupNode the group node
   * @param elementName the new element's name
   * @param id the id
   * @param regex the regex
   */
  private void createElementInGroup(Element groupNode, String elementName, String id, String regex) {
    Element elementNode = document.createElement(elementName);
    elementNode.setAttribute("id", id);
    elementNode.setTextContent(regex);
    groupNode.appendChild(elementNode);
  }

  /**
   * Find a group under the config node - add it if it's not there
   * @param group the group name
   * @return the group element
   */
  private Element findGroupNode(String group) {
    Element configNode = findConfigNode();
    Element groupNode;
    NodeList groupNodes = configNode.getElementsByTagName(group);
    if (null == groupNodes || groupNodes.getLength() == 0) {
      groupNode = document.createElement(group);
      configNode.appendChild(groupNode);
    } else {
      groupNode = (Element) groupNodes.item(0);
    }
    return groupNode;
  }

  /**
   * Build a Reader for the document - this will contain the XML doc for the parser
   * @return the reader
   * @throws TransformerException
   */
  private Reader buildDocReader() throws TransformerException {
    // use the DOM writing tools to write the XML to this document
    StringWriter writer = new StringWriter();
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();

    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(writer);
    transformer.transform(source, result);

    // return this as a reader
    return new StringReader(writer.toString());
  }

  /**
   * Create the empty document with just the config node in there
   * @return the config element
   */
  private Element createEmptyDocument() {
    Element element = document.createElement("config");
    document.appendChild(element);
    return element;
  }

  /**
   * Create and add a delimiter element to the config node
   * @return the empty delimiter
   */
  private Element addDelimiterElement() {
    Element element = findConfigNode();
    Element newChild = document.createElement("delim");
    element.appendChild(newChild);
    return newChild;
  }

  /**
   * Add a delimiter to the config node
   * @param delim the regex for the delim
   */
  private Element addDelimiter(String delim) {
    Element newChild = addDelimiterElement();
    newChild.setTextContent(delim);
    return newChild;
  }

  private void addPerLineDelimiter() {
    Element newChild = addDelimiterElement();
    newChild.setAttribute("per-line", "true");
  }

  private Element addHeaderDelimiter(String delim) {
    Element element = findConfigNode();
    Element newChild = document.createElement("header-delim");
    element.appendChild(newChild);
    newChild.setTextContent(delim);
    return element;
  }

  /**
   * Find the config node in the document
   * @return the config node
   */
  private Element findConfigNode() {
    String name = "config";
    NodeList nodes = document.getElementsByTagName(name);
    return (Element) nodes.item(0);
  }

}
