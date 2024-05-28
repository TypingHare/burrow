package me.jameschan.burrow.kernel.common;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class Types {
  public static final Type STRING_STRING_MAP = new TypeToken<Map<String, String>>() {}.getType();
  public static final Type STRING_STRING_MAP_List =
      new TypeToken<List<Map<String, String>>>() {}.getType();
}
