package com.monitorjbl.json;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.monitorjbl.json.Memoizer.FunctionCache.FIELD_NAME;
import static com.monitorjbl.json.Memoizer.FunctionCache.IGNORE_ANNOTATIONS;
import static com.monitorjbl.json.Memoizer.FunctionCache.MATCHES;
import static com.monitorjbl.json.Memoizer.FunctionCache.SERIALIZE_ANNOTATIONS;

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

  public <T> T ignoreAnnotations(Field f, Supplier<T> compute) {
    return (T) fitToMaxSize(IGNORE_ANNOTATIONS).computeIfAbsent(new MonoArg(f), (k) -> compute.get());
  }

  public <T> T serializeAnnotations(Field f, Supplier<T> compute) {
    return (T) fitToMaxSize(SERIALIZE_ANNOTATIONS).computeIfAbsent(new MonoArg(f), (k) -> compute.get());
  }

  public <T> T matches(Set<String> values, String pattern, boolean matchPrefix, Supplier<T> compute) {
    return (T) fitToMaxSize(MATCHES).computeIfAbsent(new TriArg(values, pattern, matchPrefix), (k) -> compute.get());
  }

  public <T> T fieldName(Field f, Supplier<T> compute) {
    return (T) fitToMaxSize(FIELD_NAME).computeIfAbsent(new MonoArg(f), (k) -> compute.get());
  }

  private Map<Arg, Object> fitToMaxSize(FunctionCache key) {
    Map<Arg, Object> map = cache.get(key);
    if(map.size() > maxCacheSize) {
      map.remove(map.keySet().iterator().next());
    }
    return map;
  }

  enum FunctionCache {
    IGNORE_ANNOTATIONS, SERIALIZE_ANNOTATIONS, MATCHES, FIELD_NAME
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
  }
}
