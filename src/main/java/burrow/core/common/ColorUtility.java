package burrow.core.common;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import picocli.CommandLine;

public final class ColorUtility {
    @NonNull
    public static String render(@NonNull final String string, @Nullable final String modifier) {
        if (modifier == null) {
            return string;
        }

        return CommandLine.Help.Ansi.ON.string("@|" + modifier.trim() + " " + string + "|@");
    }

    @NonNull
    public static String render(@NonNull final String string, @NonNull final Type type) {
        return render(string, type.getModifier());
    }

    public enum Type {
        // Exit code
        EXIT_CODE_SUCCESS("bold,green"),
        EXIT_CODE_ERROR("bold,red"),

        // Symbol, keyword, and null
        SYMBOL(null),
        KEYWORD("magenta"),
        NULL("bold,magenta"),

        // Key-value pair
        KEY("blue"),
        VALUE("green"),

        // Names
        NAME_CHAMBER("bold,fg(134)"),
        NAME_COMMAND("cyan"),
        NAME_COMMAND_TYPE("bg(21),fg(208)"),

        // Message
        MESSAGE_ERROR("red"),
        DESCRIPTION("fg(222)");

        private final String modifier;

        Type(final String modifier) {
            this.modifier = modifier;
        }

        @NonNull
        public String getModifier() {
            return modifier;
        }
    }
}