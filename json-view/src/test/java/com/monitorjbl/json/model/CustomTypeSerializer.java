package com.monitorjbl.json.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomTypeSerializer
    extends StdSerializer<CustomType> {
  private static final long serialVersionUID = 5275226323866715671L;

  public CustomTypeSerializer() {
    super(CustomType.class, true);
  }

  @Override
  public void serialize(CustomType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    if(value == null) {
      jgen.writeNull();
    } else {
      jgen.writeString(value.getSid() + "[" + value.getName() + "]");
    }
  }

}
