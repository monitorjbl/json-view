package com.monitorjbl.json;

public class JsonWrapper {
  private final JsonResult result;
  private final Object value;

  public JsonWrapper(JsonResult result, Object value) {
    this.result = result;
    this.value = value;
  }

  public JsonResult getResult() {
    return result;
  }

  public Object getValue() {
    return value;
  }
}
