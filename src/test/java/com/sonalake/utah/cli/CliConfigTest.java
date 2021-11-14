package com.sonalake.utah.cli;

import com.sonalake.utah.Parser;
import com.sonalake.utah.config.Config;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class CliConfigTest {

    @Test
    public void testLoadWorks() throws IOException {

        File testFile = getConfigFileForCiscoBGPSummary();

        CLIConfig cliConfig = new CLIConfig(CLIConfig.Format.CSV, testFile.getAbsolutePath());
        Config config = cliConfig.loadConfig();
        Assertions.assertNotNull(config, "No config created");
    }

    private File getConfigFileForCiscoBGPSummary() throws IOException {
        File testFile = File.createTempFile("testFile", ".tmp");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("examples/cisco_bgp_summary_template.xml");
        FileUtils.copyInputStreamToFile(in, testFile);
        return testFile;
    }


    @Test
    public void testLoadHandlesErrors() {
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            CLIConfig cliConfig = new CLIConfig(CLIConfig.Format.CSV, UUID.randomUUID().toString());
            cliConfig.loadConfig();
        });
    }

    @Test
    public void testParser() throws IOException {
        File testFile = getConfigFileForCiscoBGPSummary();

        CLIConfig cliConfig = new CLIConfig(CLIConfig.Format.CSV, testFile.getAbsolutePath());

        CommandLineInterface cli =  new CommandLineInterface();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("examples/cisco_bgp_summary_example.txt")
                )
        );
        Parser parser = cli.parseInput(cliConfig, reader);

        Map<String, String> result = parser.next();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result, "Expected results from parser"),
                () -> Assertions.assertEquals( "192.0.2.70", result.get("routerId"), "routerId record field incorrect"),
                () -> Assertions.assertEquals( "65550", result.get("localAS"), "localAS record field incorrect"),
                () -> Assertions.assertEquals( "192.0.2.77", result.get("remoteIp"), "remoteIp record field incorrect"),
                () -> Assertions.assertEquals( "65551", result.get("remoteAS"), "remoteAS record field incorrect"),
                () -> Assertions.assertEquals( "5w4d", result.get("uptime"), "uptime record field incorrect"),
                () -> Assertions.assertEquals( "1", result.get("status"), "status record field incorrect"));
    }
}
