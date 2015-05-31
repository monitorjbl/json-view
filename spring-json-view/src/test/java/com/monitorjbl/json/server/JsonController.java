package com.monitorjbl.json.server;

import com.google.common.collect.ImmutableMap;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class JsonController {
  @Autowired
  private RequestMappingHandlerAdapter adapter;

  @RequestMapping(method = RequestMethod.GET, value = "/ready")
  @ResponseBody
  public String ready() {
    return "readys";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/blank")
  @ResponseBody
  public void blank() {
    //do nothing
  }

  @RequestMapping(method = RequestMethod.GET, value = "/bean")
  @ResponseBody
  public void bean() {
    TestObject obj = new TestObject();
    obj.setInt1(1);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    obj.setList(Arrays.asList("red", "blue", "green"));
    obj.setSub(new TestSubobject("qwerqwerqwerqw"));

    JsonView.with(obj)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect"));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/bean/withReturnValue")
  @ResponseBody
  public TestObject beanWithReturn() {
    TestObject obj = new TestObject();
    obj.setInt1(1);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    obj.setList(Arrays.asList("red", "blue", "green"));
    obj.setSub(new TestSubobject("qwerqwerqwerqw"));

    return JsonView.with(obj)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect"))
        .returnValue();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/list")
  @ResponseBody
  public void list() {
    List<TestObject> list = new ArrayList<>();
    TestObject obj = new TestObject();
    obj.setInt1(1);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    list.add(obj);
    obj = new TestObject();
    obj.setInt1(2);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    list.add(obj);

    JsonView.with(list)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect"));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/map")
  @ResponseBody
  public Map<String, String> map() {
    return ImmutableMap.of(
        "red", "herring",
        "blue", "fish"
    );
  }
}
