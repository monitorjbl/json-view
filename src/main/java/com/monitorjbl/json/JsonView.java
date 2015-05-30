package com.monitorjbl.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows runtime alteration of JSON responses
 */
public class JsonView<T> {
  private static final ThreadLocal<JsonView> current = new ThreadLocal<>();

  private final T value;
  private final Map<Class<?>, Match> matches = new HashMap<>();

  private JsonView(T value) {
    this.value = value;
    current.set(this);
  }

  Object getValue() {
    return value;
  }

  Match getMatch(Class<?> cls) {
    return matches.get(cls);
  }

  public JsonView<T> onClass(Class<?> cls, Match match) {
    matches.put(cls, match);
    return this;
  }

  /**
   * Returns the object the {@code JsonView} was initiated with
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public T returnValue() {
    return value;
  }

  public static <E> JsonView<E> with(E value) {
    return new JsonView<>(value);
  }

  static JsonView get() {
    return current == null ? null : current.get();
  }

}
