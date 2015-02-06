package com.monitorjbl.json;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

public class ViewInjectingReturnValueHandler implements HandlerMethodReturnValueHandler {

  private final HandlerMethodReturnValueHandler delegate;

  public ViewInjectingReturnValueHandler(HandlerMethodReturnValueHandler delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean supportsReturnType(MethodParameter returnType) {
    for (Class<?> t : returnType.getMethod().getParameterTypes()) {
      if (JsonResult.class.equals(t)) {
        return true;
      }
    }
    return delegate.supportsReturnType(returnType);
  }

  @Override
  public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
    JsonResult result = JsonResult.get();
    delegate.handleReturnValue(new JsonResultWrapper(result, returnValue), returnType, mavContainer, webRequest);
  }

}