package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.json.model.TestChildObject;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings("unchecked")
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
  public void testJsonIgnore() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoredDirect("ignore me");
    String serialized = sut.writeValueAsString(new JsonWrapper(new JsonResult(), ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoredDirect"));
  }
  
  @Test
  public void testJsonIgnoreProperties() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoreIndirect("ignore me");
    String serialized = sut.writeValueAsString(new JsonWrapper(new JsonResult(), ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoreIndirect"));
  }

  @Test
  public void testBasicSerialization() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr2("asdf");
    ref.setArray(new String[]{"apple", "banana"});
    ref.setList(Arrays.asList("red", "blue", "green"));
    ref.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));
    String serialized = sut.writeValueAsString(new JsonWrapper(
        new JsonResult()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect"), ref));

    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("str2"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
  }

  @Test
  public void testInheritance() throws IOException {
    TestChildObject ref = new TestChildObject();
    ref.setChildField("green");
    ref.setIgnoredDirect("ignore me");
    ref.setIgnoreIndirect("ignore me too");
    ref.setArray(new String[]{"pizza", "french fry"});

    String serialized = sut.writeValueAsString(new JsonWrapper(
        new JsonResult()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect"), ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("ignoredIndirect"));
    assertNotNull(obj.get("ignoredDirect"));
    assertEquals(ref.getIgnoredDirect(), obj.get("ignoredDirect"));
    assertNotNull(obj.get("childField"));
    assertEquals(ref.getChildField(), obj.get("childField"));
  }
}
