package burrow.core.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

public final class ColorUtility {
    @NotNull
    public static String render(@NotNull final String string, @Nullable final String modifier) {
        if (modifier == null) {
            return string;
        }

        return CommandLine.Help.Ansi.ON.string("@|" + modifier.trim() + " " + string + "|@");
    }

    @NotNull
    public static String render(@NotNull final String string, @NotNull final Type type) {
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
        NAME_CHAMBER("fg(134)"),
        NAME_COMMAND("cyan"),
        NAME_COMMAND_TYPE("bg(21),fg(208)"),
        NAME_FURNITURE("blue"),

        // Message
        MESSAGE_ERROR("red"),
        DESCRIPTION("fg(222)");

        private final String modifier;

        Type(final String modifier) {
            this.modifier = modifier;
        }

        @NotNull
        public String getModifier() {
            return modifier;
        }
    }
}