package burrow.server;

import burrow.core.Burrow;
import burrow.core.chamber.ChamberInitializationException;
import burrow.core.command.CommandContext;
import burrow.core.common.Environment;
import burrow.core.furniture.InvalidFurnitureClassException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"burrow.core"})
@RestController
public class BurrowServer {
    private static final Logger logger = LoggerFactory.getLogger(BurrowServer.class);

    private final Burrow burrow;

    public BurrowServer() throws InvalidFurnitureClassException {
        burrow = new Burrow();
    }

    public static void main(final String[] args) {
        SpringApplication.run(BurrowServer.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStart() throws ChamberInitializationException {
        burrow.getChamberShepherd().initializeRootChamber();
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
        logger.info("Shutting down Burrow...");
        burrow.getChamberShepherd().terminateAll();

        logger.info("Successfully shut down Burrow.");
    }
}
