package me.jameschan.burrow.kernel.context;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext extends Context {
  @NonNull
  public ChamberContext getChamberContext() {
    return get(Key.CHAMBER_CONTEXT, ChamberContext.class);
  }

  @NonNull
  public String getWorkingDirectory() {
    return get(Key.WORKING_DIRECTORY, String.class);
  }

  @NonNull
  public String getCommandName() {
    return get(Key.COMMAND_NAME, String.class);
  }

  @NonNull
  public Integer getExitCode() {
    return get(Key.EXIT_CODE, Integer.class);
  }

  @NonNull
  public StringBuilder getBuffer() {
    return get(Key.BUFFER, StringBuilder.class);
  }

  public static final class Key {
    // Chamber context
    public static final String CHAMBER_CONTEXT = "CHAMBER_CONTEXT";

    // Command-based context
    public static final String WORKING_DIRECTORY = "WORKING_DIRECTORY";
    public static final String COMMAND_NAME = "COMMAND_NAME";

    // Result context
    public static final String EXIT_CODE = "EXIT_CODE";
    public static final String BUFFER = "BUFFER";
  }
}
