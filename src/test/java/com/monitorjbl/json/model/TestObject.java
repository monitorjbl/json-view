package com.monitorjbl.json.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties({"ignoreIndirect"})
public class TestObject {
  private String str1;
  private String str2;
  @JsonIgnore
  private String ignoredDirect;
  private String ignoreIndirect;
  private int int1;
  private TestSubobject sub;
  private List<String> list;
  private String[] array;

  public String getStr1() {
    return str1;
  }

  public void setStr1(String str1) {
    this.str1 = str1;
  }

  public String getStr2() {
    return str2;
  }

  public void setStr2(String str2) {
    this.str2 = str2;
  }

  public String getIgnoredDirect() {
    return ignoredDirect;
  }

  public void setIgnoredDirect(String ignoredDirect) {
    this.ignoredDirect = ignoredDirect;
  }

  public String getIgnoreIndirect() {
    return ignoreIndirect;
  }

  public void setIgnoreIndirect(String ignoreIndirect) {
    this.ignoreIndirect = ignoreIndirect;
  }

  public int getInt1() {
    return int1;
  }

  public void setInt1(int int1) {
    this.int1 = int1;
  }

  public TestSubobject getSub() {
    return sub;
  }

  public void setSub(TestSubobject sub) {
    this.sub = sub;
  }

  public List<String> getList() {
    return list;
  }

  public void setList(List<String> list) {
    this.list = list;
  }

  public String[] getArray() {
    return array;
  }

  public void setArray(String[] array) {
    this.array = array;
  }
}
