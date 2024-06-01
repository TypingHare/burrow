package me.jameschan.burrow.kernel.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CommandUtility {

  /**
   * Constructs the original command string from an array of arguments.
   *
   * @param args an array of command arguments
   * @return the original command string
   */
  public static String getOriginalCommand(final String[] args) {
    return Arrays.stream(args)
        .map(arg -> arg.contains(" ") ? "\"" + arg + "\"" : arg)
        .collect(Collectors.joining(" "));
  }

  /**
   * Splits a command string into individual arguments, handling quoted strings as single arguments.
   *
   * @param input the command string
   * @return a list of arguments
   */
  public static List<String> splitArguments(String input) {
    final List<String> arguments = new ArrayList<>();
    final Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
    while (matcher.find()) {
      arguments.add(matcher.group(1).replace("\"", ""));
    }

    return arguments;
  }
}
