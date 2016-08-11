package com.sonalake.utah.cli;

import com.sonalake.utah.config.Config;
import com.sonalake.utah.config.ConfigLoader;
import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CLIConfig {
    public CLIConfig(Format format, String pathToConfig) {
        this.format = format;
        this.pathToConfig = pathToConfig;
    }

    public Config loadConfig() throws FileNotFoundException, JAXBException {
        try {
            Reader reader = new InputStreamReader(new FileInputStream(this.pathToConfig));
            return new ConfigLoader().loadConfig(reader);
        } catch(FileNotFoundException e) {
            throw new FileNotFoundException("File not found");
        } catch(JAXBException e) {
        }
        return null;
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
