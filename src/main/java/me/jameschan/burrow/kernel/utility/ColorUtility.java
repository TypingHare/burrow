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
    SYMBOL("magenta"),
    NULL("red,italic"),
    CHAMBER("bold,cyan"),
    DESCRIPTION("yellow"),
    KEY("blue"),
    VALUE("green"),
    SUCCESS("bold,green"),
    ERROR("bold,red");

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
