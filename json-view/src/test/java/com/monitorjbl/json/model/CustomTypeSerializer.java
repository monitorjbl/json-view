package com.monitorjbl.json.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class CustomTypeSerializer
    extends StdSerializer<CustomType>
{
    private static final long serialVersionUID = 5275226323866715671L;

    public CustomTypeSerializer()
    {
        super( CustomType.class, true );
    }

    @Override
    public void serialize( CustomType value, JsonGenerator jgen, SerializerProvider provider )
        throws IOException, JsonGenerationException
    {
        jgen.writeString( value.getSid() + "[" + value.getName() + "]" );
    }

}
