package burrow.core.config;

import java.io.FileNotFoundException;

public class ConfigFileNotFoundException extends FileNotFoundException {
  public ConfigFileNotFoundException(final String configFilePath) {
    super(String.format("Chamber config file does not exist: %s", configFilePath));
  }
}
