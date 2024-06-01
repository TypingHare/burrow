package me.jameschan.burrow.kernel.utility;

import picocli.CommandLine;

public final class ColorUtility {
  public static String render(final String string, final Type type) {
    return CommandLine.Help.Ansi.ON.string("@|" + type.getModifier() + " " + string + "|@");
  }

  public enum Type {
    SYMBOL("magenta"),
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

    public String getModifier() {
      return modifier;
    }
  }
}
