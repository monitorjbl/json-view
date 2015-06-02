package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.json.server.JettyServer;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LiveTest {

  private static JettyServer server = new JettyServer();

  @BeforeClass
  public static void start() throws InterruptedException {
    server = new JettyServer();
    server.start(8080);
    boolean ready = false;
    while (!ready) {
      try {
        new URL("http://localhost:8080/ready").openStream();
        ready = true;
      } catch (Exception e) {
        Thread.sleep(500);
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSimple() throws IOException {
    Map<String, Object> map = new ObjectMapper().readValue(Request.Get("http://localhost:8080/bean").execute().returnContent().asStream(), HashMap.class);
    assertEquals("ignored", map.get("ignoredDirect"));
    assertNull(map.get("int1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSimpleWithReturnValue() throws IOException {
    Map<String, Object> map = new ObjectMapper().readValue(Request.Get("http://localhost:8080/bean/withReturnValue").execute().returnContent().asStream(),
        HashMap.class);
    assertEquals("ignored", map.get("ignoredDirect"));
    assertNull(map.get("int1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testList() throws IOException {
    List<Map<String, Object>> list = new ObjectMapper().readValue(Request.Get("http://localhost:8080/list").execute().returnContent().asStream(), ArrayList.class);

    assertEquals(2, list.size());
    for (Map<String, Object> map : list) {
      assertEquals("ignored", map.get("ignoredDirect"));
      assertNull(map.get("int1"));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testMultithreading() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    final AtomicInteger counter = new AtomicInteger(0);

    for (int i = 0; i < 100; i++) {
      executorService.execute(new Runnable() {
        public void run() {
          try {
            List<Map<String, Object>> list = new ObjectMapper().readValue(Request.Get("http://localhost:8080/list")
                .execute().returnContent().asStream(), ArrayList.class);
            assertEquals(2, list.size());
            counter.addAndGet(1);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    executorService.shutdown();
    executorService.awaitTermination(60L, TimeUnit.SECONDS);
    assertEquals(100, counter.get());
  }

  @Test
  public void testNoninterference() throws Exception {
    Date dt = new Date();
    String ret = Request.Post("http://localhost:8080/bean").bodyString(
        "{\"date\":\"1433214360187\",\"str1\":\"test\",\"notReal\":\"asdfas\"}", ContentType.APPLICATION_JSON)
        .execute().returnContent().asString();
    System.out.println(ret);
    assertEquals(5, ret.split("\n").length);
  }

  @AfterClass
  public static void stop() {
    server.stop();
  }
}
