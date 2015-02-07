# JSON Views for Spring MVC

Ever needed to programmatically include or exclude a field from your Spring MVC response data? Well, if you have then you probably know by now that it's very difficult to do. Spring is by nature very declarative (annotations for everything!), so doing something programmatically gets ugly fast.

Inspired by [VRaptor](http://www.vraptor.org/), this plugin provides an easy way to alter the JSON output on the fly.

## Use cases

The most common use case for this is when dealing with Hibernate POJOs. If you have an object with an expensive field on it, you may not always want to return it. Let's say that you've got this class:

```java
public class MyObject{
  private Long id;
  private String name;
  private List<MyOtherObject> contains;       //expensive list with many entries

  //getters and setters and/or builder
}
```

If you were to return a list of `MyObject`, you may not want to show the `contains` field; with *n* instances of `MyObject` and *m* instances of `MyOtherObject` per instance of `MyObject`, you'll be returning n\*m instances.

The typically suggested pattern suggests using the `@JsonIgnore` annotation on the field. However, this effectively makes this field permanently ignored everywhere in your app. What if you want do want to show this field when dealing with a single instance rather than a `List`?

Using `JsonResult` allows you to filter this field out quickly and easily in your controller methods:

```java
@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonResult.with(list).onClass(MyObject.class, Match.on().exclude("contains"));
}
```

## Usage


To use it, simply add this project to your classpath using your build tool of choice. Here's a Maven example:

```xml
<dependency>
    <groupId>com.monitorjbl</groupId>
    <artifactId>spring-json-view</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Then, just add it to your context as a bean:

```java
@Bean
public JsonResultSupportFactoryBean views() {
  return new JsonResultSupportFactoryBean();
}
```

## Examples

#### Simple object
```java
@RequestMapping(method = RequestMethod.GET, value = "/user/{id}")
@ResponseBody
public void getUser(@PathVariable("id") Long id) {
  User user = service.get(id);

  if (!isAdmin()) {
    JsonResult.with(user)
        .onClass(User.class, Match.on()
          .exclude("owner")
          .exclude("userDetails"));
  } else {
    JsonResult.with(user);
  }
}
```

#### List of objects
```java
@RequestMapping(method = RequestMethod.GET, value = "/user")
@ResponseBody
public void getUsers() {
  List<User> users = service.list();

  if (!isAdmin()) {
    JsonResult.with(user)
        .onClass(User.class, Match.on()
          .exclude("owner")
          .exclude("userDetails"));
  } else {
    JsonResult.with(user);
  }
}
```



