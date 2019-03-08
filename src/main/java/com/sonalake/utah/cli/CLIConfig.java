package com.sonalake.utah.cli;

import com.sonalake.utah.config.Config;
import com.sonalake.utah.config.ConfigLoader;
import java.io.*;

public class CLIConfig {
    public CLIConfig(Format format, String pathToConfig) {
        this.format = format;
        this.pathToConfig = pathToConfig;
    }

    public Config loadConfig() throws FileNotFoundException {
        try {
            Reader reader = new InputStreamReader(new FileInputStream(this.pathToConfig));
            return new ConfigLoader().loadConfig(reader);
        } catch(FileNotFoundException e) {
            throw new FileNotFoundException("File not found");
        } catch (IOException e) {
            throw new AssertionError("Can't parse config file", e);
        }
    }

    enum Format {CSV, JSON};

    private final Format format;
    private final String pathToConfig;

    Format getFormat() { return format; }

    String getPathToConfig() {
        return pathToConfig;
    }

    public String toString() {
        return String.format("Format: %s; config: %s", format, pathToConfig);
    }
}
