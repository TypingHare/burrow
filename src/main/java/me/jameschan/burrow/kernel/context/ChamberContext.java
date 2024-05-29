package me.jameschan.burrow.kernel.context;

import java.io.File;
import java.nio.file.Path;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.entry.Hoard;
import me.jameschan.burrow.kernel.formatter.Formatter;
import me.jameschan.burrow.kernel.furniture.Renovator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChamberContext extends Context {
  public Chamber getChamber() {
    return get(Key.CHAMBER, Chamber.class);
  }

  public Config getConfig() {
    return get(Key.CONFIG, Config.class);
  }

  public Hoard getHoard() {
    return get(Key.HOARD, Hoard.class);
  }

  public Renovator getRenovator() {
    return get(Key.RENOVATOR, Renovator.class);
  }

  public Processor getProcessor() {
    return get(Key.PROCESSOR, Processor.class);
  }

  public Formatter getFormatter() {
    return get(Key.FORMATTER, Formatter.class);
  }

  public Path getRootDir() {
    return get(Key.ROOT_DIR, Path.class);
  }

  public File getConfigFile() {
    return get(Key.CONFIG_FILE, File.class);
  }

  public File getHoardFile() {
    return get(Key.HOARD_FILE, File.class);
  }

  /**
   * The builtin keys to chamber context. Refer to the context table in documentation for more
   * information.
   */
  public static final class Key {
    // Chamber object
    public static final String CHAMBER = "CHAMBER";

    // Objects of builtin modules
    public static final String CONFIG = "CONFIG";
    public static final String HOARD = "HOARD";
    public static final String RENOVATOR = "RENOVATOR";
    public static final String PROCESSOR = "PROCESSOR";
    public static final String FORMATTER = "FORMATTER";

    // Root directories and crucial file paths
    public static final String ROOT_DIR = "ROOT_DIR";
    public static final String CONFIG_FILE = "CONFIG_FILE";
    public static final String HOARD_FILE = "HOARD_FILE";
  }
}
