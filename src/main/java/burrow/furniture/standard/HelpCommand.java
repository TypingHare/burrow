package burrow.furniture.standard;

import burrow.core.command.Command;
import burrow.core.command.CommandContext;
import burrow.core.command.CommandType;
import com.google.gson.Gson;
import org.springframework.lang.NonNull;
import picocli.CommandLine;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "help", description = "Display the usage of a command.")
@CommandType(StandardFurniture.COMMAND_TYPE)
public class HelpCommand extends Command {
    @CommandLine.Parameters(
        index = "0",
        paramLabel = "<command-name>",
        description = "The name of the command.",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String commandName;

    @CommandLine.Option(
        names = {"-j", "--json"},
        description = "Whether to display the data in JSON form",
        defaultValue = "false"
    )
    private Boolean useJson;

    public HelpCommand(@NonNull final CommandContext context) {
        super(context);
    }

    @Override
    public Integer call() {
        if (useJson) {
            if (commandName == null) {
                buffer.append("{\"error\": \"You must specify a command name when using the --json flag.\"}");
                return CommandLine.ExitCode.USAGE;
            }

            final var commandInfo = getCommandInfo(commandName);
            buffer.append(new Gson().toJson(commandInfo));
            return CommandLine.ExitCode.OK;
        }

        if (commandName == null) {
            // Display the usage of the chamber
            return CommandLine.ExitCode.OK;
        }

        final var command = getProcessor().getCommand(commandName);
        if (command == null) {
            buffer.append("No such command: ").append(commandName);
        } else {
            buffer.append(new CommandLine(command).getUsageMessage());
        }

        return CommandLine.ExitCode.OK;
    }

    @NonNull
    public CommandInfo getCommandInfo(@NonNull final String commandName) {
        final var commandClass = getProcessor().getCommand(commandName);
        final var commandInfo = new CommandInfo();

        if (commandClass == null) {
            commandInfo.setError("Command does not exist.");
            return commandInfo;
        }

        final var parameterMetadataList = new ArrayList<ParameterMetadata>();

        final var fields = commandClass.getDeclaredFields();
        for (final var field : fields) {
            final var parametersAnnotation = field.getAnnotation(CommandLine.Parameters.class);
            if (parametersAnnotation == null) continue;

            final var parameterIndexString = parametersAnnotation.index();
            if (!parameterIndexString.isEmpty()) {
                final var index = Integer.parseInt(parameterIndexString);
                final var paramLabel = parametersAnnotation.paramLabel();
                final var label = paramLabel.isEmpty() ? field.getName() : paramLabel;
                while (index >= parameterMetadataList.size()) parameterMetadataList.add(null);
                final var parameterMetadata = new ParameterMetadata();
                parameterMetadata.setLabel(label);
                parameterMetadata.setDescription(parametersAnnotation.description()[0]);
                parameterMetadata.setOptional(!parametersAnnotation.defaultValue()
                    .equals("__no_default_value__"));
                parameterMetadata.setDefaultValue(parametersAnnotation.defaultValue());
                parameterMetadataList.set(index, parameterMetadata);
            }

            // final var parameterArityString = parametersAnnotation.arity();
        }

        commandInfo.setParameters(parameterMetadataList);

        return commandInfo;
    }

    @JavaBean
    public static final class ParameterMetadata {
        private String label;
        private String description;
        private Boolean optional;
        private String defaultValue;

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public Boolean getOptional() {
            return optional;
        }

        public void setOptional(final Boolean optional) {
            this.optional = optional;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(final String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    @JavaBean
    public static final class CommandInfo {
        private String error;
        private List<ParameterMetadata> parameters;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public List<ParameterMetadata> getParameters() {
            return parameters;
        }

        public void setParameters(final List<ParameterMetadata> parameters) {
            this.parameters = parameters;
        }
    }
}