package burrow.core.command;

import burrow.chain.Context;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Environment;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommandContext extends Context {
    @NonNull
    public ChamberContext getChamberContext() {
        return Objects.requireNonNull(get(Key.CHAMBER_CONTEXT, ChamberContext.class));
    }

    @NonNull
    public String getCommandName() {
        return Objects.requireNonNull(get(Key.COMMAND_NAME, String.class));
    }

    @NonNull
    public List<String> getCommandArgs() {
        @SuppressWarnings("unchecked") final List<String> args =
            (List<String>) get(Key.COMMAND_ARGS);
        return Objects.requireNonNull(args);
    }

    @NonNull
    public Environment getWorkingDirectory() {
        return Objects.requireNonNull(get(Key.ENVIRONMENT, Environment.class));
    }

    @NonNull
    public Integer getExitCode() {
        return Objects.requireNonNull(get(Key.EXIT_CODE, Integer.class));
    }

    @NonNull
    public StringBuilder getBuffer() {
        return Objects.requireNonNull(get(Key.BUFFER, StringBuilder.class));
    }

    public static final class Key {
        // Chamber object
        public static final String CHAMBER_CONTEXT = "CHAMBER_CONTEXT";

        // Command-based context
        public static final String COMMAND_NAME = "COMMAND_NAME";
        public static final String COMMAND_ARGS = "COMMAND_ARGS";
        public static final String ENVIRONMENT = "ENVIRONMENT";

        // Result context
        public static final String EXIT_CODE = "EXIT_CODE";
        public static final String BUFFER = "BUFFER";
    }
}