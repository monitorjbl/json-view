package com.monitorjbl.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonResultRetriever {
  private static final Logger log = LoggerFactory.getLogger(JsonResultRetriever.class);

  static boolean hasValue() {
    return JsonResult.get() != null;
  }

  static JsonView retrieve() {
    JsonView val = JsonResult.get();
    JsonResult.unset();
    return val;
  }
}
