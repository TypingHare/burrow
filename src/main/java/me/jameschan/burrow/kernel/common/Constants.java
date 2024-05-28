package me.jameschan.burrow.kernel.common;

import java.nio.file.Path;

public final class Constants {
  public static final String BURROW_DIR_NAME = ".burrow";

  public static final Path ROOT_DIR =
      Path.of(System.getProperty("user.home")).resolve(BURROW_DIR_NAME);

  public static final Path CHAMBER_ROOT_DIR = ROOT_DIR.resolve("chamber");

  public static final String DEFAULT_CHAMBER = ".";

  public static final String CONFIG_FILE_NAME = "config.json";

  public static final String HOARD_FILE_NAME = "hoard.json";

  public static final String FURNITURE_NAME_SEPARATOR = ":";
}
