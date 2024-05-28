package me.jameschan.burrow.kernel.entry;

import java.io.FileNotFoundException;

public class HoardFileNotFoundException extends FileNotFoundException {
  public HoardFileNotFoundException(final String hoardFilePath) {
    super("Hoard file not found: " + hoardFilePath);
  }
}
