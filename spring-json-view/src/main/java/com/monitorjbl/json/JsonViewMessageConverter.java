package com.monitorjbl.json;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;

public class JsonViewMessageConverter extends MappingJackson2HttpMessageConverter {

  private JsonViewSerializer serializer = new JsonViewSerializer();

  public JsonViewMessageConverter() {
    super();
    ObjectMapper defaultMapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, this.serializer);
    defaultMapper.registerModule(module);
    setObjectMapper(defaultMapper);
  }

  public JsonViewMessageConverter(ObjectMapper mapper) {
    super();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, this.serializer);
    mapper.registerModule(module);
    setObjectMapper(mapper);
  }

  /**
   * Registering custom serializer allows to the JSonView to deal with custom serializations for certains field types.<br>
   * This way you could register for instance a JODA serialization as  a DateTimeSerializer. <br>
   * Thus, when JSonView find a field of that type (DateTime), it will delegate the serialization to the serializer specified.<br>
   * Example:<br>
   * <code>
   * JsonViewSupportFactoryBean bean = new JsonViewSupportFactoryBean( mapper );
   * bean.registerCustomSerializer( DateTime.class, new DateTimeSerializer() );
   * </code>
   *
   * @param <T>     Type class of the serializer
   * @param class1  {@link Class} the class type you want to add a custom serializer
   * @param forType {@link JsonSerializer} the serializer you want to apply for that type
   */
  public <T> void registerCustomSerializer(Class<T> class1, JsonSerializer<T> forType) {
    this.serializer.registerCustomSerializer(class1, forType);
  }

  /**
   * Unregister a previously registtered serializer. @see registerCustomSerializer
   *
   * @param <T>    Type class of the serializer
   * @param class1 {@link Class} the class type for which you want to remove a custom serializer
   */
  public <T> void unregisterCustomSerializer(Class<T> class1) {
    this.serializer.unregisterCustomSerializer(class1);
  }

  @Override
  protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    super.writeInternal(object, outputMessage);
  }

}