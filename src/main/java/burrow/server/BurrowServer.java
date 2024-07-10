package burrow.server;

import burrow.client.BurrowResponse;
import burrow.core.Burrow;
import burrow.core.command.CommandContext;
import burrow.core.common.Environment;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BurrowServer {
    private final Burrow burrow = new Burrow();

    public static void main(final String[] args) {
        SpringApplication.run(BurrowServer.class, args);
    }

    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    @PostMapping("/")
    public BurrowResponse receiveRequest(
        @RequestBody final String command,
        @RequestHeader("Working-Directory") final String workingDirectory,
        @RequestHeader("Console-Width") final Integer consoleWidth
    ) {
        final var environment = new Environment();
        environment.setWorkingDirectory(workingDirectory);
        environment.setConsoleWidth(consoleWidth);

        final var commandContext = burrow.getChamberShepherd().process(command, environment);

        final var response = new BurrowResponse();
        response.setMessage(CommandContext.Hook.buffer.getNonNull(commandContext).toString());
        response.setExitCode(CommandContext.Hook.exitCode.getNonNull(commandContext));

        return response;
    }

    @PreDestroy
    public void onShutdown() {
        burrow.shutdown();
    }
}
