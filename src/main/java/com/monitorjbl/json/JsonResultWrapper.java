package com.monitorjbl.json;

public class JsonResultWrapper {
  private final JsonResult result;
  private final Object value;

  public JsonResultWrapper(JsonResult result, Object value) {
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
