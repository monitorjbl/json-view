package com.monitorjbl.json;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

public class JsonViewSupportFactoryBean implements InitializingBean {

  @Autowired
  private RequestMappingHandlerAdapter adapter;

  @Override
  public void afterPropertiesSet() throws Exception {
    List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(adapter.getReturnValueHandlers());
    decorateHandlers(handlers);
    adapter.setReturnValueHandlers(handlers);
  }

  private void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
    for (HandlerMethodReturnValueHandler handler : handlers) {
      if (handler instanceof RequestResponseBodyMethodProcessor) {
        ViewInjectingReturnValueHandler decorator = new ViewInjectingReturnValueHandler(handler);
        int index = handlers.indexOf(handler);
        handlers.set(index, decorator);
        break;
      }
    }
  }

}