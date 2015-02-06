package com.monitorjbl.test;

import java.util.List;

public class TestObject {
  private String str1;
  private String str2;
  private int int1;
  private TestSubobject sub;
  private List<String> list;
  private String[] array;

  public TestObject() {
  }

  private TestObject(Builder builder) {
    setStr1(builder.str1);
    setStr2(builder.str2);
    setInt1(builder.int1);
    setSub(builder.sub);
    setList(builder.list);
    setArray(builder.array);
  }

  public static Builder builder() {
    return new Builder();
  }

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


  public static final class Builder {
    private String str1;
    private String str2;
    private int int1;
    private TestSubobject sub;
    private List<String> list;
    private String[] array;

    private Builder() {
    }

    public Builder str1(String str1) {
      this.str1 = str1;
      return this;
    }

    public Builder str2(String str2) {
      this.str2 = str2;
      return this;
    }

    public Builder int1(int int1) {
      this.int1 = int1;
      return this;
    }

    public Builder sub(TestSubobject sub) {
      this.sub = sub;
      return this;
    }

    public Builder list(List<String> list) {
      this.list = list;
      return this;
    }

    public Builder array(String[] array) {
      this.array = array;
      return this;
    }

    public TestObject build() {
      return new TestObject(this);
    }
  }
}
