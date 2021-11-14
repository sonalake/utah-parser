package com.sonalake.utah.cli;

import com.google.gson.GsonBuilder;
import com.sonalake.utah.Parser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CommandLineInterfaceTest {

    @Test
    public void testLogHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("utah", new CommandLineInterface().buildOptions());
    }

    @Test
    public void testRequiredArgsMissing() {
        Throwable exception = Assertions.assertThrows(ParseException.class, () -> generateCommandline(""));
        Assertions.assertTrue(exception.getMessage().contains("No configuration file supplied"));
    }

    /*
     * Tests CSV Output format is read correctly from command line
     */
    @Test
    public void testArgCsv() throws ParseException {
        CLIConfig config = generateCommandline(" -o csv -f config.xml");

        Assertions.assertAll(
                () -> Assertions.assertEquals(CLIConfig.Format.CSV, config.getFormat()),
                () -> Assertions.assertEquals("config.xml", config.getPathToConfig()));
    }

    /*
     * Tests JSON Output format is read correctly from command line
     */
    @Test
    public void testArgJson() throws ParseException {
        CLIConfig config = generateCommandline(" -o json -f config.xml");

        Assertions.assertAll(
                () -> Assertions.assertEquals(CLIConfig.Format.JSON, config.getFormat()),
                () -> Assertions.assertEquals("config.xml", config.getPathToConfig()));
    }

    /*
     * Tests that the default output format is CSV
     */
    @Test
    public void testArgDefaultToCSV() throws ParseException {
        CLIConfig config = generateCommandline(" -f config.xml");

        Assertions.assertAll(
                () -> Assertions.assertEquals(CLIConfig.Format.CSV, config.getFormat()),
                () -> Assertions.assertEquals("config.xml", config.getPathToConfig()));
    }

    /*
     * Tests to assure invalid formats don't work
     */
    @Test
    public void testArgBadFormat() {
        Throwable exception = Assertions.assertThrows(ParseException.class, () -> generateCommandline(" -o monkey -f config.xml"));
        Assertions.assertTrue(exception.getMessage().contains("monkey is not a valid format"));
    }

    /*
     * Tests that GSON is working
     */
    @Test
    public void testPrintOutputJSON() {
        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
        Map<String, String> map = new TreeMap<String, String>();
        map.put("age", "23");
        map.put("name", "23");
        map.put("gender", "23");
        mapList.add(map);
        Assertions.assertTrue(CommandLineInterface.mapListToJSON(mapList).contains("\"age\": \"23\","));
    }

    /*
     * Tests that the JSON output is the correct format
     */
    @Test
    public void testJsonFormat() {
        CommandLineInterface iface = new CommandLineInterface();
        OutputHelper helper  = new OutputHelper();


        Parser parser = mock(Parser.class);
        Map<String, String>[] parsedRecords  = new MapHelper()
                .newMap()
                .put("a", "hello")
                .put("b", null)
                .newMap()
                .put("b", "out")
                .put("c", "there")
                .toArray();

        when(parser.next()).thenReturn(parsedRecords[0], parsedRecords[1], null);

        iface.printToJSON(parser, helper.target);
        String expected = new GsonBuilder().setPrettyPrinting().create().toJson(parsedRecords);
        String observed  =helper.getOutputAsString();

        Assertions.assertEquals(expected, observed);
    }

    private class OutputHelper {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream target = new PrintStream(baos, true);

        public String getOutputAsString() {
            return  new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }
    /*
     * Tests that the CSV Output is in the correct format
     */
    @Test
    public void testCsvFormat() throws IOException {

        CommandLineInterface iface = new CommandLineInterface();
        OutputHelper helper  = new OutputHelper();

        Parser parser = mock(Parser.class);
        when(parser.next()).thenReturn(
                new TreeMap<String, String>() {{
                    put("a", "hello");
                    put("b", "out");
                    put("c", "there");
                }},
                new TreeMap<String, String>() {{
                    put("a", "space");
                    put("d", "monster");
                    put("f", "thing");
                }},
                null
        );

        iface.printToCSV(parser, helper.target);

        String content = helper.getOutputAsString();

        List<List<String>> expected = new ArrayList<List<String>>();
        expected.add(Arrays.asList("a", "b", "c", "d", "f"));
        expected.add(Arrays.asList("hello", "out", "there", "", ""));
        expected.add(Arrays.asList("space", "", "", "monster", "thing"));


        assertCsvContent(content, expected);
    }

    @Test
    public void testHelpOutputForNoArgs() {
        CommandLineInterface iface = new CommandLineInterface();
        OutputHelper helper  = new OutputHelper();
        iface.processArgs(new String[]{},
                new StringReader("some input file \n with another line"),
                helper.target);

        Assertions.assertTrue(helper.getOutputAsString().contains("usage: "),
                String.format("Wrong message: %s", helper.getOutputAsString()));
    }

    /**
     * The real parsing is tested elsewhere, here we just test for the flow through the CLI
     * @throws ParseException
     * @throws FileNotFoundException
     */
    @Test
    public void testBranchingForCsv() throws ParseException, IOException {
        CLIConfig.Format format  = CLIConfig.Format.CSV;

        OutputHelper helper = processMockOutput(format);

        Assertions.assertTrue(helper.getOutputAsString().contains("xyz"),
                String.format("Wrong message:\n%s", helper.getOutputAsString()));
    }

    @Test
    public void testBranchingForWrongFormat() throws ParseException, IOException {
        CLIConfig.Format format  = CLIConfig.Format.CSV;

        OutputHelper helper = processMockOutput(format);

        Assertions.assertTrue(helper.getOutputAsString().contains("xyz"),
                String.format("Wrong message:\n%s", helper.getOutputAsString()));
    }

    /**
     * The real parsing is tested elsewhere, here we just test for the flow through the CLI
     * @throws ParseException
     * @throws FileNotFoundException
     */
    @Test
    public void testBranchingForJson() throws ParseException, IOException {
        CLIConfig.Format format = CLIConfig.Format.JSON;

        OutputHelper helper = processMockOutput(format);

        Assertions.assertTrue(helper.getOutputAsString().contains("\"a\": \"xyz\""),
                String.format("Wrong message:\n %s", helper.getOutputAsString()));
    }

    /**
     * This mocks the parsing of a single record, with the value a->xyz
     * @param format
     * @return
     * @throws ParseException
     * @throws FileNotFoundException
     */
    private OutputHelper processMockOutput(CLIConfig.Format format) throws ParseException, IOException {
        CommandLineInterface iface = spy(new CommandLineInterface());
        doReturn(new CLIConfig(format, null)).when(iface).parse(any(String[].class));

        Parser parser = mock(Parser.class);
        when(parser.next()).thenReturn(new MapHelper().newMap().put("a","xyz").toArray()[0], null);
        doReturn(parser).when(iface).parseInput(any(CLIConfig.class), any(BufferedReader.class));

        OutputHelper helper  = new OutputHelper();
        iface.processArgs(new String[]{},
                new StringReader("some input file \n with another line"),
                helper.target);
        return helper;
    }

    private void assertCsvContent(String content, List<List<String>> expected) throws IOException {
        CSVParser csv = CSVParser.parse(content, CSVFormat.DEFAULT);

        List<List<String>> observed = new ArrayList<List<String>>();
        for (CSVRecord line : csv.getRecords()) {
            List<String> lineValues = new ArrayList<>();
            for (Iterator<String> seeker = line.iterator(); seeker.hasNext(); ) {
                lineValues.add(seeker.next());
            }
            observed.add(lineValues);
        }
        Assertions.assertEquals(expected, observed);
    }

    private CLIConfig generateCommandline(String cli) throws ParseException {
        String[] args = StringUtils.split(cli, ' ');
        return new CommandLineInterface().parse(args);
    }
}