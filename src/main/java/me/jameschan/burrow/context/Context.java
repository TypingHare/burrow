package me.jameschan.burrow.context;

import java.util.HashMap;
import java.util.Map;

public abstract class Context {
  // Internal map to store data
  protected final Map<String, Object> data = new HashMap<>();
}
