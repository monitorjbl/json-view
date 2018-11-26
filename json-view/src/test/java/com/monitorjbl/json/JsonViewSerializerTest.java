package com.monitorjbl.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.monitorjbl.json.model.CustomType;
import com.monitorjbl.json.model.CustomTypeSerializer;
import com.monitorjbl.json.model.NonReplacableKeyMap;
import com.monitorjbl.json.model.TestAutodetect.AutodetectDefault;
import com.monitorjbl.json.model.TestAutodetect.AutodetectFields;
import com.monitorjbl.json.model.TestAutodetect.AutodetectGetters;
import com.monitorjbl.json.model.TestAutodetect.AutodetectNotPresent;
import com.monitorjbl.json.model.TestBackreferenceObject;
import com.monitorjbl.json.model.TestBackreferenceObject.TestForwardReferenceObject;
import com.monitorjbl.json.model.TestChildObject;
import com.monitorjbl.json.model.TestDuplicateKeys.ClassC;
import com.monitorjbl.json.model.TestInterface;
import com.monitorjbl.json.model.TestNonNulls;
import com.monitorjbl.json.model.TestNulls;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestObject.TestEnum;
import com.monitorjbl.json.model.TestSubobject;
import com.monitorjbl.json.model.TestSuperinterface;
import com.monitorjbl.json.model.TestSuperinterface.TestChildInterface;
import com.monitorjbl.json.model.TestSuperinterface.TestInterfaceObject;
import com.monitorjbl.json.model.TestUnrelatedObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.monitorjbl.json.Match.match;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class JsonViewSerializerTest {

  ObjectMapper sut;
  JsonViewSerializer serializer;

  @Before
  public void setup() {
    this.serializer = new JsonViewSerializer();
    sut = new ObjectMapper()
        .registerModule(new JsonViewModule(serializer))
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
  }

  @Test
  public void testJsonIgnore() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoredDirect("ignore me");
    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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

    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

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
    for(int i = 0; i < output.size(); i++) {
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
      for(int j = 0; j < array.size(); j++) {
        assertEquals(ref.getStringArray()[j], array.get(j));
      }

      assertNotNull(obj.get("list"));
      assertTrue(obj.get("list") instanceof List);
      List list = (List) obj.get("list");
      assertEquals(ref.getList().size(), list.size());
      for(int j = 0; j < list.size(); j++) {
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

    JsonViewSerializer.log = true;
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
    JsonViewSerializer.log = false;

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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals(ref.getInt1(), obj.get("int1"));
    assertTrue(obj.get("listOfObjects") instanceof List);
    List<Map<String, Object>> list = (List<Map<String, Object>>) obj.get("listOfObjects");
    assertEquals(2, list.size());
    assertEquals("test1", list.get(0).get("val"));
    assertEquals("test2", list.get(1).get("val"));
    assertNull(list.get(1).get("sub"));
  }

  @Test
  public void testListOfSubobjects_fieldSpecific() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setListOfObjects(asList(new TestSubobject("test1"), new TestSubobject("test2", new TestSubobject("test3"))));
    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("listOfObjects.val")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals(ref.getInt1(), obj.get("int1"));
    assertTrue(obj.get("listOfObjects") instanceof List);
    List<Map<String, Object>> list = (List<Map<String, Object>>) obj.get("listOfObjects");
    assertEquals(2, list.size());
    assertNull(list.get(0).get("val"));
    assertNull(list.get(1).get("val"));
    assertNotNull(list.get(1).get("sub"));
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
    assertNotNull(((Map) obj.get("sub")).get("otherVal"));
  }

  @Test
  public void testClassMatchingWithNoRootMatcher() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("str");
    ref.setInt1(3);
    TestSubobject sub = new TestSubobject();
    sub.setVal("val1");
    sub.setOtherVal("val2");
    ref.setSub(sub);

    String serialized = sut.writeValueAsString(JsonView.with(ref).onClass(TestSubobject.class, match()
        .exclude("*")
        .include("otherVal")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getDate().getTime(), obj.get("date"));
  }

  @Test
  public void testDate_withFormatter() throws Exception {
    SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
    sut.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    sut.setDateFormat(fmt);

    TestObject ref = new TestObject();
    ref.setDate(new Date());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(fmt.format(ref.getDate()), obj.get("date"));
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
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
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getUrl().toString(), obj.get("url"));
  }

  @Test
  public void testURIs() throws Exception {
    TestObject ref = new TestObject();
    ref.setUri(new URI("http://google.com"));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getUri().toString(), obj.get("uri"));
  }

  @Test
  public void testClass() throws Exception {
    TestObject ref = new TestObject();
    ref.setCls(TestSubobject.class);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertEquals(ref.getCls().getCanonicalName(), obj.get("cls"));
  }

  @Test
  public void testEnums() throws Exception {
    TestObject ref = new TestObject();
    ref.setTestEnum(TestEnum.VALUE_A);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertNotNull(obj.get("testEnum"));
    assertEquals(ref.getTestEnum().toString(), obj.get("testEnum"));
  }

  @Test
  public void testStaticFieldsAreIgnored() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("val1");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertNull(obj.get("PUBLIC_FIELD"));
    assertNull(obj.get("PRIVATE_FIELD"));
  }

  @Test
  public void testPathNotationInList() throws Exception {
    TestSubobject sub1 = new TestSubobject("a");
    TestSubobject sub2 = new TestSubobject("b");
    sub1.setSub(sub2);
    TestSubobject sub3 = new TestSubobject("c");
    sub2.setSub(sub3);
    TestObject ref = new TestObject();
    ref.setListOfObjects(Arrays.asList(sub1, sub2));

    String serialized = sut.writeValueAsString(
        JsonView.with(ref)
            .onClass(TestObject.class, match()
                .exclude("listOfObjects.sub.val")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("listOfObjects"));
    List<Map<String, Map<String, Map<String, Map>>>> list = (List<Map<String, Map<String, Map<String, Map>>>>) obj.get("listOfObjects");
    assertEquals(2, list.size());
    assertNull(list.get(0).get("sub").get("val"));
    assertNotNull(list.get(0).get("sub").get("sub").get("val"));
    assertNull(list.get(1).get("sub").get("val"));
  }

  @Test
  public void testWriteNullValues_enabledGlobally() throws Exception {
    TestObject ref = new TestObject();
    sut = sut.setSerializationInclusion(Include.ALWAYS);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertTrue(obj.containsKey("list"));
    assertNull(obj.get("list"));
  }

  @Test
  public void testWriteNullValues_disabledGlobally() throws Exception {
    TestObject ref = new TestObject();
    sut.setSerializationInclusion(Include.NON_NULL);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("str2"));
  }

  @Test
  public void testWriteNullValues_enabledForClass() throws Exception {
    TestNulls ref = new TestNulls();
    sut.setSerializationInclusion(Include.NON_NULL);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertTrue(obj.containsKey("val"));
    assertNull(obj.get("val"));
  }

  @Test
  public void testWriteNullValues_disabledForClass() throws Exception {
    TestNonNulls ref = new TestNonNulls();
    sut.setSerializationInclusion(Include.ALWAYS);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("val"));
  }

  @Test
  public void testImplicitInclude() throws Exception {
    TestObject ref = new TestObject();
    TestSubobject sub = new TestSubobject("test1");
    sub.setOtherVal("otherVal1");
    sub.setVal("asdf");
    ref.setSub(sub);

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class, match()
            .exclude("*")
            .include("sub.otherVal")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("sub"));
    Map m = (Map) obj.get("sub");
    assertEquals(sub.getOtherVal(), m.get("otherVal"));
    assertNull(m.get("val"));
  }

  @Test
  public void testIncludesForList() throws Exception {
    TestObject ref = new TestObject();
    TestSubobject testSubobject1 = new TestSubobject("test1");
    testSubobject1.setOtherVal("otherVal1");
    testSubobject1.setVal("asdf");
    TestSubobject testSubobject2 = new TestSubobject("test2");
    testSubobject2.setOtherVal("otherVal2");
    testSubobject2.setVal("asdf");
    ref.setListOfObjects(Arrays.asList(testSubobject1, testSubobject2));

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class,
            match().exclude("*")
                .include("listOfObjects.otherVal")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("listOfObjects"));
    List<Map<String, String>> list = (List<Map<String, String>>) obj.get("listOfObjects");
    assertEquals(2, list.size());
    assertNull(list.get(0).get("val"));
    assertNull(list.get(1).get("val"));
    assertNotNull(list.get(0).get("otherVal"));
    assertNotNull(list.get(1).get("otherVal"));
  }

  @Test
  public void testMatchingOnInterfaces() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("asdf");
    ref.setDate(new Date());

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestInterface.class,
            match().exclude("date")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("str1"));
    assertEquals(ref.getStr1(), obj.get("str1"));
    assertNull(obj.get("date"));
  }

  @Test
  public void testIgnorePropertiesOnField() throws Exception {
    TestObject ref = new TestObject();
    TestSubobject testSubobject1 = new TestSubobject("test1");
    testSubobject1.setOtherVal("otherVal1");
    testSubobject1.setVal("asdf");
    ref.setSub(testSubobject1);
    ref.setSubWithIgnores(testSubobject1);

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class,
            match().exclude("sub")));
    Map<String, Map<String, Object>> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNull(obj.get("sub"));
    assertNotNull(obj.get("subWithIgnores"));
    assertNotNull(obj.get("subWithIgnores").get("otherVal"));
    assertNull(obj.get("subWithIgnores").get("val"));
  }

  @Test
  public void testBackReferenceSupport() throws Exception {
    TestForwardReferenceObject forward = new TestForwardReferenceObject();
    TestBackreferenceObject back = new TestBackreferenceObject();

    forward.setId("forward");
    forward.setParent(back);
    back.setId("back");
    back.setChildren(asList(forward));

    String serialized = sut.writeValueAsString(JsonView.with(forward));
    Map<String, Map<String, Object>> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("parent"));
    assertEquals("back", obj.get("parent").get("id"));
  }

  @Test
  public void testBigDecimalSerialization() throws Exception {
    TestObject ref = new TestObject();
    ref.setBigDecimal(new BigDecimal(Math.PI));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("bigDecimal"));
    assertEquals(3.141592653589793, obj.get("bigDecimal"));
  }

  @Test
  public void testCustomSerializationByDefault() throws Exception {
    TestObject ref = new TestObject();
    CustomType custom = new CustomType(5l, "hello");
    ref.setCustom(custom);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("custom"));
    assertTrue(obj.get("custom") instanceof Map);
    assertTrue(((Map) obj.get("custom")).get("name").equals("hello"));
    assertTrue(((Map) obj.get("custom")).get("sid").equals(5));
  }

  @Test
  public void testCustomSerializationRegistered() throws Exception {
    TestObject ref = new TestObject();
    CustomType custom = new CustomType(5l, "hello");
    ref.setCustom(custom);

    sut = new ObjectMapper().registerModule(new JsonViewModule(serializer).registerSerializer(CustomType.class, new CustomTypeSerializer()));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("custom"));
    assertTrue(obj.get("custom") instanceof String);
    assertTrue((obj.get("custom")).equals("5[hello]"));
  }

  @Test
  public void testWriteJSR310_zonedDateTime() throws Exception {
    TestObject ref = new TestObject();
    ref.setZonedDateTime(ZonedDateTime.now());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("zonedDateTime"));
    assertTrue(obj.get("zonedDateTime") instanceof Number);
  }

  @Test
  public void testWriteJSR310_formattedZonedDateTime() throws Exception {
    sut.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    TestObject ref = new TestObject();
    ref.setFormattedZonedDateTime(ZonedDateTime.now());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("formattedZonedDateTime"));
    assertTrue(obj.get("formattedZonedDateTime") instanceof String);
  }

  @Test
  public void testWriteJsonProperty() throws Exception {
    TestObject ref = new TestObject();
    ref.setJsonProp("jibjab");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("totallyJsonProp"));
    assertEquals(ref.getJsonProp(), obj.get("totallyJsonProp"));
  }

  @Test
  public void testWriteJsonProperty_noValue() throws Exception {
    TestObject ref = new TestObject();
    ref.setJsonPropNoValue("jibjab");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("jsonPropNoValue"));
    assertEquals(ref.getJsonPropNoValue(), obj.get("jsonPropNoValue"));
  }

  @Test
  public void testDeepNestedObjects() throws Exception {
    TestSubobject subobject = new TestSubobject();
    subobject.setVal("someval");
    subobject.setOtherVal("otherval");
    TestObject ref = new TestObject();
    ref.setStr1("somestr");
    ref.setSub(subobject);

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class, match()
            .include("sub.*")
            .exclude("sub.otherVal")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("sub"));
    assertTrue(obj.get("sub") instanceof Map);

    Map<String, Object> subMap = (Map<String, Object>) obj.get("sub");
    assertNotNull(subMap.get("val"));
    assertEquals(subobject.getVal(), subMap.get("val"));
    assertNull(subMap.get("otherVal"));
  }

  @Test
  public void testUUIDs() throws Exception {
    TestObject ref = new TestObject();
    ref.setUuid(UUID.randomUUID());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("uuid"));
    assertEquals(ref.getUuid().toString(), obj.get("uuid"));
  }

  @Test
  public void testJsonSerializeAnnotation() throws Exception {
    TestObject ref = new TestObject();
    CustomType custom = new CustomType(5l, "hello");
    ref.setCustomFieldSerializer(custom);

    sut = new ObjectMapper().registerModule(new JsonViewModule(serializer));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("customFieldSerializer"));
    assertTrue(obj.get("customFieldSerializer") instanceof String);
    assertEquals(obj.get("customFieldSerializer"), "5[hello]");
  }

  @Test
  public void testPathFirstMatch_defaultBehavior() throws Exception {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr1("sdfdsf");
    ref.setStr2("erer");
    ref.setRecursion(ref);

    Consumer<Map<String, Object>> doTest = obj -> {
      assertEquals(ref.getInt1(), obj.get("int1"));
      assertEquals(ref.getStr1(), obj.get("str1"));
      assertEquals(ref.getStr2(), obj.get("str2"));
      assertNotNull(obj.get("recursion"));

      Map<String, Object> rec1 = (Map<String, Object>) obj.get("recursion");
      assertNotNull(rec1.get("recursion"));
      assertEquals(ref.getStr1(), rec1.get("str1"));
      assertNull(rec1.get("str2"));
      assertNull(rec1.get("int1"));

      Map<String, Object> rec2 = (Map<String, Object>) rec1.get("recursion");
      assertEquals(ref.getStr2(), rec2.get("str2"));
      assertNull(rec2.get("str1"));
      assertNull(rec2.get("int1"));

      assertNull(rec2.get("recursion"));
    };


    // Perform test with behavior set at default level
    sut = new ObjectMapper().registerModule(new JsonViewModule(serializer)
        .withDefaultMatcherBehavior(MatcherBehavior.PATH_FIRST));
    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class, match()
            .include("recursion", "recursion.str1", "recursion.recursion.str2", "str1", "str2", "int1")
            .exclude("*")));
    doTest.accept(sut.readValue(serialized, NonReplacableKeyMap.class));

    // Perform test with behavior set at the JsonView level
    sut = new ObjectMapper().registerModule(new JsonViewModule(serializer));
    serialized = sut.writeValueAsString(JsonView.with(ref)
        .withMatcherBehavior(MatcherBehavior.PATH_FIRST)
        .onClass(TestObject.class, match()
            .include("recursion", "recursion.str1", "recursion.recursion.str2", "str1", "str2", "int1")
            .exclude("*")));
    doTest.accept(sut.readValue(serialized, NonReplacableKeyMap.class));
  }

  @Test
  public void testFieldTransform() throws Exception {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr1("sdfdsf");

    sut = new ObjectMapper().registerModule(new JsonViewModule(serializer));

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestObject.class, match()
            .exclude("*")
            .include("str1", "str2")
            .transform("str1", (TestObject t, String f) -> f.toUpperCase())
            .transform("str2", (TestObject t, String f) -> t.getStr1())));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNull(obj.get("int1"));
    assertNotNull(obj.get("str1"));
    assertEquals(ref.getStr1().toUpperCase(), obj.get("str1"));
    assertNotNull(obj.get("str2"));
    assertEquals(ref.getStr1(), obj.get("str2"));
  }

  @Test
  public void testMethodGetters() throws Exception {
    TestObject ref = new TestObject();

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("TEST", obj.get("staticValue"));
  }

  @Test
  public void testMethodGetters_ignored() throws Exception {
    TestObject ref = new TestObject();

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("ignoredValue"));
  }

  @Test
  public void testMethodGetters_combinesAnnotations() throws Exception {
    TestObject ref = new TestObject();

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("ignoredDirect"));
  }

  @Test
  public void testAutodetect_notPresent() throws Exception {
    AutodetectNotPresent ref = new AutodetectNotPresent();
    ref.setId("test");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("valid", obj.get("id"));
  }

  @Test
  public void testAutodetect_default() throws Exception {
    AutodetectDefault ref = new AutodetectDefault();
    ref.setId("test");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("valid", obj.get("id"));
  }

  @Test
  public void testAutodetect_fieldsOnly() throws Exception {
    AutodetectFields ref = new AutodetectFields();
    ref.setId("test");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("test", obj.get("id"));
  }

  @Test
  public void testAutodetect_methodsOnly() throws Exception {
    AutodetectGetters ref = new AutodetectGetters();
    ref.setId("test");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("valid", obj.get("id"));
  }

  @Test
  public void testSuperinterfaces() throws Exception {
    TestInterfaceObject ref = new TestInterfaceObject();
    ref.setDescription("description");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals("ID", obj.get("id"));
    assertEquals("NAME", obj.get("name"));
    assertEquals("description", obj.get("description"));
  }

  @Test
  public void testSuperinterfaces_matchers() throws Exception {
    TestInterfaceObject ref = new TestInterfaceObject();
    ref.setDescription("description");

    String serialized = sut.writeValueAsString(JsonView.with(ref)
        .onClass(TestSuperinterface.class, match()
            .exclude("id"))
        .onClass(TestChildInterface.class, match()
            .exclude("name")));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("id"));
    assertFalse(obj.containsKey("name"));
    assertEquals("description", obj.get("description"));
  }

  @Test
  public void testSerializationOptions_includeNonNullGlobally() throws Exception {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    sut = sut.setSerializationInclusion(Include.NON_NULL);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertFalse(obj.containsKey("str1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
  }

  @Test
  public void testSerializationOptions_includeNonNullLocally() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr1("test");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals(ref.getStr1(), obj.get("str1"));
    assertFalse(obj.containsKey("str2"));
  }

  @Test
  public void testSerializationOrder() throws Exception {
    TestObject ref = new TestObject();
    ref.setStr2("sdfsdf");

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    List<String> keys = new ArrayList<>(obj.keySet());
    assertEquals("sub", keys.get(0));
    assertEquals("subWithIgnores", keys.get(1));
    assertEquals("str1", keys.get(2));
    assertEquals("str2", keys.get(3));
    assertEquals("date", keys.get(4));
  }

  @Test
  public void testJacksonJsonNodeSupport_object() throws Exception {
    TestObject ref = new TestObject();
    ObjectNode node1 = sut.createObjectNode();
    ObjectNode node2 = sut.createObjectNode();
    node2.set("stringfield", new TextNode("hello"));
    node1.set("jacksonObject", node2);
    ref.setJsonNode(node1);

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNotNull(obj.get("jsonNode"));
    assertTrue(obj.get("jsonNode") instanceof Map);

    Map<String, Object> jsonNode = (Map<String, Object>) obj.get("jsonNode");
    assertTrue(jsonNode.get("jacksonObject") instanceof Map);
    assertEquals("hello", ((Map) jsonNode.get("jacksonObject")).get("stringfield"));
  }

  @Test
  public void testJacksonJsonNodeSupport_textNode() throws Exception {
    TestObject ref = new TestObject();
    ref.setJsonNode(new TextNode("asdf"));

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals(obj.get("jsonNode"), "asdf");
  }

  @Test
  public void testJacksonJsonNodeSupport_nullNode() throws Exception {
    TestObject ref = new TestObject();
    ref.setJsonNode(NullNode.getInstance());

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertNull(obj.get("jsonNode"));
  }

  /**
   * Verify that multiple accessible properties are not serialized more than once (issue #59)
   */
  @Test
  public void testDuplicateKeysOnInheritance() throws Exception {
    ClassC ref = new ClassC();

    String serialized = sut.writeValueAsString(JsonView.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);

    assertEquals(ref.getId(), obj.get("id"));
  }
  
  @Test
  public void testFieldWithGetInName() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setWidgetName("random name");
    String serialized = sut.writeValueAsString(
        JsonView.with(ref).onClass(TestObject.class, match()));

    Map<String, Object> obj = sut.readValue(serialized, NonReplacableKeyMap.class);
    assertNull(obj.get("widName"));
    assertNotNull(obj.get("widgetName"));
  }

}
