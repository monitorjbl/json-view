package com.monitorjbl.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Allows runtime alteration of JSON responses
 */
public class JsonView<T> {
  protected final T value;
  protected final Map<Class<?>, Match> matches = new HashMap<>();
  protected MatcherBehavior matcherBehavior;

  protected JsonView(T value) {
    this.value = value;
  }

  T getValue() {
    return value;
  }

  Match getMatch(Class<?> cls) {
    return matches.get(cls);
  }

  public JsonView<T> onClass(Class<?> cls, Match match) {
    matches.put(cls, match);
    return this;
  }

  public JsonView<T> withMatcherBehavior(MatcherBehavior matcherBehavior) {
    this.matcherBehavior = matcherBehavior;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    JsonView<?> jsonView = (JsonView<?>) o;
    return Objects.equals(matches, jsonView.matches) &&
        matcherBehavior == jsonView.matcherBehavior;
  }

  @Override
  public int hashCode() {
    return Objects.hash(matches, matcherBehavior);
  }

  public static <E> JsonView<E> with(E value) {
    return new JsonView<>(value);
  }

}
