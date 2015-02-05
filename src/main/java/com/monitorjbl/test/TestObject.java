package com.monitorjbl.test;

public class TestObject {
  private String str1;
  private String str2;
  private int int1;

  private TestObject(Builder builder) {
    str1 = builder.str1;
    str2 = builder.str2;
    int1 = builder.int1;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String str1;
    private String str2;
    private int int1;

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

    public TestObject build() {
      return new TestObject(this);
    }
  }
}
