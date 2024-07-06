package burrow.furniture.exec;

import burrow.core.chamber.Chamber;
import burrow.core.command.CommandContext;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "Exec",
    description = "Execute a command using process builder."
)
public class ExecFurniture extends Furniture {
    public static final String COMMAND_TYPE = "Exec";

    public ExecFurniture(@NonNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public void beforeInitialization() {
        registerCommand(ExecCommand.class);
    }

    @Override
    public Collection<String> configKeys() {
        return List.of(ConfigKey.EXEC_SHELL);
    }

    @Override
    public void initializeConfig(@NonNull final Config config) {
        config.setIfAbsent(ConfigKey.EXEC_SHELL, "bash");
    }

    public void execute(
        @NonNull final CommandContext commandContext,
        @NonNull final String command
    ) throws IOException, InterruptedException {
        final var shell = getConfig().get(ConfigKey.EXEC_SHELL);
        final var processBuilder = new ProcessBuilder(shell, "-c", '"' + command + '"');

        // Set the working directory
        final var environment = commandContext.getEnvironment();
        final var workingDirectory = environment.getWorkingDirectory();
        processBuilder.directory(new File(workingDirectory));

        // Set the environment
        final var env = processBuilder.environment();
        env.put("PATH", System.getenv("PATH"));

        // Execute the command
        final var process = processBuilder.start();

        String line;

        // Read the output
        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        final var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final var errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }

        // Wait for the process to complete
        final var exitCode = process.waitFor();
        final var buffer = commandContext.getBuffer();
        buffer.append(exitCode == 0 ? stringBuilder : errorOutput);
        commandContext.setExitCode(exitCode);
    }

    public @interface ConfigKey {
        String EXEC_SHELL = "exec.shell";
    }
}
