package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.test.TestObject;
import com.monitorjbl.test.TestSubobject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonWrapperSerializerTest {

  ObjectMapper sut;

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonWrapper.class, new JsonWrapperSerializer());
    sut.registerModule(module);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testBasics() throws IOException {
    TestObject ref = TestObject.builder()
        .int1(1)
        .str2("asdf")
        .ignoredDirect("ignore me")
        .ignoreIndirect("ignore me too")
        .array(new String[]{"apple", "banana"})
        .list(Arrays.asList("red", "blue", "green"))
        .sub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")))
        .build();
    String serialized = sut.writeValueAsString(new JsonWrapper(
        new JsonResult()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect"), ref));

    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("str2"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
    assertNotNull(obj.get("ignoredDirect"));
    assertEquals(ref.getIgnoredDirect(), obj.get("ignoredDirect"));
  }
}
