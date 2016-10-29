package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonViewSupportFactoryBean implements InitializingBean {
  private static final Logger log = LoggerFactory.getLogger(JsonViewSupportFactoryBean.class);

  @Autowired
  private RequestMappingHandlerAdapter adapter;

  private final JsonViewMessageConverter converter;
  private final DefaultView defaultView;

  public JsonViewSupportFactoryBean() {
    this(new ObjectMapper());
  }

  public JsonViewSupportFactoryBean(ObjectMapper mapper) {
    this(new JsonViewMessageConverter(mapper.copy()), DefaultView.create());
  }

  public JsonViewSupportFactoryBean(DefaultView defaultView) {
    this(new JsonViewMessageConverter(new ObjectMapper()), defaultView);
  }

  public JsonViewSupportFactoryBean(ObjectMapper mapper, DefaultView defaultView) {
    this(new JsonViewMessageConverter(mapper.copy()), defaultView);
  }

  private JsonViewSupportFactoryBean(JsonViewMessageConverter converter, DefaultView defaultView) {
    this.converter = converter;
    this.defaultView = defaultView;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(adapter.getReturnValueHandlers());

    List<HttpMessageConverter<?>> converters = removeJackson(adapter.getMessageConverters());
    converters.add(converter);
    adapter.setMessageConverters(converters);

    decorateHandlers(handlers);
    adapter.setReturnValueHandlers(handlers);
  }

  private List<HttpMessageConverter<?>> removeJackson(List<HttpMessageConverter<?>> converters) {
    List<HttpMessageConverter<?>> copy = new ArrayList<>(converters);
    Iterator<HttpMessageConverter<?>> iter = copy.iterator();
    while(iter.hasNext()) {
      HttpMessageConverter<?> next = iter.next();
      if(next instanceof MappingJackson2HttpMessageConverter) {
        log.debug("Removing MappingJackson2HttpMessageConverter as it interferes with us");
        iter.remove();
      }
    }
    return copy;
  }

  private void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
    List<HttpMessageConverter<?>> converters = new ArrayList<>(adapter.getMessageConverters());
    converters.add(converter);
    for(HandlerMethodReturnValueHandler handler : handlers) {
      int index = handlers.indexOf(handler);
      if(handler instanceof HttpEntityMethodProcessor) {
        handlers.set(index, new JsonViewHttpEntityMethodProcessor(converters));
      } else if(handler instanceof RequestResponseBodyMethodProcessor) {
        handlers.set(index, new JsonViewReturnValueHandler(converters, defaultView));
        break;
      }
    }
  }

}