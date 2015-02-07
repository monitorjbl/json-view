package com.monitorjbl.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

public class JsonWrapperSerializer extends JsonSerializer<JsonWrapper> {

  @Override
  public void serialize(JsonWrapper wrapper, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
    new Writer(jgen, wrapper.getResult()).write(null, wrapper.getValue());
  }

  private static class Writer {
    Stack<String> path = new Stack<>();
    String currentPath = "";

    JsonGenerator jgen;
    JsonResult result;

    public Writer(JsonGenerator jgen, JsonResult result) {
      this.jgen = jgen;
      this.result = result;
    }

    boolean writePrimitive(Object obj) throws IOException {
      if (obj instanceof String) {
        jgen.writeString((String) obj);
      } else if (Integer.class.isInstance(obj)) {
        jgen.writeNumber((Integer) obj);
      } else if (Double.class.isInstance(obj)) {
        jgen.writeNumber((Double) obj);
      } else if (Float.class.isInstance(obj)) {
        jgen.writeNumber((Float) obj);
      } else if (Boolean.class.isInstance(obj)) {
        jgen.writeBoolean((Boolean) obj);
      } else if (obj instanceof List) {
        jgen.writeObject(obj);
      } else if (obj instanceof Map) {
        jgen.writeObject(obj);
      } else if (obj instanceof Set) {
        jgen.writeObject(obj);
      } else if (obj.getClass().isArray()) {
        jgen.writeObject(obj);
      } else {
        return false;
      }
      return true;
    }

    void writeObject(Object obj) throws IOException {
      jgen.writeStartObject();

      Class cls = obj.getClass();
      while (!cls.equals(Object.class)) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
          try {
            field.setAccessible(true);
            Object val = field.get(obj);

            if (val != null && fieldAllowed(field)) {
              String name = field.getName();
              jgen.writeFieldName(name);
              write(name, val);
            }
          } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
        cls = cls.getSuperclass();
      }

      jgen.writeEndObject();
    }

    boolean fieldAllowed(Field field) {
      String name = field.getName();
      String prefix = currentPath.length() > 0 ? currentPath + "." : "";
      return (result.getIncludes().contains(prefix + name) || !annotatedWithIgnore(field)) && !result.getExcludes().contains(prefix + name);
    }

    //TODO: respect class inheritance
    boolean annotatedWithIgnore(Field f) {
      JsonIgnore jsonIgnore = f.getAnnotation(JsonIgnore.class);
      JsonIgnoreProperties ignoreProperties = f.getDeclaringClass().getAnnotation(JsonIgnoreProperties.class);
      return (jsonIgnore != null && jsonIgnore.value()) ||
          (ignoreProperties != null && Arrays.asList(ignoreProperties.value()).contains(f.getName()));
    }

    void write(String fieldName, Object value) throws IOException {
      if (fieldName != null) {
        path.push(fieldName);
        updateCurrentPath();
      }

      if (value != null && !writePrimitive(value)) {
        writeObject(value);
      }

      if (fieldName != null) {
        path.pop();
        updateCurrentPath();
      }
    }

    void updateCurrentPath() {
      StringBuilder builder = new StringBuilder();
      for (String s : path) {
        builder.append(".");
        builder.append(s);
      }
      currentPath = builder.length() > 0 ? builder.toString().substring(1) : "";
    }
  }
}
