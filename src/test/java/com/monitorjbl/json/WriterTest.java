package com.monitorjbl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.monitorjbl.json.JsonViewSerializer.JsonWriter;
import com.monitorjbl.json.model.TestObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WriterTest {
  @Mock
  JsonGenerator jgen;
  @Mock
  JsonView result;
  JsonWriter sut;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    sut = new JsonWriter(jgen, result, 10000);
  }

  @Test
  public void testContainsMatchingPattern_basic() {
    List<String> patterns = newArrayList("field1", "field2");
    assertTrue(sut.containsMatchingPattern(patterns, "field1"));
    assertTrue(sut.containsMatchingPattern(patterns, "field2"));
    assertFalse(sut.containsMatchingPattern(patterns, "field3"));
  }

  @Test
  public void testContainsMatchingPattern_wildcard() {
    List<String> patterns = newArrayList("field*");
    assertTrue(sut.containsMatchingPattern(patterns, "field1"));
    assertTrue(sut.containsMatchingPattern(patterns, "field2"));
    assertFalse(sut.containsMatchingPattern(patterns, "val1"));
  }

  @Test
  public void testContainsMatchingPattern_wildcardAll() {
    List<String> patterns = newArrayList("*");
    assertTrue(sut.containsMatchingPattern(patterns, "field1"));
    assertTrue(sut.containsMatchingPattern(patterns, "field2"));
    assertTrue(sut.containsMatchingPattern(patterns, "val1"));
  }

  @Test
  public void testContainsMatchingPattern_wildcardInChildPath() {
    List<String> patterns = newArrayList("*.green");
    assertTrue(sut.containsMatchingPattern(patterns, "field1.green"));
    assertFalse(sut.containsMatchingPattern(patterns, "field2.blue"));
  }

  @Test
  public void testContainsMatchingPattern_wildcardInComplexPath() {
    List<String> patterns = newArrayList("*.green.*");
    assertFalse(sut.containsMatchingPattern(patterns, "field1.green"));
    assertFalse(sut.containsMatchingPattern(patterns, "field2.blue"));
    assertTrue(sut.containsMatchingPattern(patterns, "field1.green.id"));
    assertFalse(sut.containsMatchingPattern(patterns, "field1.blue.id"));
    assertTrue(sut.containsMatchingPattern(patterns, "field2.green.name"));
  }

  @Test
  public void testAnnotatedWithIgnore() throws Exception {
    assertTrue(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("ignoredDirect")));
    assertTrue(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("ignoredIndirect")));
    assertFalse(sut.annotatedWithIgnore(TestObject.class.getDeclaredField("str1")));
  }

  @Test
  public void testWritePrimitive_int() throws Exception {
    int primitive = 1;
    Integer obj = 2;
    sut.writePrimitive(primitive);
    sut.writePrimitive(obj);
    verify(jgen, times(1)).writeNumber(1);
    verify(jgen, times(1)).writeNumber(2);
  }

  @Test
  public void testWritePrimitive_long() throws Exception {
    long primitive = 1L;
    Long obj = 2L;
    sut.writePrimitive(primitive);
    sut.writePrimitive(obj);
    verify(jgen, times(1)).writeNumber(1L);
    verify(jgen, times(1)).writeNumber(2L);
  }

  @Test
  public void testWritePrimitive_double() throws Exception {
    double primitive = 1.0;
    Double obj = 2.0;
    sut.writePrimitive(primitive);
    sut.writePrimitive(obj);
    verify(jgen, times(1)).writeNumber(1.0);
    verify(jgen, times(1)).writeNumber(2.0);
  }

  @Test
  public void testWritePrimitive_float() throws Exception {
    float primitive = 1.0f;
    Float obj = 2.0f;
    sut.writePrimitive(primitive);
    sut.writePrimitive(obj);
    verify(jgen, times(1)).writeNumber(1.0f);
    verify(jgen, times(1)).writeNumber(2.0f);
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
  public void testWritePrimitive_string() throws Exception {
    sut.writePrimitive("test");
    verify(jgen, times(1)).writeString("test");
  }
}
