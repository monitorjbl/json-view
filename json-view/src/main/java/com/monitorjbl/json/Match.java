package com.monitorjbl.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Match {
  private final List<String> includes = new ArrayList<>();
  private final List<String> excludes = new ArrayList<>();

  Match() {

  }

  public Match include(String... fields) {
    if (fields != null) {
      includes.addAll(Arrays.asList(fields));
    }
    return this;
  }

  public Match exclude(String... fields) {
    if (fields != null) {
      excludes.addAll(Arrays.asList(fields));
    }
    return this;
  }

  List<String> getIncludes() {
    return includes;
  }

  List<String> getExcludes() {
    return excludes;
  }

  public static Match match() {
    return new Match();
  }

  @Override
  public String toString() {
    return "Match{" +
        "includes=" + includes +
        ", excludes=" + excludes +
        '}';
  }
}
