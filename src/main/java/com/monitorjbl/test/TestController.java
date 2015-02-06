package com.monitorjbl.test;

import com.monitorjbl.json.JsonResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

@Controller
public class TestController {
  @RequestMapping(method = RequestMethod.GET, value = "/map")
  @ResponseBody
  public TestObject map(JsonResult result) {
    result.exclude("int1");
    return TestObject.builder()
        .int1(1)
        .str2("asdf")
        .list(Arrays.asList("red", "blue", "green"))
        .sub(new TestSubobject("qwerqwerqwerqw"))
        .build();
  }
}
