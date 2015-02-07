package com.monitorjbl.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows runtime alteration of JSON responses
 */
public class JsonView {
  private static ThreadLocal<JsonView> current;

  private final Object value;
  private final Map<Class<?>, Match> matches = new HashMap<>();

  private JsonView(Object value) {
    this.value = value;
    current = new ThreadLocal<>();
    current.set(this);
  }

  Object getValue() {
    return value;
  }

  Match getMatch(Class<?> cls) {
    return matches.get(cls);
  }

  public JsonView onClass(Class<?> cls, Match match) {
    matches.put(cls, match);
    return this;
  }

  public static JsonView with(Object value) {
    return new JsonView(value);
  }

  static JsonView get() {
    return current == null ? null : current.get();
  }

}
