package com.monitorjbl.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class JsonViewSerializer extends JsonSerializer<JsonView> {
  private final int cacheSize;

  public JsonViewSerializer() {
    this(1000);
  }

  public JsonViewSerializer(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  @Override
  public void serialize(JsonView result, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
    new JsonWriter(jgen, result, cacheSize).write(null, result.getValue());
  }

  static class JsonWriter {
    //caches the results of the @JsonIgnore test to cut down on expensive reflection calls
    static final Map<Field, Boolean> hasJsonIgnoreCache = new ConcurrentHashMap<>();

    Stack<String> path = new Stack<>();
    String currentPath = "";
    Match currentMatch = null;

    final JsonGenerator jgen;
    final JsonView result;
    final int cacheSize;

    JsonWriter(JsonGenerator jgen, JsonView result, int cacheSize) {
      this.jgen = jgen;
      this.result = result;
      this.cacheSize = cacheSize;
    }

    //internal use only to encapsulate what the current state was
    private JsonWriter(JsonGenerator jgen, JsonView result, int cacheSize, Match currentMatch) {
      this.jgen = jgen;
      this.result = result;
      this.cacheSize = cacheSize;
      this.currentMatch = currentMatch;
    }

    boolean writePrimitive(Object obj) throws IOException {
      if(obj instanceof String) {
        jgen.writeString((String) obj);
      } else if(obj instanceof Integer) {
        jgen.writeNumber((Integer) obj);
      } else if(obj instanceof Long) {
        jgen.writeNumber((Long) obj);
      } else if(obj instanceof Short) {
        jgen.writeNumber((Short) obj);
      } else if(obj instanceof Double) {
        jgen.writeNumber((Double) obj);
      } else if(obj instanceof Float) {
        jgen.writeNumber((Float) obj);
      } else if(obj instanceof Character) {
        jgen.writeNumber((Character) obj);
      } else if(obj instanceof Byte) {
        jgen.writeNumber((Byte) obj);
      } else if(obj instanceof Boolean) {
        jgen.writeBoolean((Boolean) obj);
      } else {
        return false;
      }
      return true;
    }

    boolean writeSpecial(Object obj) throws IOException {
      if(obj instanceof Date) {
        jgen.writeNumber(((Date) obj).getTime());
      } else if(obj instanceof URL) {
        jgen.writeString(obj.toString());
      } else if(obj instanceof URI) {
        jgen.writeString(obj.toString());
      } else if(obj instanceof Class) {
        jgen.writeString(((Class) obj).getCanonicalName());
      } else {
        return false;
      }
      return true;
    }

    boolean writeEnum(Object obj) throws IOException {
      if(obj.getClass().isEnum()) {
        jgen.writeString(obj.toString());
      } else {
        return false;
      }
      return true;
    }

    @SuppressWarnings("unchecked")
    boolean writeList(Object obj) throws IOException {
      if(obj instanceof List || obj instanceof Set || obj.getClass().isArray()) {
        Iterable iter;
        if(obj.getClass().isArray()) {
          if(obj instanceof byte[]) {
            jgen.writeBinary((byte[]) obj);
            return true;
          } else {
            iter = convertArray(obj);
          }
        } else {
          iter = (Iterable<Object>) obj;
        }

        jgen.writeStartArray();
        for(Object o : iter) {
          new JsonWriter(jgen, result, cacheSize, currentMatch).write(null, o);
        }
        jgen.writeEndArray();
      } else {
        return false;
      }
      return true;
    }

    @SuppressWarnings("unchecked")
    Iterable convertArray(Object obj) {
      Iterable iter;
      if(obj instanceof int[]) {
        int[] arr = (int[]) obj;
        iter = new ArrayList<>();
        for(int v : arr) {
          ((List<Integer>) iter).add(v);
        }
      } else if(obj instanceof double[]) {
        double[] arr = (double[]) obj;
        iter = new ArrayList<>();
        for(double v : arr) {
          ((List<Double>) iter).add(v);
        }
      } else if(obj instanceof float[]) {
        float[] arr = (float[]) obj;
        iter = new ArrayList<>();
        for(float v : arr) {
          ((List<Float>) iter).add(v);
        }
      } else if(obj instanceof long[]) {
        long[] arr = (long[]) obj;
        iter = new ArrayList<>();
        for(long v : arr) {
          ((List<Long>) iter).add(v);
        }
      } else if(obj instanceof short[]) {
        short[] arr = (short[]) obj;
        iter = new ArrayList<>();
        for(short v : arr) {
          ((List<Short>) iter).add(v);
        }
      } else if(obj instanceof char[]) {
        char[] arr = (char[]) obj;
        iter = new ArrayList<>();
        for(char v : arr) {
          ((List<Character>) iter).add(v);
        }
      } else if(obj instanceof boolean[]) {
        boolean[] arr = (boolean[]) obj;
        iter = new ArrayList<>();
        for(boolean v : arr) {
          ((List<Boolean>) iter).add(v);
        }
      } else {
        iter = Arrays.asList((Object[]) obj);
      }
      return iter;
    }

    @SuppressWarnings("unchecked")
    boolean writeMap(Object obj) throws IOException {
      if(obj instanceof Map) {
        Map<Object, Object> map = (Map<Object, Object>) obj;

        jgen.writeStartObject();
        for(Object key : map.keySet()) {
          jgen.writeFieldName(key.toString());
          new JsonWriter(jgen, result, cacheSize, currentMatch).write(null, map.get(key));
        }
        jgen.writeEndObject();
      } else {
        return false;
      }
      return true;
    }

    void writeObject(Object obj) throws IOException {
      jgen.writeStartObject();

      Class cls = obj.getClass();
      while(!cls.equals(Object.class)) {
        Field[] fields = cls.getDeclaredFields();
        for(Field field : fields) {
          try {
            field.setAccessible(true);
            Object val = field.get(obj);

            if(val != null && fieldAllowed(field, obj.getClass())) {
              String name = field.getName();
              jgen.writeFieldName(name);
              new JsonWriter(jgen, result, cacheSize, currentMatch).write(name, val);
            }
          } catch(IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
        cls = cls.getSuperclass();
      }

      jgen.writeEndObject();
    }

    @SuppressWarnings("unchecked")
    boolean fieldAllowed(Field field, Class declaringClass) {
      String name = field.getName();
      String prefix = currentPath.length() > 0 ? currentPath + "." : "";
      if(Modifier.isStatic(field.getModifiers())) {
        return false;
      }

      //search for matcher
      Match match = null;
      Class cls = declaringClass;
      while(!cls.equals(Object.class) && match == null) {
        match = result.getMatch(cls);
        cls = cls.getSuperclass();
      }
      if(match == null) {
        match = currentMatch;
      } else {
        prefix = "";
      }

      //if there is a match, respect it
      if(match != null) {

        if(currentMatch == null) {
          currentMatch = match;
        }

        int included = containsMatchingPattern(match.getIncludes(), prefix + name);
        int excluded = containsMatchingPattern(match.getExcludes(), prefix + name);

        /*
        The logic for this is a little complex. We're dealing with ternary logic to
        properly handle wildcard matches. We want matches made with wildcards to be
        overruled by matches without them.
         */
        if(included == 1) {
          return true;
        } else if(excluded == 1) {
          return false;
        } else if(included == 0) {
          return true;
        } else if(excluded == 0) {
          return false;
        } else {
          return !annotatedWithIgnore(field);
        }
      } else {
        //else, respect JsonIgnore only
        return !annotatedWithIgnore(field);
      }
    }

    boolean annotatedWithIgnore(Field f) {
      if(!hasJsonIgnoreCache.containsKey(f)) {
        JsonIgnore jsonIgnore = f.getAnnotation(JsonIgnore.class);
        JsonIgnoreProperties ignoreProperties = f.getDeclaringClass().getAnnotation(JsonIgnoreProperties.class);
        if(hasJsonIgnoreCache.size() > cacheSize) {
          hasJsonIgnoreCache.remove(hasJsonIgnoreCache.keySet().iterator().next());
        }
        hasJsonIgnoreCache.put(f, (jsonIgnore != null && jsonIgnore.value()) ||
            (ignoreProperties != null && Arrays.asList(ignoreProperties.value()).contains(f.getName())));
      }
      return hasJsonIgnoreCache.get(f);
    }

    /**
     * Returns one of the following values:
     * <pre>
     * -1: No match found
     *  0: Wildcard-based match
     *  1: Non-wildcard match
     * </pre>
     *
     * @param values
     * @param pattern
     * @return
     */
    int containsMatchingPattern(List<String> values, String pattern) {
      for(String val : values) {
        String replaced = val.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
        if(Pattern.compile(replaced).matcher(pattern).matches()) {
          return replaced.contains("*") ? 0 : 1;
        }
      }
      return -1;
    }

    void write(String fieldName, Object value) throws IOException {
      if(fieldName != null) {
        path.push(fieldName);
        updateCurrentPath();
      }

      //try to handle all primitives/special cases before treating this as json object
      if(value != null && !writePrimitive(value) && !writeSpecial(value) && !writeEnum(value) && !writeList(value) && !writeMap(value)) {
        writeObject(value);
      }

      if(fieldName != null) {
        path.pop();
        updateCurrentPath();
      }
    }

    void updateCurrentPath() {
      StringBuilder builder = new StringBuilder();
      for(String s : path) {
        builder.append(".");
        builder.append(s);
      }
      currentPath = builder.length() > 0 ? builder.toString().substring(1) : "";
    }
  }
}
