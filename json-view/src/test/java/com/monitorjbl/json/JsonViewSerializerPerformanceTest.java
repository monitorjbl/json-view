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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.monitorjbl.json.Match.match;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class JsonViewSerializerPerformanceTest {
  private static final Logger log = LoggerFactory.getLogger(JsonViewSerializerPerformanceTest.class);
  private int repetitions;
  private JsonViewSerializer serializer = new JsonViewSerializer();
  private ObjectMapper sut;
  private ObjectMapper compare = new ObjectMapper();

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {100},
        {1_000},
        {10_000},
        {100_000},
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
    long baselineTimes = randomSingleObjectPerformance(() -> {
      // Include this because we're testing the serializer. It's not apples-to-apples if we don't
      // include the same pre-work.
      JsonView.with(testObject()).onClass(TestObject.class, match().exclude("int1"));
      compare.writeValueAsString(testObject());
    });
    long jsonViewTimes = randomSingleObjectPerformance(() ->
        sut.writeValueAsString(JsonView.with(testObject()).onClass(TestObject.class, match().exclude("int1"))));
    String difference = divide(jsonViewTimes * 100L, baselineTimes);

    System.out.printf("[%-8s]: | Baseline: %-8s | JsonView: %-8s | Difference: %-6s |\n",
        repetitions, divide(baselineTimes, 1000000L) + "ms", divide(jsonViewTimes, 1000000L) + "ms", difference + "%");

    if(((double) jsonViewTimes / (double) baselineTimes) > 10) {
      fail("Performance delta is greater than 10x slower than basic Jackson (" + difference + "% slower)");
    }
  }

  public long randomSingleObjectPerformance(UncheckedRunnable mapper) throws Exception {
    long totalTime = 0;
    long chunkTime = 0;
    for(int i = 1; i <= repetitions; i++) {
      long time = System.nanoTime();
      mapper.run();
      time = System.nanoTime() - time;

      totalTime += time;
      chunkTime += time;
      if(i % 100000 == 0) {
        log.trace("Time per 100k entries: " + ((double) chunkTime / 1000000.0) + "ms");
        chunkTime = 0;
      }
    }

    return totalTime;
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

  String divide(long numerator, long denominator) {
    DecimalFormat df = new DecimalFormat("#.###");
    df.setRoundingMode(RoundingMode.CEILING);
    return df.format((double) numerator / (double) denominator);
  }

  interface UncheckedRunnable {
    public void run() throws Exception;
  }

}