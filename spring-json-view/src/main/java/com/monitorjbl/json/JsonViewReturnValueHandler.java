package com.monitorjbl.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

public class JsonViewReturnValueHandler implements HandlerMethodReturnValueHandler {
  private static final Logger log = LoggerFactory.getLogger(JsonViewReturnValueHandler.class);

  private final HandlerMethodReturnValueHandler delegate;

  public JsonViewReturnValueHandler(List<HttpMessageConverter<?>> converters) {
    this.delegate = new JsonViewResponseProcessor(converters);
  }

  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
    Object val = returnValue;
    if(JsonResultRetriever.hasValue()) {
      val = JsonResultRetriever.retrieve();
      log.debug("Found [" + ((JsonView) val).getValue().getClass() + "] to serialize");
    } else {
      log.debug("No JsonView found for thread, using returned value");
    }

    delegate.handleReturnValue(val, returnType, mavContainer, webRequest);
  }

}