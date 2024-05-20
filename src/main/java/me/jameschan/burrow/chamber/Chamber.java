package me.jameschan.burrow.chamber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import me.jameschan.burrow.Constants;
import me.jameschan.burrow.command.CommandManager;
import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.context.ChamberContext;
import me.jameschan.burrow.context.RequestContext;
import me.jameschan.burrow.furniture.Furniture;
import me.jameschan.burrow.furniture.Renovator;
import me.jameschan.burrow.hoard.Hoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Chamber {
  private final ApplicationContext applicationContext;
  private final ChamberContext chamberContext;

  @Autowired
  public Chamber(
      final ApplicationContext applicationContext,
      @Qualifier("chamberContext") final ChamberContext chamberContext) {
    this.applicationContext = applicationContext;
    this.chamberContext = chamberContext;
    initContext();
  }

  public ChamberContext getContext() {
    return chamberContext;
  }

  private void initContext() {
    chamberContext.set(ChamberContext.Key.CHAMBER, this);
    chamberContext.set(ChamberContext.Key.CONFIG, applicationContext.getBean(Config.class));
    chamberContext.set(ChamberContext.Key.HOARD, applicationContext.getBean(Hoard.class, this));
    chamberContext.set(
        ChamberContext.Key.RENOVATOR, applicationContext.getBean(Renovator.class, this));
    chamberContext.set(
        ChamberContext.Key.COMMAND_MANAGER, applicationContext.getBean(CommandManager.class, this));
  }

  /**
   * Constructs this chamber.
   *
   * @param name The name of the chamber to construct.
   */
  public void construct(final String name) throws ChamberNotFoundException {
    checkChamberDir(name);
    loadConfig();
    loadFurniture();
    loadHoard();
  }

  public void restart() throws ChamberNotFoundException {
    initContext();
    construct(chamberContext.getChamberName());
  }

  /** Destructs this chamber. */
  public void destruct() {
    saveHoard();
  }

  /**
   * Executes a command. This method first extracts the command name (if presents) and create a
   * request context, then execute the command using the command manager. The request context will
   * be updated after executing the command.
   *
   * @param rawArgs The raw arguments to process.
   * @return a request context.
   */
  public RequestContext execute(final List<String> rawArgs) {
    final var hasCommand = !rawArgs.isEmpty() && !rawArgs.getFirst().startsWith("-");
    final var commandName = hasCommand ? rawArgs.getFirst() : "";
    final var args = hasCommand ? rawArgs.subList(1, rawArgs.size()) : rawArgs;
    final var requestContext = applicationContext.getBean(RequestContext.class, chamberContext);
    requestContext.set(RequestContext.Key.COMMAND_NAME, commandName);
    requestContext.set(RequestContext.Key.BUFFER, new StringBuffer());

    final var statusCode =
        chamberContext.getCommandManager().execute(commandName, args, requestContext);
    requestContext.set(RequestContext.Key.STATUS_CODE, statusCode);

    return requestContext;
  }

  /** Saves the config of this chamber in JSON format. */
  public void saveConfig() {
    final var configFile = chamberContext.getConfigFile();
    final var content = new Gson().toJson(chamberContext.getConfig().getData());
    try {
      Files.write(configFile.toPath(), content.getBytes());
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void saveHoard() {
    final var hoardFile = chamberContext.getHoardFile();
    final var hoard = chamberContext.getHoard();
    final var objects = hoard.getEntries().stream().map(hoard::getEntryObject).toList();
    final var json =
        new Gson().toJson(objects, new TypeToken<List<Map<String, String>>>() {}.getType());

    try {
      Files.write(hoardFile.toPath(), json.getBytes());
    } catch (final IOException ex) {
      throw new RuntimeException(
          "Fail to save hoard for chamber: " + chamberContext.getChamberName(), ex);
    }
  }

  @NonNull
  public <T extends Furniture> T getFurniture(final Class<T> clazz) {
    final var furniture = chamberContext.getRenovator().getFurniture(clazz);
    if (furniture == null) {
      throw new RuntimeException("Failed to find furniture: " + clazz.getName());
    }

    return furniture;
  }

  /**
   * Checks if the chamber directory exists, and if it does, set ROOT_DIR for context.
   *
   * @param name The name of the chamber.
   */
  private void checkChamberDir(final String name) throws ChamberNotFoundException {
    final var dirPath = Constants.CHAMBER_ROOT_DIR.resolve(name).normalize();
    final var file = dirPath.toFile();
    if (!file.exists() || !file.isDirectory()) {
      throw new ChamberNotFoundException(name);
    }

    chamberContext.set(ChamberContext.Key.CHAMBER_NAME, name);
    chamberContext.set(ChamberContext.Key.ROOT_DIR, dirPath);
  }

  /**
   * Loads the config file from the chamber root directory and sets the CONFIG in context.
   *
   * @throws RuntimeException if the chamber config file does not exist.
   */
  private void loadConfig() {
    final var filePath =
        chamberContext.getRootDir().resolve(Constants.CONFIG_FILE_NAME).normalize();
    chamberContext.set(ChamberContext.Key.CONFIG_FILE, filePath.toFile());

    if (!filePath.toFile().exists()) {
      throw new RuntimeException("Chamber config file does not exist: " + filePath);
    }

    try {
      final var config = chamberContext.getConfig();
      final var content = Files.readString(filePath);
      final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
      final Map<String, String> map = new Gson().fromJson(content, mapType);
      map.keySet().forEach(config::addKey);
      map.forEach(config::set);
      chamberContext.set(ChamberContext.Key.CONFIG, config);
    } catch (final Exception ex) {
      throw new RuntimeException("Fail to load config: " + filePath, ex);
    }
  }

  /** Loads furniture. */
  private void loadFurniture() {
    final var config = chamberContext.getConfig();
    final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
    final var furnitureList =
        Arrays.stream(furnitureListString.split(":"))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
    furnitureList.forEach(chamberContext.getRenovator()::loadByName);
  }

  /**
   * Loads the entries from the hoard file in the chamber root directory. If the hoard file does not
   * exist, create one.
   */
  private void loadHoard() {
    final var filePath = chamberContext.getRootDir().resolve(Constants.HOARD_FILE_NAME);
    if (!filePath.toFile().exists()) {
      // Create a database file and write "[]"
      try {
        Files.write(filePath, "[]".getBytes());
        return;
      } catch (final IOException ex) {
        throw new RuntimeException("Fail to create database: " + filePath, ex);
      }
    }

    chamberContext.set(ChamberContext.Key.HOARD_FILE, filePath.toFile());

    try {
      final var content = Files.readString(filePath);
      final Type mapType = new TypeToken<List<Map<String, String>>>() {}.getType();
      final List<Map<String, String>> entries = new Gson().fromJson(content, mapType);
      entries.forEach(chamberContext.getHoard()::register);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
