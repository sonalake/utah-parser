package com.sonalake.utah.cli;

import com.sonalake.utah.Parser;
import com.sonalake.utah.config.Config;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CliConfigTest {

    @Test
    public void testLoadWorks() throws IOException, IOException {

        File testFile = getConfigFileForCiscoBGPSummary();

        CLIConfig cliConfig = new CLIConfig(CLIConfig.Format.CSV, testFile.getAbsolutePath());
        Config config = cliConfig.loadConfig();
        assertNotNull("No config created", config);
    }

    private File getConfigFileForCiscoBGPSummary() throws IOException {
        File testFile = File.createTempFile("testFile", ".tmp");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("examples/cisco_bgp_summary_template.xml");
        FileUtils.copyInputStreamToFile(in, testFile);
        return testFile;
    }


    @Test(expected = FileNotFoundException.class)
    public void testLoadHandlesErrors() throws IOException {
        CLIConfig cliConfig = new CLIConfig(CLIConfig.Format.CSV, UUID.randomUUID().toString());
        cliConfig.loadConfig();
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
        assertNotNull("Expected results from parser", result);
        assertEquals("routerId record field incorrect", "192.0.2.70", result.get("routerId"));
        assertEquals("localAS record field incorrect", "65550", result.get("localAS"));
        assertEquals("remoteIp record field incorrect", "192.0.2.77", result.get("remoteIp"));
        assertEquals("remoteAS record field incorrect", "65551", result.get("remoteAS"));
        assertEquals("uptime record field incorrect", "5w4d", result.get("uptime"));
        assertEquals("status record field incorrect", "1", result.get("status"));
    }
}
