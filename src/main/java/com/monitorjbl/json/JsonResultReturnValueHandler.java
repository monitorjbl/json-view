package com.monitorjbl.json;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

public class JsonResultReturnValueHandler implements HandlerMethodReturnValueHandler {

  private final HandlerMethodReturnValueHandler delegate;

  public JsonResultReturnValueHandler(List<HttpMessageConverter<?>> converters) {
    this.delegate = new JsonResultResponseProcessor(converters);
  }

  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
    Object val = returnValue;
    if (void.class.equals(returnType.getMethod().getReturnType()) && JsonResult.get() != null) {
      val = JsonResult.get();
    }
    delegate.handleReturnValue(val, returnType, mavContainer, webRequest);
  }

}