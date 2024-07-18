package burrow.furniture.shellcore;

import burrow.core.chamber.Chamber;
import burrow.core.command.CommandContext;
import burrow.core.common.ColorUtility;
import burrow.core.config.Config;
import burrow.core.furniture.BurrowFurniture;
import burrow.core.furniture.Furniture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

@BurrowFurniture(
    simpleName = "Shell Core",
    description = "Shell Core allows developers to execute shell commands.",
    type = BurrowFurniture.Type.COMPONENT
)
public class ShellCoreFurniture extends Furniture {
    public static final String DEFAULT_SHELL_PATH = "/bin/bash";

    public ShellCoreFurniture(@NotNull final Chamber chamber) {
        super(chamber);
    }

    @Override
    public @Nullable Collection<String> configKeys() {
        return List.of(ConfigKey.SHELL_PATH);
    }

    @Override
    public void initializeConfig(@NotNull final Config config) {
        config.setIfAbsent(ConfigKey.SHELL_PATH, DEFAULT_SHELL_PATH);
    }

    public @NotNull ProcessBuilder getProcessBuilder(
        @NotNull final CommandContext commandContext,
        @NotNull final String command
    ) {
        final var shell = getConfig().get(ConfigKey.SHELL_PATH);
        final var processBuilder = new ProcessBuilder(shell, "-c", command);

        // Set the working directory
        final var environment = commandContext.getEnvironment();
        final var workingDirectory = environment.getWorkingDirectory();
        processBuilder.directory(new File(workingDirectory));

        return processBuilder;
    }

    public @NotNull ShellResponse executeCommand(
        @NotNull final CommandContext commandContext,
        @NotNull final String command
    ) throws IOException, InterruptedException {
        final var processBuilder = getProcessBuilder(commandContext, command);

        // Execute the command
        final var process = processBuilder.start();

        String line;

        // Read the standard output
        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final var standardOutputBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            standardOutputBuilder.append(line).append("\n");
        }

        // Read the error output
        final var errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final var errorOutputBuilder = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errorOutputBuilder.append(line).append("\n");
        }

        // Wait for the process to complete
        final var exitCode = process.waitFor();
        final var response = new ShellResponse();
        response.setExitCode(exitCode);
        response.setStandardOutput(standardOutputBuilder.toString());
        response.setErrorOutput(errorOutputBuilder.toString());

        return response;
    }

    public void dispatch(
        @NotNull final CommandContext commandContext,
        @NotNull final String command
    ) throws IOException, InterruptedException {
        final var shellResponse = executeCommand(commandContext, command);
        final var exitCode = shellResponse.getExitCode();
        commandContext.setExitCode(exitCode);

        final var buffer = commandContext.getBuffer();
        buffer.append(shellResponse.getStandardOutput());

        if (exitCode != 0) {
            final var errorOutput = shellResponse.getErrorOutput();
            buffer.append("\n")
                .append(ColorUtility.render(errorOutput, ColorUtility.Type.MESSAGE_ERROR));
        }
    }



    public @interface ConfigKey {
        String SHELL_PATH = "shell.path";
    }
}
