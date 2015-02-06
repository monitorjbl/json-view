package com.monitorjbl.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.test.TestObject;
import com.monitorjbl.test.TestSubobject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class JsonViewSerializerTest {

  ObjectMapper sut;

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonResultWrapper.class, new JsonViewSerializer());
    sut.registerModule(module);
  }

  @Test
  public void test() throws JsonProcessingException {

    System.out.println(sut.writeValueAsString(new JsonResultWrapper(
        new JsonResult()
            .exclude("str2")
            .exclude("sub.val"),
        TestObject.builder()
            .int1(1)
            .str2("asdf")
            .array(new String[]{"apple", "banana"})
            .list(Arrays.asList("red", "blue", "green"))
            .sub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")))
            .build())));
  }
}
