package com.monitorjbl.json.server;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Configures Jackson mapper for unit tests. Works with XML-based configuration
 * if it is included as a bean.
 */
public class JacksonConfiguration {
  private final ObjectMapper mapper;

  public JacksonConfiguration(ObjectMapper mapper) {
    this.mapper = mapper;
    configureJackson(mapper);
  }

  public static ObjectMapper configureJackson(ObjectMapper mapper) {
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }
}
