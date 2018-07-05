package com.monitorjbl.json;

import com.monitorjbl.json.JsonViewSerializer.AccessibleProperty;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.monitorjbl.json.Memoizer.FunctionCache.ACCESSIBLE_PROPERTY;
import static com.monitorjbl.json.Memoizer.FunctionCache.ANNOTATIONS;
import static com.monitorjbl.json.Memoizer.FunctionCache.CLASS_MATCHES;
import static com.monitorjbl.json.Memoizer.FunctionCache.IGNORE_ANNOTATIONS;
import static com.monitorjbl.json.Memoizer.FunctionCache.MATCHES;

@SuppressWarnings("unchecked")
class Memoizer {
  private final int maxCacheSize;
  private final Map<FunctionCache, Map<Arg, Object>> cache = new EnumMap<>(FunctionCache.class);

  public Memoizer(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
    for(FunctionCache key : FunctionCache.class.getEnumConstants()) {
      cache.put(key, new ConcurrentHashMap<>());
    }
  }

  public <T> T matches(Set<String> values, String pattern, boolean matchPrefix, Supplier<T> compute) {
    return computeIfAbsent(MATCHES, new TriArg(values, pattern, matchPrefix), compute);
  }

  public <T> T classMatches(JsonView jsonView, Class cls, Supplier<T> compute) {
    return computeIfAbsent(CLASS_MATCHES, new BiArg(jsonView, cls), compute);
  }

  public <T> T annotations(Class cls, Supplier<T> compute) {
    return computeIfAbsent(ANNOTATIONS, new MonoArg(cls), compute);
  }

  public <T> T annotatedWithIgnore(AccessibleProperty property, Supplier<T> compute) {
    return computeIfAbsent(IGNORE_ANNOTATIONS, new MonoArg(property), compute);
  }

  public <T> T accessibleProperty(Class cls, Supplier<T> compute) {
    return computeIfAbsent(ACCESSIBLE_PROPERTY, new MonoArg(cls), compute);
  }

  public <T> T computeIfAbsent(FunctionCache cacheName, Arg arg, Supplier<T> compute) {
    Map<Arg, Object> map = fitToMaxSize(cacheName);
    if(!map.containsKey(arg)) {
      map.put(arg, compute.get());
    }
    return (T) map.get(arg);
  }

  private Map<Arg, Object> fitToMaxSize(FunctionCache key) {
    Map<Arg, Object> map = cache.get(key);
    if(map.size() > maxCacheSize) {
      map.remove(map.keySet().iterator().next());
    }
    return map;
  }

  enum FunctionCache {
    IGNORE_ANNOTATIONS, MATCHES, ANNOTATIONS, ACCESSIBLE_PROPERTY, CLASS_MATCHES
  }

  private interface Arg {}

  private class MonoArg implements Arg {
    private final Object arg1;

    public MonoArg(Object arg1) {
      this.arg1 = arg1;
    }

    @Override
    public boolean equals(Object o) {
      MonoArg monoArg = (MonoArg) o;

      return arg1 != null ? arg1.equals(monoArg.arg1) : monoArg.arg1 == null;
    }

    @Override
    public int hashCode() {
      return arg1 != null ? arg1.hashCode() : 0;
    }

    @Override
    public String toString() {
      return "MonoArg{" +
          "arg1=" + arg1 +
          '}';
    }
  }

  private class BiArg implements Arg {
    private final Object arg1;
    private final Object arg2;

    public BiArg(Object arg1, Object arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
    }

    @Override
    public boolean equals(Object o) {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;
      BiArg biArg = (BiArg) o;
      return Objects.equals(arg1, biArg.arg1) &&
          Objects.equals(arg2, biArg.arg2);
    }

    @Override
    public int hashCode() {
      return Objects.hash(arg1, arg2);
    }

    @Override
    public String toString() {
      return "BiArg{" +
          "arg1=" + arg1 +
          ", arg2=" + arg2 +
          '}';
    }
  }

  private class TriArg implements Arg {
    private final Object arg1;
    private final Object arg2;
    private final Object arg3;

    public TriArg(Object arg1, Object arg2, Object arg3) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.arg3 = arg3;
    }

    @Override
    public boolean equals(Object o) {
      TriArg triArg = (TriArg) o;

      if(arg1 != null ? !arg1.equals(triArg.arg1) : triArg.arg1 != null) return false;
      if(arg2 != null ? !arg2.equals(triArg.arg2) : triArg.arg2 != null) return false;
      return arg3 != null ? arg3.equals(triArg.arg3) : triArg.arg3 == null;
    }

    @Override
    public int hashCode() {
      int result = arg1 != null ? arg1.hashCode() : 0;
      result = 31 * result + (arg2 != null ? arg2.hashCode() : 0);
      result = 31 * result + (arg3 != null ? arg3.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "TriArg{" +
          "arg1=" + arg1 +
          ", arg2=" + arg2 +
          ", arg3=" + arg3 +
          '}';
    }
  }
}
