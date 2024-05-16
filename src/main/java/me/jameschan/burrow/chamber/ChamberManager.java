package me.jameschan.burrow.chamber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChamberManager {
    private final Map<String, Chamber> byName = new HashMap<>();

    private final ApplicationContext applicationContext;

    @Autowired
    public ChamberManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Chamber getChamber(final String name) {
        if (!byName.containsKey(name)) {
            final var chamber = applicationContext.getBean(Chamber.class, applicationContext);
            chamber.construct(name);
            byName.put(name, chamber);
        }

        return byName.get(name);
    }
}
