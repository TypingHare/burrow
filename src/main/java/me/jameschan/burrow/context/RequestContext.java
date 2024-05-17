package me.jameschan.burrow.context;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext extends Context {
  @Autowired
  public RequestContext(final Context context) {
    set(Context.Key.ROOT_DIR, context.getRootDir());
    set(Context.Key.CONFIG_FILE, context.getConfigFile());
    set(Context.Key.CHAMBER, context.getChamber());
    set(Context.Key.CONFIG, context.getConfig());
    set(Context.Key.HOARD, context.getHoard());
    set(Context.Key.RENOVATOR, context.getRenovator());
    set(Context.Key.COMMAND_MANAGER, context.getCommandManager());
  }

  public Path getWorkingDir() {
    return get(Key.WORKING_DIR, Path.class);
  }

  public String getCommandName() {
    return get(Key.COMMAND_NAME);
  }

  public Integer getStatusCode() {
    return get(Key.STATUS_CODE, Integer.class);
  }

  public StringBuffer getBuffer() {
    return get(Key.BUFFER, StringBuffer.class);
  }

  public static final class Key {
    // Command-based context
    public static final String WORKING_DIR = "WORKING_DIR";
    public static final String COMMAND_NAME = "COMMAND_NAME";

    // Resulting context
    public static final String STATUS_CODE = "STATUS_CODE";
    public static final String BUFFER = "BUFFER";
  }
}
