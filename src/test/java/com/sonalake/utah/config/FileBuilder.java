package com.sonalake.utah.config;

import java.io.StringReader;

/**
 * A test helper for building a file
 */
final class FileBuilder {
  private StringBuilder file;

  FileBuilder() {
    this.file = new StringBuilder();
  }

  public FileBuilder addLine(String s) {
    file.append(s + "\n");
    return this;
  }

  StringReader buildReader() {
    return new StringReader(file.toString());
  }

}
