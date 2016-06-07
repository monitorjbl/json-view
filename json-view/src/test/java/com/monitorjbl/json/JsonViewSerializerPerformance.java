package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;

@Ignore
public class JsonViewSerializerPerformance {
    public static final int REPETITIONS = 100000000;
  ObjectMapper sut;
  ObjectMapper compare = new ObjectMapper();

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, new JsonViewSerializer());
    sut.registerModule(module);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void sutRandomSingleObjectPerformance() throws Exception {
    long times = 0;
    for (int i = 0; i < REPETITIONS; i++) {
      TestObject ref = testObject();

      long time = System.nanoTime();
      String serialized = sut.writeValueAsString(JsonView.with(ref));
      time = System.nanoTime() - time;
      if (i > 100) {
        times += time;
      }
    }

    //System.out.println(times / (REPETITIONS - 100));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void compareRandomSingleObjectPerformance() throws Exception {
    long times = 0;
    for (int i = 0; i < REPETITIONS; i++) {
      TestObject ref = testObject();

      long time = System.nanoTime();
      String serialized = compare.writeValueAsString(ref);
      time = System.nanoTime() - time;
      if (i > 100) {
        times += time;
      }
    }

    //System.out.println(times / (REPETITIONS - 100));
  }

  TestObject testObject() {
    TestObject ref = new TestObject();
    ref.setInt1(RandomUtils.nextInt(0, 100000000));
    ref.setIgnoredDirect(RandomStringUtils.random(16));
    ref.setStr2(RandomStringUtils.random(4));
    ref.setStringArray(new String[]{RandomStringUtils.random(7), RandomStringUtils.random(5)});
    ref.setList(newArrayList(RandomStringUtils.random(3), RandomStringUtils.random(14)));
    ref.setMapOfObjects(ImmutableMap.of(RandomStringUtils.random(14), new TestSubobject()));
    TestSubobject sub = new TestSubobject();
    sub.setVal("wer");
    ref.setListOfObjects(newArrayList(sub));
    return ref;
  }
}