package com.monitorjbl.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows runtime alteration of JSON responses
 */
public class JsonResult {
  private static ThreadLocal<JsonResult> current;

  private final List<String> includes = new ArrayList<>();
  private final List<String> excludes = new ArrayList<>();

  JsonResult() {
    current = new ThreadLocal<>();
    current.set(this);
  }

  public JsonResult include(String field) {
    includes.add(field);
    return this;
  }

  public JsonResult exclude(String field) {
    excludes.add(field);
    return this;
  }

  List<String> getIncludes() {
    return includes;
  }

  List<String> getExcludes() {
    return excludes;
  }

  static JsonResult get() {
    return current.get();
  }
}
