package com.monitorjbl.json.model;

public class TestSubobject {
  private String val;
  private TestSubobject sub;

  public TestSubobject(String val) {
    this.val = val;
  }

  public TestSubobject(String val, TestSubobject sub) {
    this.val = val;
    this.sub = sub;
  }

  public TestSubobject() {
  }

  public String getVal() {
    return val;
  }

  public void setVal(String val) {
    this.val = val;
  }

  public TestSubobject getSub() {
    return sub;
  }

  public void setSub(TestSubobject sub) {
    this.sub = sub;
  }
}