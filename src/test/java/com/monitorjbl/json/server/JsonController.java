package com.monitorjbl.json.server;

import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.Arrays;
import java.util.HashMap;
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

  @RequestMapping(method = RequestMethod.GET, value = "/bean")
  @ResponseBody
  public TestObject bean(JsonResult result) {
    TestObject obj = new TestObject();
    obj.setInt1(1);
    obj.setIgnoredDirect("ignored");
    obj.setStr2("asdf");
    obj.setList(Arrays.asList("red", "blue", "green"));
    obj.setSub(new TestSubobject("qwerqwerqwerqw"));

    result.exclude("int1").include("ignoredDirect");
    return obj;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/map")
  @ResponseBody
  public Map<String, String> map() {
    return new HashMap<String, String>() {{
      put("red", "herring");
      put("blue", "fish");
    }};
  }
}
