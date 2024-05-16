package me.jameschan.burrow;

import me.jameschan.burrow.chamber.ChamberManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
@RestController
public class BurrowApp {
    private final ChamberManager chamberManager;

    public static void main(final String[] args) {
        SpringApplication.run(BurrowApp.class, args);
    }

    @Autowired
    public BurrowApp(final ChamberManager chamberManager) {
        this.chamberManager = chamberManager;
        chamberManager.getChamber(Constants.DEFAULT_APP);
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> receiveRequest(
        @RequestBody final Map<String, String> requestObject
    ) {
        final var args = splitArguments(requestObject.get("command"));
        final var hasChamber = !args.isEmpty() && !args.getFirst().startsWith("-");
        final var chamberName = hasChamber ? args.getFirst() : Constants.DEFAULT_APP;
        final var chamber = chamberManager.getChamber(chamberName);
        final var context = chamber.execute(hasChamber ? args.subList(1, args.size()) : args);

        final var responseObject = new HashMap<String, String>();
        responseObject.put("code", String.valueOf(context.getStatusCode()));
        responseObject.put("output", context.getBuffer().toString());

        return ResponseEntity.ok().body(responseObject);
    }

    public static List<String> splitArguments(String input) {
        final List<String> arguments = new ArrayList<>();
        final Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (matcher.find()) {
            arguments.add(matcher.group(1).replace("\"", ""));
        }

        return arguments;
    }
}
