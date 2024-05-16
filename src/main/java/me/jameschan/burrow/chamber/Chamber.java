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
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Chamber {
    private final ApplicationContext applicationContext;
    private final Context context;
    private final Config config;
    private final Hoard hoard;
    private final Renovator renovator;
    private final CommandManager commandManager;

    public Chamber(
        final ApplicationContext applicationContext
    ) {
        this.applicationContext = applicationContext;
        context = (Context) applicationContext.getBean("context");
        config = applicationContext.getBean(Config.class);
        hoard = applicationContext.getBean(Hoard.class, this);
        renovator = applicationContext.getBean(Renovator.class, this);
        commandManager = applicationContext.getBean(CommandManager.class, this);
    }

    /**
     * Constructs this chamber.
     * @param name The name of the chamber to construct.
     */
    public void construct(final String name) {
        checkChamberDir(name);
        loadConfig();
        loadHoard();
    }

    public RequestContext execute(final List<String> rawArgs) {
        final var hasCommand = !rawArgs.isEmpty() && !rawArgs.getFirst().startsWith("-");
        final var commandName = hasCommand ? rawArgs.getFirst() : "";
        final var args = hasCommand ? rawArgs.subList(1, rawArgs.size()) : rawArgs;
        final var requestContext = applicationContext.getBean(RequestContext.class, context);
        requestContext.set(RequestContext.Key.COMMAND_NAME, commandName);
        requestContext.set(RequestContext.Key.BUFFER, new StringBuffer());

        final var statusCode = commandManager.execute(commandName, args, requestContext);
        requestContext.set(RequestContext.Key.STATUS_CODE, statusCode);

        return requestContext;
    }

    public Config getConfig() {
        return config;
    }

    public Hoard getHoard() {
        return hoard;
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

    private void loadConfig() {
        final var filePath = context.getRootDir().resolve(Constants.CONFIG_FILE_NAME).normalize();
        context.set(Context.Key.CONFIG_FILE_PATH, filePath);

        if (!filePath.toFile().exists()) {
            throw new RuntimeException("Chamber config file does not exist: " + filePath);
        }

        try {
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
            entries.forEach(hoard::register);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
