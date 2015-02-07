package com.monitorjbl.json;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

public class JsonViewResponseProcessor extends RequestResponseBodyMethodProcessor {
  public JsonViewResponseProcessor(List<HttpMessageConverter<?>> messageConverters) {
    super(messageConverters);
  }
}
