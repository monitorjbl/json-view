package com.monitorjbl.json.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.ALWAYS)
public class TestNulls {
  private String val;
  private String otherVal;
  private TestNulls sub;

  public TestNulls(String val) {
    this.val = val;
  }

  public TestNulls(String val, TestNulls sub) {
    this.val = val;
    this.sub = sub;
  }

  public TestNulls() {
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

  public TestNulls getSub() {
    return sub;
  }

  public void setSub(TestNulls sub) {
    this.sub = sub;
  }
}