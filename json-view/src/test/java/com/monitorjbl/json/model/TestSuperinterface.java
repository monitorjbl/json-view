package com.monitorjbl.json.model;

public interface TestSuperinterface {

  default String getId() {
    return "ID";
  }

  public static interface TestChildInterface extends TestSuperinterface {
    default String getName() {
      return "NAME";
    }
  }

  public static class TestInterfaceObject implements TestChildInterface {
    private String description;

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }
}
