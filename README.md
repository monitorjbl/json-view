# JSON Views for Spring MVC

Ever needed to programmatically include or exclude a field from your Spring MVC response data? Well, if you have then you probably know by now that it's very difficult to do. Spring is by nature very declarative (annotations for everything!), so doing something programmatically gets ugly fast.

While the declarative style certainly has many benefits (compile-time checking, ease of refactoring, etc.), the inability to simply and programmatically control your responses is one major downside. Inspired by [VRaptor](http://www.vraptor.org/), this plugin provides an easy way to alter the JSON output on the fly.

## Use cases

The most common use case for this is when dealing with Hibernate POJOs. If you have an object with an expensive field on it, you may not always want to return it. Let's say that you've got this class:

```java
public class MyObject{
  private Long id;
  private String name;
  private MySmallObject smallObj;
  private List<MyBigObject> contains;       //expensive list with many entries

  //getters and setters and/or builder
}
```

If you were to return a list of `MyObject`, you may not want to show the `contains` field; with *n* instances of `MyObject` and *m* instances of `MyBigObject` per instance of `MyObject`, you'll be returning n\*m instances.

The typically suggested pattern suggests using the `@JsonIgnore` annotation on the field. However, this effectively makes this field permanently ignored everywhere in your app. What if you want do want to show this field when dealing with a single instance rather than a `List`?

Using `JsonResult` allows you to filter this field out quickly and easily in your controller methods:

```java
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonResult.with(list).onClass(MyObject.class, match().exclude("contains"));
}
```

You can also ignore fields on classes referenced by a class! Simply reference the field in a dot-path to do this. In the below example, the field `id` on the class `MySmallObject` is ignored:

```java
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonResult.with(list).onClass(MyObject.class, match()
      .exclude("smallObj.id")
      .exclude("contains"));
}
```

Alternatively, you can make a separate matcher for other classes:

```java
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonResult.with(list)
      .onClass(MyObject.class, match()
        .exclude("contains"))
      .onClass(MySmallObject.class, match()
        .exclude("id");
}
```

## Rules

The `JsonResult` object is built to make it simple to include/exclude fields from your POJOs. However, when parsing your specified config, you should be aware of the following rules:

1. `@JsonIgnore` and `@JsonIgnoreProperties` are respected, unless overridden by `include()`.
2. Class inheritance is respected. If you `match()` on a parent class's field, it will be respected without needing a separate `match()` for the parent class.
3. Higher class specificity in `Match.match()` overrides lower and it is *not* field-based; use of a matcher is an all-or-nothing affair based on the class for which you declare it to be used. Here are a couple of examples where this is important to keep in mind:
  1. If you provide matchers for both your class *and* its parent class, the parent's matcher will be used.
  2. If you provide matchers for Class A and Class B, and Class A has a field typed Class B, the following will occur
    1. If the matcher for Class A references the field in Class A, Class A's matcher will be respected
    2. If the matcher for Class A references fields in Class B with a path, Class B's matcher will be respected


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
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/user/{id}")
@ResponseBody
public void getUser(@PathVariable("id") Long id) {
  User user = service.get(id);

  if (!isAdmin()) {
    JsonResult.with(user)
        .onClass(User.class, match()
          .exclude("owner")
          .exclude("userDetails"));
  } else {
    JsonResult.with(user);
  }
}
```

#### List of objects
```java
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/user")
@ResponseBody
public void getUsers() {
  List<User> users = service.list();

  if (!isAdmin()) {
    JsonResult.with(user)
        .onClass(User.class, match()
          .exclude("owner")
          .exclude("userDetails"));
  } else {
    JsonResult.with(user);
  }
}
```



