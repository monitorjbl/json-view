package com.monitorjbl.json;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

public class ViewInjectingReturnValueHandler implements HandlerMethodReturnValueHandler {

  private final HandlerMethodReturnValueHandler delegate;

  public ViewInjectingReturnValueHandler(List<HttpMessageConverter<?>> converters) {
    this.delegate = new JsonResponseProcessor(converters);
  }

  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
    Object val = returnValue;
    for (Class<?> t : returnType.getMethod().getParameterTypes()) {
      if (JsonResult.class.equals(t)) {
        val = new JsonWrapper(JsonResult.get(), returnValue);
      }
    }
    delegate.handleReturnValue(val, returnType, mavContainer, webRequest);
  }

}