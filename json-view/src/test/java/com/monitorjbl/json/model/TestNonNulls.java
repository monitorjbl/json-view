package com.monitorjbl.json.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.NON_NULL)
public class TestNonNulls {
  private String val;
  private String otherVal;
  private TestNonNulls sub;

  public TestNonNulls(String val) {
    this.val = val;
  }

  public TestNonNulls(String val, TestNonNulls sub) {
    this.val = val;
    this.sub = sub;
  }

  public TestNonNulls() {
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

  public TestNonNulls getSub() {
    return sub;
  }

  public void setSub(TestNonNulls sub) {
    this.sub = sub;
  }
}