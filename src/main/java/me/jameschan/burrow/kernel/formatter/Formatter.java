package me.jameschan.burrow.kernel.formatter;

import java.util.*;
import me.jameschan.burrow.kernel.Chamber;
import me.jameschan.burrow.kernel.ChamberModule;
import me.jameschan.burrow.kernel.entry.Entry;
import me.jameschan.burrow.kernel.utility.ColorUtility;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Formatter extends ChamberModule {
  public static final String NULL = "null";
  public static final String EMPTY_LIST = "[]";

  public Formatter(final Chamber chamber) {
    super(chamber);
  }

  public String format(final List<?> itemList) {
    if (itemList.isEmpty()) {
      return EMPTY_LIST;
    }

    final var stringList = itemList.stream().map(Object::toString).toList();

    String result = null;
    for (final var furniture : context.getRenovator().getAllFurniture()) {
      result = furniture.formatStringList(stringList);
    }

    return Optional.ofNullable(result).orElse(formatStringList(stringList));
  }

  public String format(final Entry entry) {
    if (entry == null) {
      return NULL;
    } else {
      final var allFurniture = context.getRenovator().getAllFurniture();
      final var formattedObject = new HashMap<>(entry.getProperties());
      allFurniture.forEach(furniture -> furniture.toFormattedObject(formattedObject, entry));

      String result = null;
      for (final var furniture : allFurniture) {
        result = furniture.formatEntry(entry, formattedObject);
      }

      return Optional.ofNullable(result).orElse(formatEntry(entry, formattedObject));
    }
  }

  private String formatStringList(final List<String> itemList) {
    final var lengthSum = itemList.stream().mapToInt(String::length).sum();
    if (lengthSum > 80) {
      final var indentation = " ".repeat(2);
      final var stringBuilder = new StringBuilder();
      stringBuilder.append("[");
      for (final var item : itemList) {
        final var itemString = item.replaceAll("\n", "\n" + indentation);
        stringBuilder.append("\n").append(indentation).append(itemString).append(",");
      }
      stringBuilder.append("\n]");

      return stringBuilder.toString();
    } else {
      return "[" + String.join(", ", itemList) + "]";
    }
  }

  private String formatEntry(final Entry entry, final Map<String, String> formattedObject) {
    final var lines = new ArrayList<String>();
    lines.add("[" + entry.getId() + "] {");
    for (final var objectEntry : formattedObject.entrySet()) {
      final var key = ColorUtility.render(objectEntry.getKey(), ColorUtility.Type.KEY);
      final var value =
          ColorUtility.render('"' + objectEntry.getValue() + '"', ColorUtility.Type.VALUE);
      lines.add("  " + key + ": " + value);
    }
    lines.add("}");

    return String.join("\n", lines);
  }
}
