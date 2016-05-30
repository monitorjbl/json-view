package com.monitorjbl.json.server.controller;

import com.google.common.collect.ImmutableMap;
import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Controller
public class JsonController {
  private static final Logger log = LoggerFactory.getLogger(JsonController.class);
  private final JsonResult json = JsonResult.instance();

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
    obj.setList(asList("red", "blue", "green"));
    obj.setSub(new TestSubobject("qwerqwerqwerqw"));

    json.use(JsonView.with(obj)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect")));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/bean/withReturnValue")
  @ResponseBody
  public TestObject beanWithReturn() {
    TestObject obj = new TestObject();
    obj.setInt1(1);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    obj.setList(asList("red", "blue", "green"));
    obj.setSub(new TestSubobject("qwerqwerqwerqw"));

    return json.use(JsonView.with(obj)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect")))
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

    log.debug("GET testList()");

    json.use(JsonView.with(list)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect")));
  }

  @RequestMapping(method = RequestMethod.POST, value = "/bean")
  @ResponseBody
  public TestObject acceptData(@RequestBody TestObject object) {
    if(object.getDate().getTime() != 1433214360187L) {
      throw new RuntimeException("field not set properly");
    }
    log.debug("POST testNoninterference()");
    return object;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/map")
  @ResponseBody
  public Map<String, String> map() {
    return ImmutableMap.of(
        "red", "herring",
        "blue", "fish");
  }

  @RequestMapping(method = RequestMethod.GET, value = "/circularReference")
  @ResponseBody
  public ResponseEntity<TestSubobject> circular() {
    TestSubobject parent = new TestSubobject();
    TestSubobject child = new TestSubobject();

    child.setVal("child");
    child.setSubs(asList(parent));

    parent.setVal("parent");
    parent.setSubs(asList(child));

    parent = json.use(JsonView.with(parent)
        .onClass(TestSubobject.class, Match.match()
            .exclude("subs")))
        .returnValue();

    return ResponseEntity.ok(parent);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/responseEntity")
  @ResponseBody
  public ResponseEntity<TestObject> responseEntity() {
    TestObject obj = new TestObject();
    obj.setInt1(4);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("qwerqwer");
    return ResponseEntity.accepted()
        .header("TEST", "asdfasdf")
        .body(json.use(JsonView.with(obj)
            .onClass(TestObject.class, Match.match()
                .exclude("int1")
                .include("ignoredDirect")))
            .returnValue());
  }
}
