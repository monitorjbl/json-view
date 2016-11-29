package com.monitorjbl.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonViewModule extends SimpleModule {

  public JsonViewModule() {
    this(new JsonViewSerializer());
  }

  public JsonViewModule(JsonViewSerializer serializer) {
    super(new Version(0, 14, 0, "", "com.monitorjbl", "json-view"));
    addSerializer(JsonView.class, serializer);
  }

}
