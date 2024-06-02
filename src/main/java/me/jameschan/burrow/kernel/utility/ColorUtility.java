package me.jameschan.burrow.kernel.utility;

import org.springframework.lang.NonNull;
import picocli.CommandLine;

public final class ColorUtility {
  @NonNull
  public static String render(@NonNull final String string, @NonNull final String modifier) {
    return CommandLine.Help.Ansi.ON.string("@|" + modifier.trim() + " " + string + "|@");
  }

  @NonNull
  public static String render(@NonNull final String string, @NonNull final Type type) {
    return render(string, type.getModifier());
  }

  public enum Type {
    SUCCESS("bold,green"),
    ERROR("bold,red"),
    SYMBOL("magenta"),
    KEYWORD("magenta,italic"),
    NULL("red,italic"),
    CHAMBER("bold,fg(134)"),
    DESCRIPTION("fg(222)"),
    KEY("blue"),
    VALUE("green"),
    COMMAND_NAME("cyan"),
    COMMAND_TYPE("bg(21),fg(208)");

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
