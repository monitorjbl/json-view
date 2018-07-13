[![Run Status](https://api.shippable.com/projects/55cfbb00edd7f2c052a980a8/badge?branch=master)](https://app.shippable.com/projects/55cfbb00edd7f2c052a980a8)

# Programmatic JSON Views

Ever needed to programmatically include or exclude a field when serializing object with Jackson? Well, if you have then you probably know by now that it's very difficult to do. Jackson is by nature very declarative (annotations for everything!), so doing something programmatically gets ugly fast.

While the declarative style certainly has many benefits (compile-time checking, ease of refactoring, etc.), the inability to simply and programmatically control your inclusions/exclusions is one major downside. Inspired by [VRaptor](http://www.vraptor.org/), this library provides an easy way to alter serialized output on the fly.

* [JsonView](#jsonview)
  * [Usage](#usage)
  * [Including](#including)
  * [Typical use cases](#typical-use-cases)
    * [Excluding](#excluding)
    * [Including](#including)
  * [Advanced use cases](#advanced-use-cases)
    * [Wildcard matchers](#wildcard-matchers)
    * [Class matchers](#class-matchers)
  * [Custom Serializers](#custom-serializers)
  * [Field Transformations](#field-transformations)
  * [Rules](#rules)
* [Spring Integration](#spring-integration)
  * [Including](#including-1)
  * [Configuration](#configuration)
  * [Usage](#usage-1)
  * [Return value](#return-value)
* [Building from source](#building-from-source)

# JsonView

All the functionality of this library really boils down to a custom Jackson serializer.

## Usage

Just initialize a standard Jackson `ObjectMapper` class like so:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.JsonViewSerializer;

//initialize jackson
ObjectMapper mapper = new ObjectMapper().registerModule(new JsonViewModule());
```

## Including

To use it, simply add this project to your classpath using your build tool of choice. This project is available on Maven Central, so if you're using Maven you can just add this to your pom.xml:

```xml
<dependency>
    <groupId>com.monitorjbl</groupId>
    <artifactId>json-view</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Typical use cases

The potential use cases for this library are pretty varied, but here are a few to get you started.

### Exclusion

The most common use case for this is when you have an object with an expensive (big) field on it. You may not always want to serialize it. Let's say that you've got this class:

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

Using `JsonView` allows you to filter this field out quickly and easily:

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

//get a list of the objects
List<MyObject> list = myObjectService.list();

//exclude expensive field
String json = mapper.writeValueAsString(JsonView.with(list).onClass(MyObject.class, match().exclude("contains")));
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

//get a list of the objects
List<MyObject> list = myObjectService.list();

//exclude expensive field
String json = mapper.writeValueAsString(JsonView.with(list).onClass(MyObject.class, match().include("contains")));
```

## Advanced use cases

But wait, there's more!

### Wildcard matchers

This is very handy if you have a limited set of fields you actually want to include.

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

//get a list of the objects
List<MyObject> list = myObjectService.list();

String json = mapper.writeValueAsString(JsonView.with(list).onClass(MyObject.class, match()
      .exclude("*")
      .include("name")));
```

Wildcards are implemented with trenary logic. If you specify a matcher without a wildcard, it will supercede any other matchers with a wildcard.

### Class matchers

You can also ignore fields on classes referenced by a class! Simply reference the field in a dot-path to do this. In the below example, the field `id` on the class `MySmallObject` is ignored:

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

List<MyObject> list = myObjectService.list();

String json = mapper.writeValueAsString(JsonView.with(list).onClass(MyObject.class, match()
    .exclude("smallObj.id")
    .exclude("contains")));
```

Alternatively, you can make a separate matcher for other classes:

```java
import com.monitorjbl.json.JsonView;
import static com.monitorjbl.json.Match.match;

//get a list of the objects
List<MyObject> list = myObjectService.list();

String json = mapper.writeValueAsString(JsonView.with(list)
    .onClass(MyObject.class, match()
        .exclude("contains"))
    .onClass(MySmallObject.class, match()
        .exclude("id"));
```

## Custom Serializers

Due to the way json-view works, it must assume that it can serialize any class (except for certain [special types](json-view/src/main/java/com/monitorjbl/json/JsonViewSerializer.java#L169). If you want to use another custom serializer alongside `JsonViewSerializer`, you must explicitly register them with the `JsonViewSerializer` instance. This is a little backwards compared to the way normal registration works, but its unfortunately necessary. However, the `JsonViewModule` class provides an easy way to do this:

```java
ObjectMapper mapper = new ObjectMapper().registerModule(new JsonViewModule()
      .registerSerializer(Date.class, new MyCustomDateSerializer())
      .registerSerializer(URL.class, new MyCustomURLSerializer()));
```

## Field Transformations

If you have a field that needs to be transformed in a programatic way, there are ways to do so [inside Jackson](https://stackoverflow.com/a/12046979). These are generally intended to be static transformations, and while they can be used in a dynamic way, they often are simply painful to use. json-view can be used to dynamically perform transforms with lambdas:

```java
JsonView.with(ref)
        .onClass(TestObject.class, match()
            .exclude("*")
            .include("str1")
            .transform("str1", (TestObject t, String f) -> f.toUpperCase()))
```

## Rules

The `JsonView` object is built to make it simple to include/exclude fields from your POJOs. However, when parsing your specified config, you should be aware of the following rules:

1. Matching logic is trenary and wildcard matches are "less true" than specific matches.
2. `includes()` supercedes `excludes()` on equivalent level of matches.
3. Class inheritance is respected. If you `match()` on a parent class's field, it will be respected without needing a separate `match()` for the parent class.
4. Higher class specificity in `Match.match()` overrides lower and it is *not* field-based; use of a matcher is an all-or-nothing affair based on the class for which you declare it to be used. Here are a couple of examples where this is important to keep in mind:
  1. If you provide matchers for both your class *and* its parent class, the parent's matcher will be used.
  2. If you provide matchers for Class A and Class B, and Class A has a field typed Class B, the following will occur
    1. If the matcher for Class A references the field in Class A, Class A's matcher will be respected
    2. If the matcher for Class A references fields in Class B with a path, Class B's matcher will be respected
5. `@JsonIgnore` on fields (not methods) and `@JsonIgnoreProperties` are respected, unless overridden by `include()`.
6. All serialization is done via fields only. There is no current support for method-based serialization.

# Spring Integration

The Spring integration is really a `ThreadLocal` wrapper around the `JsonView` object.

## Including

To use it, simply add this project to your classpath using your build tool of choice. This project is available on Maven Central, so if you're using Maven you can just add this to your pom.xml:

```xml
<dependency>
    <groupId>com.monitorjbl</groupId>
    <artifactId>spring-json-view</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Configuration

A word of warning: this project was built for Spring 4+, integration with Spring 3 is not supported yet. Make sure you're using the correct version. If you are, just add it to your context as a bean:

**Java config**
```java
@EnableWebMvc
@Configuration
public class Context extends WebMvcConfigurerAdapter {
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean();
  }
}
```

**XML config**
```xml
<bean id="jsonViewSupport" class="com.monitorjbl.json.JsonViewSupportFactoryBean"/>
```

## Usage

Using it is very simple:

```java
import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import com.monitorjbl.json.model.TestObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class JsonController {
  private JsonResult json = JsonResult.instance();
  @Autowired
  private TestObjectService service;

  @RequestMapping(method = RequestMethod.GET, value = "/bean")
  @ResponseBody
  public void getTestObject() {
    List<TestObject> list = service.list();

    json.use(JsonView.with(list)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect")));
  }
}
```

## Return value

While the return value of the method isn't actually used with this library, documentation libraries like Swagger may depend on it being present. To make life simpler, you can simply tack on a `.returnValue()` to the end to grab the object you're manipulating:

```java
import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import com.monitorjbl.json.model.TestObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class JsonController {
  private JsonResult json = JsonResult.instance();
  @Autowired
  private TestObjectService service;

  @RequestMapping(method = RequestMethod.GET, value = "/bean")
  @ResponseBody
  public List<TestObject> getTestObject() {
    List<TestObject> list = service.list();

    return json.use(JsonView.with(list)
        .onClass(TestObject.class, Match.match()
            .exclude("int1")
            .include("ignoredDirect")))
         .returnValue();
  }
}
```

## Default views

If you would like to set common views for specific classes, simply include a `DefaultView` instance in the `JsonViewSupportFactoryBean`.

**Java config**
```java
@EnableWebMvc
@Configuration
public class Context extends WebMvcConfigurerAdapter {
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean(DefaultView.create()
        .onClass(TestObject.class, Match.match()
          .exclude("int1")
          .include("ignoredDirect")));
  }
}
```

**XML config**

For a real example, look at the following test files:

* [DefaultViewFactory](spring-json-view/src/test/java/com/monitorjbl/json/server/DefaultViewFactory.java)
* [XML configuration](spring-json-view/src/test/resources/context.xml)

```xml
<bean id="jsonViewSupport" class="com.monitorjbl.json.JsonViewSupportFactoryBean">
  <constructor-arg ref="defaultView"/>
</bean>

<!-- Bean in which you create a factory method to generate a DefaultView instance -->
<bean id="defaultView" class="com.monitorjbl.json.server.DefaultViewFactory" factory-method="instance"/>

```

# Building from source

To build, all you need is Java 8+, Maven 3+, and git:

```
# Checkout code from GitHub
git clone https://github.com/monitorjbl/json-view.git
cd json-view

# Build and install to local Maven repo
mvn clean install
```

Once you've done this, you can refer to the latest version of the library in your POM, like so:

```
<dependency>
  <groupId>com.monitorjbl</groupId>
  <artifactId>json-view</artifactId>
  <version>1.0.1</version>
</dependency>
```
