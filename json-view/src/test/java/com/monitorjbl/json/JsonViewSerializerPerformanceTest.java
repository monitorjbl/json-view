package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.monitorjbl.json.Match.match;

@RunWith(Parameterized.class)
public class JsonViewSerializerPerformanceTest {
  private int repetitions;
  private JsonViewSerializer serializer = new JsonViewSerializer();
  private ObjectMapper sut;
  private ObjectMapper compare = new ObjectMapper();

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {500}, {1000}, {10000}, {100000}, {1000000}
    });
  }

  public JsonViewSerializerPerformanceTest(int repetitions) {
    this.repetitions = repetitions;
  }

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, serializer);
    sut.registerModule(module);
  }

  @Test
  public void comparePerformance() throws Exception {
    long baselineTimes = baselineRandomSingleObjectPerformance();
    long jsonViewTimes = jsonViewRandomSingleObjectPerformance();
    long difference = (long) (((double) jsonViewTimes) / (double) baselineTimes) * 100L;

    System.out.printf("[%-8s]: | Baseline: %-8s | JsonView: %-8s | Difference: %-6s |\n",
        repetitions, (baselineTimes / 1000000) + "ms", (jsonViewTimes / 1000000) + "ms", difference + "%");
  }

  public long jsonViewRandomSingleObjectPerformance() throws Exception {
    long times = 0;
    for(int i = 0; i < repetitions; i++) {
      TestObject ref = testObject();

      long time = System.nanoTime();
      sut.writeValueAsString(JsonView.with(ref).onClass(TestObject.class, match()
          .exclude("int1")));
      time = System.nanoTime() - time;
      if(i > 100) {
        times += time;
      }
    }

    return times;
  }

  public long baselineRandomSingleObjectPerformance() throws Exception {
    long times = 0;
    for(int i = 0; i < repetitions; i++) {
      TestObject ref = testObject();

      long time = System.nanoTime();
      compare.writeValueAsString(ref);
      time = System.nanoTime() - time;
      if(i > 100) {
        times += time;
      }
    }
    return times;
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