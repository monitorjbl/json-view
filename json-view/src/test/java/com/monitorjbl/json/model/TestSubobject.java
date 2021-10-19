package com.monitorjbl.json.model;

public class TestSubobject {
  private String val;
  private String otherVal;
  private TestSubobject sub;
  private boolean booleanVal;
  private Boolean booleanBoxedVal;

  public TestSubobject(String val) {
    this.val = val;
  }

  public TestSubobject(String val, TestSubobject sub) {
    this.val = val;
    this.sub = sub;
  }

  public TestSubobject() {
  }

  public boolean isBooleanVal() {
    return booleanVal;
  }

  public void setBooleanVal(boolean booleanVal) {
    this.booleanVal = booleanVal;
  }

  public Boolean isBooleanBoxedVal() {
    return booleanBoxedVal;
  }

  public void setBooleanBoxedVal(Boolean booleanBoxedVal) {
    this.booleanBoxedVal = booleanBoxedVal;
  }

  public String getVal() {
    return val;
  }

  public void setVal(String val) {
    this.val = val;
  }

  public String getOtherVal() {
    return otherVal;
  }

  public void setOtherVal(String otherVal) {
    this.otherVal = otherVal;
  }

  public TestSubobject getSub() {
    return sub;
  }

  public void setSub(TestSubobject sub) {
    this.sub = sub;
  }
}