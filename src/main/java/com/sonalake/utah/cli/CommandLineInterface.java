package com.sonalake.utah.cli;

import com.google.gson.GsonBuilder;
import com.sonalake.utah.Parser;
import com.sonalake.utah.config.Config;

import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;


public class CommandLineInterface {

    private static final String FORMAT_PARAM = "o";
    private static final String CONFIG_PARAM = "f";

    public static void main(String[] args) {
        CommandLineInterface iface = new CommandLineInterface();
        Reader source = new InputStreamReader(System.in);
        PrintStream target = System.out;
        iface.processArgs(args, source, target);
    }

    void processArgs(String[] args, Reader source, PrintStream target) {
        try {
            CLIConfig cliConfig = parse(args);
            try (BufferedReader reader = new BufferedReader(source)) {
                Parser parser = parseInput(cliConfig, reader);
                String format = cliConfig.getFormat().toString();
                switch (format.toUpperCase()) {
                    case "JSON":
                        printToJSON(parser, target);
                        break;
                    case "CSV":
                        printToCSV(parser, target);
                        break;
                }
            }
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            PrintWriter pw = new PrintWriter(target);
            formatter.printHelp(pw, formatter.getWidth(), "utah", "",
                    buildOptions(), formatter.getLeftPadding(), formatter.getDescPadding(),
                    "", true);
            pw.flush();
        }
    }

    /**
     * Print the content of the parsed records as json to the target
     *
     * @param parser
     */
    void printToJSON(Parser parser, PrintStream target) {
        Map<String, String> curr = parser.next();

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();

        while (curr != null) {
            mapList.add(curr);
            curr = parser.next();
        }
        target.print(mapListToJSON(mapList));
    }

    /**
     * Creates a list of headers, prints the headers, then prints the records for each header.
     * If a record doesn't exist for a header, a blank space is put in its place
     *
     * @param parser
     * @param target
     * @throws IOException
     */
    void printToCSV(Parser parser, PrintStream target) throws IOException {
        Map<String, String> curr = parser.next();

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();

        Set<String> CSV_HEADERS = new TreeSet<>();
        // Get headers and create list
        while (curr != null) {
            mapList.add(curr);
            for (String str : curr.keySet()) {
                CSV_HEADERS.add(str);
            }
            curr = parser.next();
        }

        CSVFormat csvFormat = CSVFormat.DEFAULT;
        CSVPrinter printer = new CSVPrinter(target, csvFormat);
        printer.printRecord(CSV_HEADERS);

        // Cycle through array of maps printing value for each header
        for (Map<String, String> map : mapList) {
            List<String> values = new ArrayList<String>();
            for (String header : CSV_HEADERS) {
                String value = StringUtils.trimToEmpty(map.get(header));
                values.add(value);
            }
            printer.printRecord(values.toArray());
        }
    }

    /**
     * Converts mapList to JSON Object ready for printing
     *
     * @param mapList
     * @return
     */
    static String mapListToJSON(List<Map<String, String>> mapList) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        String jsonOutput = gsonBuilder.setPrettyPrinting().create().toJson(mapList);
        return jsonOutput;
    }

    Parser parseInput(CLIConfig cliConfig, BufferedReader reader) throws FileNotFoundException {
        Config parserConfig = cliConfig.loadConfig();
        Parser results = Parser.parse(parserConfig, reader);
        return results;
    }

    /*
     * Set-up for the Apache Commons CLI options
     */
    Options buildOptions() {
        Options options = new Options();
        options.addOption(FORMAT_PARAM, true, "The output format, must be one of: " + (StringUtils.join(CLIConfig.Format.values(), ", ")).toLowerCase());
        options.addOption(CONFIG_PARAM, true, "The config file");
        options.getOption(CONFIG_PARAM).setRequired(true);
        return options;
    }

    CLIConfig parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(buildOptions(), args);
        return deriveConfig(cmd);
    }

    /*
     * Loads the configuration for the parser from the command line input.
     */
    CLIConfig deriveConfig(CommandLine cmd) throws ParseException {
        // Get format
        CLIConfig.Format format;

        String opt = cmd.getOptionValue(FORMAT_PARAM);
        String conf = cmd.getOptionValue(CONFIG_PARAM);

        if (StringUtils.isEmpty(conf)) {
            throw new ParseException("No configuration file supplied!");
        }
        if (StringUtils.isEmpty(opt)) {
            format = CLIConfig.Format.CSV;
        } else {
            // is opt CSV or JSON - otherwise throw a ParseException
            switch (opt.toUpperCase()) {
                case "CSV":
                    format = CLIConfig.Format.CSV;
                    break;
                case "JSON":
                    format = CLIConfig.Format.JSON;
                    break;
                default:
                    throw new ParseException(opt + " is not a valid format!");
            }
        }
        // Get output path
        String outputPath = cmd.getOptionValue(CONFIG_PARAM);
        return new CLIConfig(format, outputPath);
    }
}