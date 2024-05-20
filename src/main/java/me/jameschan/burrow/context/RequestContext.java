package me.jameschan.burrow.context;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext extends ChamberContext {
  @Autowired
  public RequestContext(final ChamberContext chamberContext) {
    set(ChamberContext.Key.ROOT_DIR, chamberContext.getRootDir());
    set(ChamberContext.Key.CONFIG_FILE, chamberContext.getConfigFile());
    set(ChamberContext.Key.CHAMBER, chamberContext.getChamber());
    set(ChamberContext.Key.CONFIG, chamberContext.getConfig());
    set(ChamberContext.Key.HOARD, chamberContext.getHoard());
    set(ChamberContext.Key.RENOVATOR, chamberContext.getRenovator());
    set(ChamberContext.Key.COMMAND_MANAGER, chamberContext.getCommandManager());
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
