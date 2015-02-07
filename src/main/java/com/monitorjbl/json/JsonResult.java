package com.monitorjbl.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows runtime alteration of JSON responses
 */
public class JsonResult {
  private static ThreadLocal<JsonResult> current;

  private final Object value;
  private final Map<Class<?>, Match> matches = new HashMap<>();

  JsonResult(Object value) {
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

  public JsonResult onClass(Class<?> cls, Match match) {
    matches.put(cls, match);
    return this;
  }

  public static JsonResult with(Object value) {
    return new JsonResult(value);
  }

  static JsonResult get() {
    return current == null ? null : current.get();
  }

}
