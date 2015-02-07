package com.monitorjbl.json;

import java.util.ArrayList;
import java.util.List;

public class Match {
  private final List<String> includes = new ArrayList<>();
  private final List<String> excludes = new ArrayList<>();

  Match() {

  }

  public Match include(String field) {
    includes.add(field);
    return this;
  }

  public Match exclude(String field) {
    excludes.add(field);
    return this;
  }

  List<String> getIncludes() {
    return includes;
  }

  List<String> getExcludes() {
    return excludes;
  }

  public static Match on() {
    return new Match();
  }
}
