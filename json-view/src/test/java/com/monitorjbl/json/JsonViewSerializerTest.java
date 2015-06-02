package com.monitorjbl.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.monitorjbl.json.model.TestChildObject;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestObject.TestEnum;
import com.monitorjbl.json.model.TestSubobject;
import com.monitorjbl.json.model.TestUnrelatedObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.monitorjbl.json.Match.match;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class JsonViewSerializerTest {

  ObjectMapper sut;

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, new JsonViewSerializer());
    sut.registerModule(module);
  }

  @Test
  public void testJsonIgnore() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoredDirect("ignore me");
    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoredDirect"));
  }

  @Test
  public void testJsonIgnoreProperties() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoredIndirect("ignore me");
    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoredIndirect"));
  }

  @Test
  public void testBasicSerialization() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr2("asdf");
    ref.setStringArray(new String[]{"apple", "banana"});
    ref.setList(asList("red", "blue", "green"));
    ref.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref).onClass(TestObject.class, match()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));

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
    ref.setIgnoredIndirect("ignore me too");
    ref.setStringArray(new String[]{"pizza", "french fry"});

    String serialized = sut.writeValueAsString(
        JsonView.with(ref).onClass(TestObject.class, match()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("ignoredIndirect"));
    assertNotNull(obj.get("ignoredDirect"));
    assertEquals(ref.getIgnoredDirect(), obj.get("ignoredDirect"));
    assertNotNull(obj.get("childField"));
    assertEquals(ref.getChildField(), obj.get("childField"));
  }

  @Test
  public void testFieldSpecificity() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr2("asdf");
    ref.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("str2")
                .exclude("sub.val"))
            .onClass(TestSubobject.class, match()
                .exclude("sub")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);

    assertNotNull(obj.get("sub"));
    assertEquals(ref.getSub().getVal(), ((Map) obj.get("sub")).get("val"));
  }

  @Test
  public void testListWithSingleClass() throws IOException {
    TestObject ref1 = new TestObject();
    ref1.setInt1(1);
    ref1.setStr2("asdf");
    ref1.setStringArray(new String[]{"apple", "banana"});
    ref1.setList(asList("red", "blue", "green"));
    ref1.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));

    TestObject ref2 = new TestObject();
    ref2.setInt1(2);
    ref2.setStr2("asdf");
    ref2.setStringArray(new String[]{"orange", "kiwi"});
    ref2.setList(asList("cyan", "indigo", "violet"));
    ref2.setSub(new TestSubobject("zxcvxzcv", new TestSubobject("hjhljkljh")));

    List<TestObject> refList = asList(ref1, ref2);

    String serialized = sut.writeValueAsString(
        JsonView.with(refList).onClass(TestObject.class, match()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));
    List<Map<String, Object>> output = sut.readValue(serialized, ArrayList.class);

    assertEquals(refList.size(), output.size());
    for (int i = 0; i < output.size(); i++) {
      Map<String, Object> obj = output.get(i);
      TestObject ref = refList.get(i);

      assertEquals(ref.getInt1(), obj.get("int1"));
      assertNull(obj.get("str2"));
      assertNotNull(obj.get("sub"));
      assertNull(((Map) obj.get("sub")).get("val"));

      assertNotNull(obj.get("stringArray"));
      assertTrue(obj.get("stringArray") instanceof List);
      List array = (List) obj.get("stringArray");
      assertEquals(ref.getStringArray().length, array.size());
      for (int j = 0; j < array.size(); j++) {
        assertEquals(ref.getStringArray()[j], array.get(j));
      }

      assertNotNull(obj.get("list"));
      assertTrue(obj.get("list") instanceof List);
      List list = (List) obj.get("list");
      assertEquals(ref.getList().size(), list.size());
      for (int j = 0; j < list.size(); j++) {
        assertEquals(ref.getList().get(j), list.get(j));
      }

    }
  }

  @Test
  public void testListWithMultipleClasses() throws IOException {
    TestObject ref1 = new TestObject();
    ref1.setInt1(1);
    ref1.setStr2("asdf");
    ref1.setStringArray(new String[]{"apple", "banana"});
    ref1.setList(asList("red", "blue", "green"));
    ref1.setIgnoredIndirect("ignore me too");
    ref1.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));

    TestChildObject ref2 = new TestChildObject();
    ref2.setChildField("green");
    ref2.setIgnoredDirect("ignore me");
    ref2.setStringArray(new String[]{"pizza", "french fry"});

    TestUnrelatedObject ref3 = new TestUnrelatedObject();
    ref3.setId(3L);
    ref3.setName("xxzcvxc");

    String serialized = sut.writeValueAsString(
        JsonView.with(asList(ref1, ref2, ref3))
            .onClass(TestObject.class, match()
                .exclude("str2")
                .include("ignoredIndirect"))
            .onClass(TestChildObject.class, match()
                .exclude("array")
                .include("ignoredDirect"))
            .onClass(TestUnrelatedObject.class, match()
                .exclude("name")));
    List<Map<String, Object>> output = sut.readValue(serialized, ArrayList.class);

    assertEquals(3, output.size());

    Map<String, Object> t1 = output.get(0);
    assertEquals(ref1.getInt1(), t1.get("int1"));
    assertNull(t1.get("srt2"));
    assertEquals(ref1.getIgnoredIndirect(), t1.get("ignoredIndirect"));

    Map<String, Object> t2 = output.get(1);
    assertEquals(ref2.getChildField(), t2.get("childField"));
    assertNull(t2.get("array"));
    assertEquals(ref2.getIgnoredDirect(), t2.get("ignoredDirect"));

    Map<String, Object> t3 = output.get(2);
    assertEquals(ref3.getId().longValue(), ((Integer) t3.get("id")).longValue());
    assertNull(t3.get("name"));
  }

  @Test
  public void testListOfSubobjects() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setListOfObjects(asList(new TestSubobject("test1"), new TestSubobject("test2", new TestSubobject("test3"))));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("sub.val"))
            .onClass(TestSubobject.class, match()
                .exclude("sub")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);

    assertEquals(ref.getInt1(), obj.get("int1"));
    assertTrue(obj.get("listOfObjects") instanceof List);
    List<Map<String, Object>> list = (List<Map<String, Object>>) obj.get("listOfObjects");
    assertEquals(2, list.size());
    assertEquals("test1", list.get(0).get("val"));
    assertEquals("test2", list.get(1).get("val"));
    assertNull(list.get(1).get("sub"));
  }

  @Test
  public void testMapOfSubobjects() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setMapOfObjects(ImmutableMap.of(
        "key1", new TestSubobject("test1"),
        "key2", new TestSubobject("test2", new TestSubobject("test3"))
    ));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("sub.val"))
            .onClass(TestSubobject.class, match()
                .exclude("sub")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);

    assertEquals(ref.getInt1(), obj.get("int1"));
    assertTrue(obj.get("mapOfObjects") instanceof Map);
    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) obj.get("mapOfObjects");
    assertEquals(2, map.size());
    assertEquals("test1", map.get("key1").get("val"));
    assertEquals("test2", map.get("key2").get("val"));
    assertNull(map.get("key2").get("sub"));
  }

  @Test
  public void testMapWithIntKeys() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setMapWithIntKeys(ImmutableMap.of(
        1, "red",
        2, "green"
    ));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("sub.val"))
            .onClass(TestSubobject.class, match()
                .exclude("sub")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);

    assertEquals(ref.getInt1(), obj.get("int1"));
    assertTrue(obj.get("mapWithIntKeys") instanceof Map);
    Map map = (Map) obj.get("mapWithIntKeys");
    assertEquals(2, map.size());
    assertNull(map.get(1));
    assertEquals(ref.getMapWithIntKeys().get(1), map.get("1"));
    assertNull(map.get(2));
    assertEquals(ref.getMapWithIntKeys().get(2), map.get("2"));
  }

  @Test
  public void testClassMatching() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("str");
    TestSubobject sub = new TestSubobject();
    sub.setVal("val1");
    ref.setSub(sub);

    String serialized = sut.writeValueAsString(JsonView.with(ref).onClass(TestSubobject.class, match().exclude("val")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
  }

  @Test
  public void testClassMatchingMixedWithPathMatching() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("str");
    ref.setInt1(3);
    TestSubobject sub = new TestSubobject();
    sub.setVal("val1");
    sub.setOtherVal("val2");
    ref.setSub(sub);

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class, match()
            .exclude("int1", "sub.otherVal"))
        .onClass(TestSubobject.class, match()
            .exclude("val")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
    assertNotNull(((Map) obj.get("sub")).get("otherVal"));
  }

  @Test
  public void testBlanketExclude() throws Exception {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr1("str1");
    ref.setStr2("str2");
    ref.setMapOfObjects(ImmutableMap.of(
        "key1", new TestSubobject("test1"),
        "key2", new TestSubobject("test2", new TestSubobject("test3"))
    ));

    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("*")
                .include("str2")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getStr2(), obj.get("str2"));
    assertNull(obj.get("str1"));
    assertNull(obj.get("mapWithIntKeys"));
    assertNull(obj.get("int1"));
  }

  @Test
  public void testBlanketInclude() throws Exception {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr1("str1");
    ref.setStr2("str2");
    ref.setIgnoredDirect("ignoredDirect");
    ref.setIgnoredIndirect("ignoredIndirect");
    ref.setMapOfObjects(ImmutableMap.of(
        "key1", new TestSubobject("test1"),
        "key2", new TestSubobject("test2", new TestSubobject("test3"))
    ));

    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .include("*")
                .exclude("str2")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertNull(obj.get("str2"));
    assertEquals(ref.getIgnoredIndirect(), obj.get("ignoredIndirect"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNotNull(obj.get("mapOfObjects"));
  }

  @Test
  public void testDate() throws Exception {
    TestObject ref = new TestObject();
    ref.setDate(new Date());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getDate().getTime(), obj.get("date"));
  }

  @Test
  public void testPrimitiveArrays() throws Exception {
    TestObject ref = new TestObject();
    ref.setIntArray(new int[]{1, 2, 3});
    ref.setByteArray("asdf".getBytes());
    ref.setStringArray(new String[]{"val1", "val2"});

    TestObject t1 = new TestObject();
    t1.setInt1(1);
    TestObject t2 = new TestObject();
    t2.setInt1(2);
    ref.setObjArray(new TestObject[]{t1, t2});

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(Ints.asList(ref.getIntArray()), obj.get("intArray"));
    assertEquals(Arrays.asList(ref.getStringArray()), obj.get("stringArray"));

    assertEquals("asdf", new String(BaseEncoding.base64().decode((String) obj.get("byteArray"))));

    List<Map<String, Object>> objList = (List<Map<String, Object>>) obj.get("objArray");
    assertEquals(2, objList.size());
    assertEquals(t1.getInt1(), objList.get(0).get("int1"));
    assertEquals(t2.getInt1(), objList.get(1).get("int1"));
  }

  @Test
  public void testURLs() throws Exception {
    TestObject ref = new TestObject();
    ref.setUrl(new URL("http://google.com"));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getUrl().toString(), obj.get("url"));
  }

  @Test
  public void testURIs() throws Exception {
    TestObject ref = new TestObject();
    ref.setUri(new URI("http://google.com"));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertEquals(ref.getUri().toString(), obj.get("uri"));
  }

  @Test
  public void testEnums() throws Exception {
    TestObject ref = new TestObject();
    ref.setTestEnum(TestEnum.VALUE_A);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("testEnum"));
    assertEquals(ref.getTestEnum().toString(), obj.get("testEnum"));
  }
}
