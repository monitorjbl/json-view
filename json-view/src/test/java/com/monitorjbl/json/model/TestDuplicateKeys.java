package com.monitorjbl.json.model;

import java.util.LinkedHashMap;

public interface TestDuplicateKeys {
  static class ClassA {
    private int id;

    public int getId() {
      return this.id;
    }
  }

  static class ClassB extends ClassA {}

  static class ClassC extends ClassB {}

}
