package com.monitorjbl.json.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

public class TestAutodetect {

  public static class AutodetectNotPresent {
    private String id;

    public String getId() {
      return "valid";
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonAutoDetect
  public static class AutodetectDefault {
    private String id;

    public String getId() {
      return "valid";
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
  public static class AutodetectFields {
    private String id;

    public void setId(String id) {
      this.id = id;
    }

    private String getId() {
      return "not_valid";
    }
  }

  @JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.ANY)
  public static class AutodetectGetters {
    private String id;

    public void setId(String id) {
      this.id = id;
    }

    private String getId() {
      return "valid";
    }
  }
}