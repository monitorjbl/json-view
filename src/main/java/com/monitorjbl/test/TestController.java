package com.monitorjbl.test;

import com.monitorjbl.json.JsonResult;
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
public class TestController {
  @Autowired
  private RequestMappingHandlerAdapter adapter;
  
  @RequestMapping(method = RequestMethod.GET, value = "/bean")
  @ResponseBody
  public TestObject bean(JsonResult result) {
    result.exclude("int1");
    return TestObject.builder()
        .int1(1)
        .str2("asdf")
        .list(Arrays.asList("red", "blue", "green"))
        .sub(new TestSubobject("qwerqwerqwerqw"))
        .build();
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
