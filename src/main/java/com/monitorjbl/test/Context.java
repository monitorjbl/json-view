package com.monitorjbl.test;

import com.monitorjbl.json.JsonViewSupportFactoryBean;
import com.monitorjbl.json.ViewAwareJsonMessageConverter;
import com.monitorjbl.json.ViewWebArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@EnableWebMvc
@Configuration
@ComponentScan({"com.monitorjbl"})
public class Context extends WebMvcConfigurerAdapter {
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new ViewAwareJsonMessageConverter());
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new ViewWebArgumentResolver());
  }

  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean();
  }
}
