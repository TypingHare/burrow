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

        for (int i = 0; i < lines.size(); i++) {
            final var line = "[" + i + "] " + throwable.getClass().getName() + " - " + lines.get(i);
            lines.set(i, ColorUtility.render(line, ColorUtility.Type.MESSAGE_ERROR));
        }

        return lines;
    }
}
