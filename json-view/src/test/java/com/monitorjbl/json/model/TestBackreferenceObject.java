package com.monitorjbl.json.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class TestBackreferenceObject {

  private String id;
  @JsonBackReference
  private List<TestForwardReferenceObject> children;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TestForwardReferenceObject> getChildren() {
    return children;
  }

  public void setChildren(List<TestForwardReferenceObject> children) {
    this.children = children;
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  public static class TestForwardReferenceObject{
    private String id;
    @JsonManagedReference
    private TestBackreferenceObject parent;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public TestBackreferenceObject getParent() {
      return parent;
    }

    public void setParent(TestBackreferenceObject parent) {
      this.parent = parent;
    }
  }
}
