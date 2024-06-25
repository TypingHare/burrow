package burrow.server;

import burrow.core.chamber.ChamberInitializationException;
import burrow.core.chamber.ChamberShepherd;
import burrow.core.common.Environment;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"burrow.core"})
@RestController
public class BurrowServer {
    private static final Logger logger = LoggerFactory.getLogger(BurrowServer.class);

    private final ChamberShepherd chamberShepherd;

    @Autowired
    public BurrowServer(final ChamberShepherd chamberShepherd) {
        this.chamberShepherd = chamberShepherd;
    }

    public static void main(final String[] args) {
        SpringApplication.run(BurrowServer.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStart() throws ChamberInitializationException {
        chamberShepherd.initRootChamber();
    }

    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    @PostMapping("/")
    @ResponseBody
    public Map<String, String> receiveRequest(
        @RequestBody final String command,
        @RequestHeader("Working-Directory") final String workingDirectory,
        @RequestHeader("Console-Width") final String consoleWidth
    ) {
        final var environment = new Environment();
        environment.setWorkingDirectory(workingDirectory);

        final var commandContext = chamberShepherd.processCommand(command, environment);

        final var map = new HashMap<String, String>();
        map.put("message", commandContext.getBuffer().toString());
        map.put("code", commandContext.getExitCode().toString());

        return map;
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("Shutting down Burrow...");
        chamberShepherd.terminateAll();

        logger.info("Successfully shut down Burrow.");
    }
}
