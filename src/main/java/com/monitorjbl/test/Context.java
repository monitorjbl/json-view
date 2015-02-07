package com.monitorjbl.test;

import com.monitorjbl.json.JsonViewSupportFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan({"com.monitorjbl"})
public class Context extends WebMvcConfigurerAdapter {
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean();
  }
}
