package com.monitorjbl.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Match {
  private final Set<String> includes = new HashSet<>();
  private final Set<String> excludes = new HashSet<>();

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

  Set<String> getIncludes() {
    return includes;
  }

  Set<String> getExcludes() {
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

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    Match match = (Match) o;

    if(includes != null ? !includes.equals(match.includes) : match.includes != null) return false;
    return excludes != null ? excludes.equals(match.excludes) : match.excludes == null;

  }

  @Override
  public int hashCode() {
    int result = includes != null ? includes.hashCode() : 0;
    result = 31 * result + (excludes != null ? excludes.hashCode() : 0);
    return result;
  }
}
