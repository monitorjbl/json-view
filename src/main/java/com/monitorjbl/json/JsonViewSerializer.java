package com.monitorjbl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class JsonViewSerializer extends JsonSerializer<JsonResultWrapper> {

  @Override
  public void serialize(JsonResultWrapper wrapper, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
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
      Field[] fields = obj.getClass().getDeclaredFields();

      for (Field field : fields) {
        try {
          field.setAccessible(true);
          String name = field.getName();
          Object val = field.get(obj);

          if (val != null && fieldAllowed(name)) {
            jgen.writeFieldName(name);
            write(name, val);
          }
        } catch (IllegalArgumentException | IllegalAccessException e) {
          e.printStackTrace();
        }

      }
      jgen.writeEndObject();
    }

    boolean fieldAllowed(String name) {
      String prefix = currentPath.length() > 0 ? currentPath + "." : "";
      if (result.getIncludes().size() > 0) {
        return result.getIncludes().contains(prefix + name);
      } else if (result.getExcludes().size() > 0) {
        return !result.getExcludes().contains(prefix + name);
      } else {
        return true;
      }
    }

    void write(String fieldName, Object value) throws IOException {
      if (fieldName != null) {
        path.push(fieldName);
        StringBuilder builder = new StringBuilder();
        for (String s : path) {
          builder.append(".");
          builder.append(s);
        }
        currentPath = builder.toString().substring(1);
        System.out.println(currentPath);
      }

      if (value != null && !writePrimitive(value)) {
        writeObject(value);
      }

      if (fieldName != null) {
        path.pop();
      }
    }
  }
}