package com.monitorjbl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.monitorjbl.json.JsonViewSerializer.JsonWriter;
import com.monitorjbl.json.model.TestObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SerializerProvider.class)
public class WriterTest {
  @Mock
  JsonGenerator jgen;
  @Mock
  JsonView result;
  SerializerProvider serializerProvider;

  // has to be mocked with powermock due to final methods
  JsonWriter sut;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    serializerProvider = PowerMockito.mock(SerializerProvider.class);
    sut = new JsonViewSerializer().new JsonWriter(serializerProvider, jgen, result);
  }

  @Test
  public void testContainsMatchingPattern_basic() {
    Set<String> patterns = newHashSet("field1", "field2");
    assertEquals(1, sut.containsMatchingPattern(patterns, "field1", true));
    assertEquals(1, sut.containsMatchingPattern(patterns, "field2", true));
    assertEquals(-1, sut.containsMatchingPattern(patterns, "field3", true));
  }

  @Test
  public void testContainsMatchingPattern_wildcard() {
    Set<String> patterns = newHashSet("field*");
    assertEquals(0, sut.containsMatchingPattern(patterns, "field1", true));
    assertEquals(0, sut.containsMatchingPattern(patterns, "field2", true));
    assertEquals(-1, sut.containsMatchingPattern(patterns, "val1", true));
  }

  @Test
  public void testContainsMatchingPattern_wildcardAll() {
    Set<String> patterns = newHashSet("*");
    assertEquals(0, sut.containsMatchingPattern(patterns, "field1", true));
    assertEquals(0, sut.containsMatchingPattern(patterns, "field2", true));
    assertEquals(0, sut.containsMatchingPattern(patterns, "val1", true));
  }

  @Test
  public void testContainsMatchingPattern_wildcardInChildPath() {
    Set<String> patterns = newHashSet("*.green");
    assertEquals(0, sut.containsMatchingPattern(patterns, "field1.green", true));
    assertEquals(-1, sut.containsMatchingPattern(patterns, "field2.blue", true));
  }

  @Test
  public void testContainsMatchingPattern_wildcardInComplexPath() {
    Set<String> patterns = newHashSet("*.green.*");
    assertEquals(-1, sut.containsMatchingPattern(patterns, "field1.green", true));
    assertEquals(-1, sut.containsMatchingPattern(patterns, "field2.blue", true));
    assertEquals(0, sut.containsMatchingPattern(patterns, "field1.green.id", true));
    assertEquals(-1, sut.containsMatchingPattern(patterns, "field1.blue.id", true));
    assertEquals(0, sut.containsMatchingPattern(patterns, "field2.green.name", true));
  }

//  @Test
//  public void testAnnotatedWithIgnore() throws Exception {
//    assertTrue(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("ignoredDirect")));
//    assertTrue(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("ignoredIndirect")));
//    assertFalse(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("str1")));
//  }

  @Test
  public void testWritePrimitive_object() throws Exception {
    assertFalse(sut.writePrimitive(new TestObject()));
  }

  @Test
  public void testWritePrimitive_string() throws Exception {
    sut.writePrimitive("test");
    verify(jgen, times(1)).writeString("test");
  }

  @Test
  public void testWritePrimitive_int() throws Exception {
    int primitive = 1;
    Integer obj = 2;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber(1);
    verify(jgen, times(1)).writeNumber(2);
  }

  @Test
  public void testWritePrimitive_long() throws Exception {
    long primitive = 1L;
    Long obj = 2L;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber(1L);
    verify(jgen, times(1)).writeNumber(2L);
  }

  @Test
  public void testWritePrimitive_short() throws Exception {
    short primitive = 1;
    Short obj = 2;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber((short) 1);
    verify(jgen, times(1)).writeNumber((short) 2);
  }

  @Test
  public void testWritePrimitive_char() throws Exception {
    char primitive = 'a';
    Character obj = 'b';
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber('a');
    verify(jgen, times(1)).writeNumber('b');
  }

  @Test
  public void testWritePrimitive_double() throws Exception {
    double primitive = 1.0;
    Double obj = 2.0;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber(1.0);
    verify(jgen, times(1)).writeNumber(2.0);
  }

  @Test
  public void testWritePrimitive_float() throws Exception {
    float primitive = 1.0f;
    Float obj = 2.0f;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber(1.0f);
    verify(jgen, times(1)).writeNumber(2.0f);
  }

  @Test
  public void testWritePrimitive_byte() throws Exception {
    byte primitive = 1;
    Byte obj = 2;
    assertTrue(sut.writePrimitive(primitive));
    assertTrue(sut.writePrimitive(obj));
    verify(jgen, times(1)).writeNumber((byte) 1);
    verify(jgen, times(1)).writeNumber((byte) 2);
  }

  @Test
  public void testWritePrimitive_boolean() throws Exception {
    boolean primitive = true;
    Boolean obj = false;
    sut.writePrimitive(primitive);
    sut.writePrimitive(obj);
    verify(jgen, times(1)).writeBoolean(true);
    verify(jgen, times(1)).writeBoolean(false);
  }

  @Test
  public void testWriteSpecial_date() throws Exception {
    Date dt = new Date();
    sut.writeSpecial(dt);
    verify(serializerProvider, times(1)).defaultSerializeDateValue(dt, jgen);
  }

  @Test
  public void testWriteSpecial_url() throws Exception {
    URL url = new URL("http://google.com");
    sut.writeSpecial(url);
    verify(jgen, times(1)).writeString(url.toString());
  }

  @Test
  public void testWriteSpecial_uri() throws Exception {
    URI uri = new URI("http://google.com");
    sut.writeSpecial(uri);
    verify(jgen, times(1)).writeString(uri.toString());
  }

  @Test
  public void testWriteSpecial_class() throws Exception {
    Class cls = TestObject.class;
    sut.writeSpecial(cls);
    verify(jgen, times(1)).writeString(cls.getCanonicalName());
  }


  @Test
  public void testWriteList_stringList() throws Exception {
    assertTrue(sut.writeList(newArrayList("val1", "val2")));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
  }

  @Test
  public void testWriteList_objectList() throws Exception {
    assertTrue(sut.writeList(newArrayList(new TestObject(), new TestObject())));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
  }

  @Test
  public void testWriteList_mixedList() throws Exception {
    assertTrue(sut.writeList(newArrayList(new TestObject(), "val2")));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
  }

  @Test
  public void testWriteList_stringArray() throws Exception {
    assertTrue(sut.writeList(new String[]{"val1", "val2"}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
  }

  @Test
  public void testWriteList_objectArray() throws Exception {
    assertTrue(sut.writeList(new TestObject[]{new TestObject(), new TestObject()}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
  }

  @Test
  public void testWriteList_intArray() throws Exception {
    assertTrue(sut.writeList(new int[]{1, 2}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeNumber(1);
    verify(jgen, times(1)).writeNumber(2);
  }

  @Test
  public void testWriteList_longArray() throws Exception {
    assertTrue(sut.writeList(new long[]{1L, 2L}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeNumber(1L);
    verify(jgen, times(1)).writeNumber(2L);
  }

  @Test
  public void testWriteList_shortArray() throws Exception {
    assertTrue(sut.writeList(new short[]{1, 2}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeNumber((short) 1);
    verify(jgen, times(1)).writeNumber((short) 2);
  }

  @Test
  public void testWriteList_doubleArray() throws Exception {
    assertTrue(sut.writeList(new double[]{1.0, 2.0}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeNumber(1.0);
    verify(jgen, times(1)).writeNumber(2.0);
  }

  @Test
  public void testWriteList_floatArray() throws Exception {
    assertTrue(sut.writeList(new float[]{1f, 2f}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeNumber(1f);
    verify(jgen, times(1)).writeNumber(2f);
  }

  @Test
  public void testWriteList_booleanArray() throws Exception {
    assertTrue(sut.writeList(new boolean[]{true, false}));
    verify(jgen, times(1)).writeStartArray();
    verify(jgen, times(1)).writeEndArray();
    verify(jgen, times(1)).writeBoolean(true);
    verify(jgen, times(1)).writeBoolean(false);
  }

  @Test
  public void testWriteList_byteArray() throws Exception {
    String val = "asdf";
    assertTrue(sut.writeList(val.getBytes()));
    verify(jgen, times(1)).writeBinary(val.getBytes());
  }

}
