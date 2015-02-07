# JSON Views for Spring

Ever needed to programmatically include or exclude a field from your Spring MVC response data? Well, if you have then you probably know by now that it's very difficult to do. Spring is by nature very declarative (annotations for everything!), so doing something programmatically gets ugly fast.

Inspired by [VRaptor](http://www.vraptor.org/), this plugin provides an easy way to alter the JSON output on the fly. Check it out!

```
  @RequestMapping(method = RequestMethod.GET, value = "/user/{id}")
  @ResponseBody
  public TestObject getUser(@PathVariable("id") Long id, JsonResult result) {
    User user = service.get(id);
    if (!isAdmin()) {
      result
          .exclude("owner")
          .exclude("userDetails");
    }
    return obj;
  }
```

To use it, add this to your context as a bean:

```
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean();
  }
```


