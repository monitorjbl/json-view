package com.monitorjbl.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonViewModule extends SimpleModule {

  private final JsonViewSerializer jsonView;

  public JsonViewModule() {
    this(new JsonViewSerializer());
  }

  public JsonViewModule(JsonViewSerializer jsonView) {
    super(new Version(0, 16, 0, "", "com.monitorjbl", "json-view"));
    addSerializer(JsonView.class, jsonView);
    this.jsonView = jsonView;
  }

  public <E> JsonViewModule registerSerializer(Class<E> cls, JsonSerializer<E> serializer) {
    jsonView.registerCustomSerializer(cls, serializer);
    return this;
  }
}
