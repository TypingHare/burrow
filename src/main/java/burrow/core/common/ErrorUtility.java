package burrow.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ErrorUtility {
    public static List<String> getCauseStack(final Throwable throwable) {
        final var lines = new ArrayList<String>();
        for (var ex = throwable; ex != null; ex = ex.getCause()) {
            lines.add(ex.getMessage());
        }

        final AtomicInteger atomicInteger = new AtomicInteger(lines.size());
        return lines.stream()
            .map(line -> "[" + atomicInteger.decrementAndGet() + "] " + line)
            .map(line -> ColorUtility.render(line, ColorUtility.Type.MESSAGE_ERROR))
            .toList();
    }
}
