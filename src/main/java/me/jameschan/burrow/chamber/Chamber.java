package me.jameschan.burrow.chamber;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.jameschan.burrow.Constants;
import me.jameschan.burrow.command.CommandManager;
import me.jameschan.burrow.config.Config;
import me.jameschan.burrow.context.Context;
import me.jameschan.burrow.context.RequestContext;
import me.jameschan.burrow.furniture.Renovator;
import me.jameschan.burrow.hoard.Hoard;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Chamber {
    private final ApplicationContext applicationContext;
    private final Context context;

    public Chamber(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        context = (Context) applicationContext.getBean("context");
        context.set(Context.Key.CHAMBER, this);
        context.set(Context.Key.CONFIG, applicationContext.getBean(Config.class));
        context.set(Context.Key.HOARD, applicationContext.getBean(Hoard.class, this));
        context.set(Context.Key.RENOVATOR, applicationContext.getBean(Renovator.class, this));
        context.set(Context.Key.COMMAND_MANAGER, applicationContext.getBean(CommandManager.class, this));
    }

    public Context getContext() {
        return context;
    }

    /**
     * Constructs this chamber.
     * @param name The name of the chamber to construct.
     */
    public void construct(final String name) {
        checkChamberDir(name);
        loadConfig();
        loadFurniture();
        loadHoard();
    }

    /**
     * Executes a command. This method first extracts the command name (if presents) and create a
     * request context, then execute the command using the command manager. The request context will
     * be updated after executing the command.
     * @param rawArgs The raw arguments to process.
     * @return a request context.
     */
    public RequestContext execute(final List<String> rawArgs) {
        final var hasCommand = !rawArgs.isEmpty() && !rawArgs.getFirst().startsWith("-");
        final var commandName = hasCommand ? rawArgs.getFirst() : "";
        final var args = hasCommand ? rawArgs.subList(1, rawArgs.size()) : rawArgs;
        final var requestContext = applicationContext.getBean(RequestContext.class, context);
        requestContext.set(RequestContext.Key.COMMAND_NAME, commandName);
        requestContext.set(RequestContext.Key.BUFFER, new StringBuffer());

        final var statusCode = context.getCommandManager().execute(commandName, args, requestContext);
        requestContext.set(RequestContext.Key.STATUS_CODE, statusCode);

        return requestContext;
    }

    /**
     * Saves config.
     */
    public void saveConfig() {
        final var configFile = context.getConfigFile();
        final var content = new Gson().toJson(context.getConfig().getData());
        try {
            Files.write(configFile.toPath(), content.getBytes());
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Checks if the chamber directory exists, and if it does, set ROOT_DIR for context.
     * @param name The name of the chamber.
     */
    private void checkChamberDir(final String name) {
        final var dirPath = Constants.CHAMBER_ROOT_DIR.resolve(name).normalize();
        final var file = dirPath.toFile();
        if (!file.exists() || !file.isDirectory()) {
            throw new RuntimeException("Chamber root directory does not exist: " + dirPath);
        }

        context.set(Context.Key.ROOT_DIR, dirPath);
    }

    /**
     * Loads the config file from the chamber root directory and sets the CONFIG in context.
     * @throws RuntimeException if the chamber config file does not exist.
     */
    private void loadConfig() {
        final var filePath = context.getRootDir().resolve(Constants.CONFIG_FILE_NAME).normalize();
        context.set(Context.Key.CONFIG_FILE, filePath.toFile());

        if (!filePath.toFile().exists()) {
            throw new RuntimeException("Chamber config file does not exist: " + filePath);
        }

        try {
            final var config = context.getConfig();
            final var content = Files.readString(filePath);
            final Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            final Map<String, String> map = new Gson().fromJson(content, mapType);
            map.keySet().forEach(config::addKey);
            map.forEach(config::set);
            context.set(Context.Key.CONFIG, config);
        } catch (final Exception ex) {
            throw new RuntimeException("Fail to load config: " + filePath, ex);
        }
    }

    /**
     * Loads furniture.
     */
    private void loadFurniture() {
        final var config = context.getConfig();
        final var furnitureListString = config.get(Config.Key.FURNITURE_LIST);
        final var furnitureList = Arrays.stream(furnitureListString.split(":"))
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .toList();
        furnitureList.forEach(context.getRenovator()::loadByName);
    }

    /**
     * Loads the entries from the hoard file in the chamber root directory. If the hoard file does
     * not exist, create one.
     */
    private void loadHoard() {
        final var filePath = context.getRootDir().resolve(Constants.HOARD_FILE_NAME);
        if (!filePath.toFile().exists()) {
            // Create a database file and write "[]"
            try {
                Files.write(filePath, "[]".getBytes());
                return;
            } catch (final IOException ex) {
                throw new RuntimeException("Fail to create database: " + filePath, ex);
            }
        }

        try {
            final var content = Files.readString(filePath);
            final Type mapType = new TypeToken<List<Map<String, String>>>() {
            }.getType();
            final List<Map<String, String>> entries = new Gson().fromJson(content, mapType);
            entries.forEach(context.getHoard()::register);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
