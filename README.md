# JSON Views for Spring MVC

Ever needed to programmatically include or exclude a field from your Spring MVC response data? Well, if you have then you probably know by now that it's very difficult to do. Spring is by nature very declarative (annotations for everything!), so doing something programmatically gets ugly fast.

While the declarative style certainly has many benefits (compile-time checking, ease of refactoring, etc.), the inability to simply and programmatically control your responses is one major downside. Inspired by [VRaptor](http://www.vraptor.org/), this library provides an easy way to alter the JSON output on the fly.

## Use cases

The potential use cases for this library are pretty varied, but here are a few to get you started.

### Exclusion

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

The typically suggested pattern suggests using the `@JsonIgnore` annotation on the field. However, this effectively makes this field permanently ignored everywhere in your app. What if you want only don't want to show this field when dealing with a single instance rather than a `List`?

Using `JsonView` allows you to filter this field out quickly and easily in your controller methods (note that your method return value must be `void`):

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonView.with(list).onClass(MyObject.class, match().exclude("contains"));
}
```

### Inclusion

The inverse of this is also possible. For example, let's say this was your class instead:

```java
public class MyObject{
  private Long id;
  private String name;
  private MySmallObject smallObj;
  @JsonIgnore
  private List<MyBigObject> contains;       //expensive list with many entries

  //getters and setters and/or builder
}
```

You can programmatically include fields that are ignored by default:


```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

@RequestMapping(method = RequestMethod.GET, value = "/myObject")
@ResponseBody
public void getMyObjects() {
    //get a list of the objects
    List<MyObject> list = myObjectService.list();

    //exclude expensive field
    JsonView.with(list).onClass(MyObject.class, match().include("contains"));
}
```

## Advanced

**Wildcard Matchers**

This is very handy if you have a limited

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

List<MyObject> list = myObjectService.list();

JsonView.with(list).onClass(MyObject.class, match()
      .exclude("*")
      .include("name"));
```

Wildcards are implemented with trenary logic. If you specify a matcher without a wildcard, it will supercede any other matchers with a wildcard.

**Class mMtchers**

You can also ignore fields on classes referenced by a class! Simply reference the field in a dot-path to do this. In the below example, the field `id` on the class `MySmallObject` is ignored:

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

List<MyObject> list = myObjectService.list();

JsonView.with(list).onClass(MyObject.class, match()
    .exclude("smallObj.id")
    .exclude("contains"));
```

Alternatively, you can make a separate matcher for other classes:

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

  //get a list of the objects
  List<MyObject> list = myObjectService.list();

  JsonView.with(list)
    .onClass(MyObject.class, match()
      .exclude("contains"))
    .onClass(MySmallObject.class, match()
      .exclude("id");
```

**Use outside of Spring MVC**

All this functionality really boils down to a custom Jackson serializer. If you'd like to use it outside of Spring, you certainly can! Just initialize a standard Jackson `ObjectMapper` class and tell it to serialize your object like so:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.json.JsonView;

import static com.monitorjbl.json.Match.match;

ObjectMapper mapper = new ObjectMapper();
SimpleModule module = new SimpleModule();
module.addSerializer(JsonView.class, new JsonViewSerializer());
mapper.registerModule(module);

mapper.writeValueAsString(JsonView.with(list)
      .onClass(MyObject.class, match()
        .exclude("contains"))
      .onClass(MySmallObject.class, match()
        .exclude("id"));
```

## Rules

The `JsonView` object is built to make it simple to include/exclude fields from your POJOs. However, when parsing your specified config, you should be aware of the following rules:

1. Class inheritance is respected. If you `match()` on a parent class's field, it will be respected without needing a separate `match()` for the parent class.
2. Higher class specificity in `Match.match()` overrides lower and it is *not* field-based; use of a matcher is an all-or-nothing affair based on the class for which you declare it to be used. Here are a couple of examples where this is important to keep in mind:
  1. If you provide matchers for both your class *and* its parent class, the parent's matcher will be used.
  2. If you provide matchers for Class A and Class B, and Class A has a field typed Class B, the following will occur
    1. If the matcher for Class A references the field in Class A, Class A's matcher will be respected
    2. If the matcher for Class A references fields in Class B with a path, Class B's matcher will be respected
3. `@JsonIgnore` on fields (not methods) and `@JsonIgnoreProperties` are respected, unless overridden by `include()`.
4. All serialization is done via fields only. There is no current support for method-based serialization.


## Usage

To use it, simply add this project to your classpath using your build tool of choice. This project is available on Maven Central, so if you're using Maven you can just add this to your pom.xml:

```xml
<dependency>
    <groupId>com.monitorjbl</groupId>
    <artifactId>spring-json-view</artifactId>
    <version>0.1</version>
</dependency>
```

A word of warning: this project was built for Spring 4+, integration with Spring 3 is not supported yet. Make sure you're using the correct version. If you are, just add it to your context as a bean:

**Java config**
```java
@EnableWebMvc
@Configuration
@ComponentScan({"com.monitorjbl"})
public class Context extends WebMvcConfigurerAdapter {
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean();
  }
}
```

**XML config**
```xml
<bean id="jsonViewSupport" class="com.monitorjbl.json.JsonViewSupportFactoryBean/>
```

## Design

Basic design information about this library.

#### Serializer
As stated above, the heart of this library is the custom Jackson serializer. The [JsonViewSerializer](src/main/java/com/monitorjbl/json/JsonViewSerializer.java) class interprets both the object to serialize and the include/exclude config to write your object as a String. Pretty much everything else is simply to integrate it nicely with Spring MVC.

#### Spring MVC Integration
The [JsonView](src/main/java/com/monitorjbl/json/JsonView.java) class that you refer to stores a `ThreadLocal` var containing your returned object and all configuration information. The `ThreadLocal` is used to store the result so your method doesn't have to return a particular type value. If you have used `JsonView` at all in your current thread, this value will be set and your response will generated from it.

#### External use information
Use of the JsonViewSerializer outside of the Spring MVC integration is fine, however there will still be a `ThreadLocal` reference to your object and config. If this cause an issue for you, you can simply call `JsonView.with()` again to reset the reference.
