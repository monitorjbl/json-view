package com.monitorjbl.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.monitorjbl.json.MatcherBehavior.CLASS_FIRST;
import static com.monitorjbl.json.MatcherBehavior.PATH_FIRST;
import static java.util.Arrays.asList;

public class JsonViewSerializer extends JsonSerializer<JsonView> {
  public static boolean log = false;
  /**
   * Cached results from expensive (pure) methods
   */
  private final Memoizer memoizer;

  /**
   * Map of custom serializers to take into account when serializing fields.
   */
  private Map<Class<?>, JsonSerializer<Object>> customSerializersMap = null;

  private MatcherBehavior defaultMatcherBehavior = CLASS_FIRST;

  public JsonViewSerializer() {
    this(1024);
  }

  public JsonViewSerializer(int maxCacheSize) {
    this.memoizer = new Memoizer(maxCacheSize);
  }

  /**
   * Registering custom serializer allows to the JSonView to deal with custom serializations for certains field types.<br>
   * This way you could register for instance a JODA serialization as  a DateTimeSerializer. <br>
   * Thus, when JSonView find a field of that type (DateTime), it will delegate the serialization to the serializer specified.<br>
   * Example:<br>
   * <code>
   * JsonViewSupportFactoryBean bean = new JsonViewSupportFactoryBean( mapper );
   * bean.registerCustomSerializer( DateTime.class, new DateTimeSerializer() );
   * </code>
   *
   * @param <T>     Type class of the serializer
   * @param cls     {@link Class} The class type you want to add a custom serializer
   * @param forType {@link JsonSerializer} The serializer you want to apply for that type
   */
  @SuppressWarnings("unchecked")
  public <T> void registerCustomSerializer(Class<T> cls, JsonSerializer<T> forType) {
    if(customSerializersMap == null) {
      customSerializersMap = new HashMap<>();
    }

    if(cls == null) {
      throw new IllegalArgumentException("Class must not be null");
    } else if(cls.equals(JsonView.class)) {
      throw new IllegalArgumentException("Class cannot be " + JsonView.class);
    } else if(customSerializersMap.containsKey(cls)) {
      throw new IllegalArgumentException("Class " + cls + " already has a serializer registered (" + customSerializersMap.get(cls) + ")");
    }

    customSerializersMap.put(cls, (JsonSerializer<Object>) forType);
  }

  /**
   * Unregister a previously registered serializer. @see registerCustomSerializer
   *
   * @param cls The class type the serializer was registered for
   */
  public void unregisterCustomSerializer(Class<?> cls) {
    if(customSerializersMap != null) {
      customSerializersMap.remove(cls);
    }
  }

  /**
   * Set the default matcher behavior to be used if the {@link JsonView} object to
   * be serialized does not specify one.
   *
   * @param defaultMatcherBehavior The default behavior to use
   */
  public void setDefaultMatcherBehavior(MatcherBehavior defaultMatcherBehavior) {
    this.defaultMatcherBehavior = defaultMatcherBehavior;
  }

  @Override
  public void serialize(JsonView result, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
    new JsonWriter(serializers, jgen, result).write(null, result.getValue());
  }

  class JsonWriter {
    Stack<String> path = new Stack<>();
    String currentPath = "";
    Match currentMatch = null;
    AccessibleProperty referringField = null;

    final SerializerProvider serializerProvider;
    final JsonGenerator jgen;
    final JsonView result;

    JsonWriter(SerializerProvider serializerProvider, JsonGenerator jgen, JsonView result) {
      this.serializerProvider = serializerProvider;
      this.jgen = jgen;
      this.result = result;
    }

    //internal use only to encapsulate what the current state was
    private JsonWriter(JsonGenerator jgen, JsonView result, Match currentMatch, SerializerProvider serializerProvider) {
      this.jgen = jgen;
      this.result = result;
      this.currentMatch = currentMatch;
      this.serializerProvider = serializerProvider;
    }

    //internal use only to encapsulate what the current state was
    private JsonWriter(JsonGenerator jgen, JsonView result, Match currentMatch,
                       String currentPath, Stack<String> path, AccessibleProperty referringField, SerializerProvider serializerProvider) {
      this.jgen = jgen;
      this.result = result;
      this.currentMatch = currentMatch;
      this.currentPath = currentPath;
      this.referringField = referringField;
      this.path = path;
      this.serializerProvider = serializerProvider;
    }

    boolean writePrimitive(Object obj) throws IOException {
      if(obj instanceof String) {
        jgen.writeString((String) obj);
      } else if(obj instanceof Integer) {
        jgen.writeNumber((Integer) obj);
      } else if(obj instanceof Long) {
        jgen.writeNumber((Long) obj);
      } else if(obj instanceof Short) {
        jgen.writeNumber((Short) obj);
      } else if(obj instanceof Double) {
        jgen.writeNumber((Double) obj);
      } else if(obj instanceof Float) {
        jgen.writeNumber((Float) obj);
      } else if(obj instanceof Character) {
        jgen.writeNumber((Character) obj);
      } else if(obj instanceof Byte) {
        jgen.writeNumber((Byte) obj);
      } else if(obj instanceof Boolean) {
        jgen.writeBoolean((Boolean) obj);
      } else if(obj == null) {
        jgen.writeNull();
      } else if(obj instanceof BigDecimal) {
        jgen.writeNumber((BigDecimal) obj);
      } else {
        return false;
      }
      return true;
    }

    boolean writeSpecial(Object obj) throws IOException {
      if(obj instanceof Date) {
        serializerProvider.defaultSerializeDateValue((Date) obj, jgen);
      } else if(obj instanceof Temporal) {
        serializerProvider.defaultSerializeValue(obj, jgen);
      } else if(obj instanceof URL) {
        jgen.writeString(obj.toString());
      } else if(obj instanceof URI) {
        jgen.writeString(obj.toString());
      } else if(obj instanceof UUID) {
        jgen.writeString(obj.toString());
      } else if(obj instanceof Class) {
        jgen.writeString(((Class) obj).getCanonicalName());
      } else {
        return false;
      }
      return true;
    }

    boolean writeEnum(Object obj) throws IOException {
      if(obj.getClass().isEnum()) {
        jgen.writeString(((Enum) obj).name());
      } else {
        return false;
      }
      return true;
    }

    @SuppressWarnings("unchecked")
    boolean writeList(Object obj) throws IOException {
      if(obj instanceof List || obj instanceof Set || obj.getClass().isArray()) {
        Iterable iter;
        if(obj.getClass().isArray()) {
          if(obj instanceof byte[]) {
            jgen.writeBinary((byte[]) obj);
            return true;
          } else {
            iter = convertArray(obj);
          }
        } else {
          iter = (Iterable<Object>) obj;
        }

        jgen.writeStartArray();
        for(Object o : iter) {
          new JsonWriter(jgen, result, currentMatch, currentPath, path, referringField, serializerProvider).write(null, o);
        }
        jgen.writeEndArray();
      } else {
        return false;
      }
      return true;
    }

    @SuppressWarnings("unchecked")
    Iterable convertArray(Object obj) {
      Iterable iter;
      if(obj instanceof int[]) {
        int[] arr = (int[]) obj;
        iter = new ArrayList<>();
        for(int v : arr) {
          ((List<Integer>) iter).add(v);
        }
      } else if(obj instanceof double[]) {
        double[] arr = (double[]) obj;
        iter = new ArrayList<>();
        for(double v : arr) {
          ((List<Double>) iter).add(v);
        }
      } else if(obj instanceof float[]) {
        float[] arr = (float[]) obj;
        iter = new ArrayList<>();
        for(float v : arr) {
          ((List<Float>) iter).add(v);
        }
      } else if(obj instanceof long[]) {
        long[] arr = (long[]) obj;
        iter = new ArrayList<>();
        for(long v : arr) {
          ((List<Long>) iter).add(v);
        }
      } else if(obj instanceof short[]) {
        short[] arr = (short[]) obj;
        iter = new ArrayList<>();
        for(short v : arr) {
          ((List<Short>) iter).add(v);
        }
      } else if(obj instanceof char[]) {
        char[] arr = (char[]) obj;
        iter = new ArrayList<>();
        for(char v : arr) {
          ((List<Character>) iter).add(v);
        }
      } else if(obj instanceof boolean[]) {
        boolean[] arr = (boolean[]) obj;
        iter = new ArrayList<>();
        for(boolean v : arr) {
          ((List<Boolean>) iter).add(v);
        }
      } else {
        iter = asList((Object[]) obj);
      }
      return iter;
    }

    @SuppressWarnings("unchecked")
    boolean writeMap(Object obj) throws IOException {
      if(obj instanceof Map) {
        Map<Object, Object> map = (Map<Object, Object>) obj;

        jgen.writeStartObject();
        for(Object key : map.keySet()) {
          jgen.writeFieldName(key.toString());
          new JsonWriter(jgen, result, currentMatch, serializerProvider).write(null, map.get(key));
        }
        jgen.writeEndObject();
      } else {
        return false;
      }
      return true;
    }

    @SuppressWarnings("unchecked")
    void writeObject(Object obj) throws IOException {
      jgen.writeStartObject();

      List<AccessibleProperty> fields = getAccessibleProperties(obj.getClass());

      for(AccessibleProperty property : fields) {
        try {
          //if the field has a serializer annotation on it, serialize with it
          if(fieldAllowed(property, obj.getClass())) {
            Object val = readField(obj, property);
            if(!valueAllowed(property, val, obj.getClass())) {
              continue;
            }

            String name = getFieldName(property);
            jgen.writeFieldName(name);

            JsonSerializer fieldSerializer = annotatedWithJsonSerialize(property);
            if(fieldSerializer != null) {
              fieldSerializer.serialize(val, jgen, serializerProvider);
            } else if(customSerializersMap != null && val != null) {
              JsonSerializer<Object> serializer = customSerializersMap.get(val.getClass());
              if(serializer != null) {
                serializer.serialize(val, jgen, serializerProvider);
              } else {
                new JsonWriter(jgen, result, currentMatch, currentPath, path, property, serializerProvider).write(name, val);
              }
            } else if(val instanceof JsonNode) {
              // Let Jackson deal with these, they're special
              serializerProvider.defaultSerializeValue(val, jgen);
            } else {
              new JsonWriter(jgen, result, currentMatch, currentPath, path, property, serializerProvider).write(name, val);
            }
          }
        } catch(IllegalArgumentException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }

      jgen.writeEndObject();
    }

    boolean valueAllowed(AccessibleProperty property, Object value, Class cls) {
      Include defaultInclude = serializerProvider.getConfig() == null ? Include.ALWAYS : serializerProvider.getConfig().getSerializationInclusion();
      JsonInclude jsonInclude = getAnnotation(property, JsonInclude.class);
      JsonSerialize jsonSerialize = getAnnotation(cls, JsonSerialize.class);

      // Make sure local annotations win over global ones
      if(jsonInclude != null && jsonInclude.value() == Include.NON_NULL && value == null) {
        return false;
      }

      return value != null
          || (defaultInclude == Include.ALWAYS && jsonSerialize == null)
          || (jsonSerialize != null && jsonSerialize.include() == Inclusion.ALWAYS);
    }

    /**
     * Do a search for *all* matchers for a class. This takes into account all relevant
     * parents in the class hierarchy. If multiple matches are found, the matches will
     * be unioned together.
     */
    @SuppressWarnings("unchecked")
    private Optional<Match> classMatchSearch(Class declaringClass) {
//      return memoizer.classMatches(result, declaringClass, () -> {
      List<Match> matches = new ArrayList<>();
      Stack<Class> classes = new Stack<>();
      classes.push(declaringClass);
      while(!classes.isEmpty()) {
        Class cls = classes.pop();
        Match match = result.getMatch(cls);

        if(match != null) {
          matches.add(match);
        }
        if(cls.getInterfaces() != null) {
          Stream.of(cls.getInterfaces()).forEach(c -> classes.push(c));
        }
        if(cls.getSuperclass() != null && !cls.getSuperclass().equals(Object.class)) {
          classes.push(cls.getSuperclass());
        }
      }

      if(matches.size() == 1) {
        return Optional.of(matches.get(0));
      } else if(matches.size() > 1) {
        // Join all the includes and excludes
        Match unionMatch = new Match();
        matches.forEach(m -> {
          unionMatch.getExcludes().addAll(m.getExcludes());
          unionMatch.getIncludes().addAll(m.getIncludes());
          unionMatch.getTransforms().putAll(m.getTransforms());
        });
        return Optional.of(unionMatch);
      } else {
        return Optional.empty();
      }
//      });
    }

    @SuppressWarnings("unchecked")
    boolean fieldAllowed(AccessibleProperty property, Class declaringClass) {
      String name = property.name;
      if(Modifier.isStatic(property.modifiers)) {
        return false;
      }

      MatchPrefixTuple tuple = getMatchPrefix(declaringClass);
      String prefix = tuple.prefix;
      Match match = tuple.match;

      //if there is a match, respect it
      if(match != null) {

        if(currentMatch == null) {
          currentMatch = match;
        }

        int included = containsMatchingPattern(match.getIncludes(), prefix + name, true);
        int excluded = containsMatchingPattern(match.getExcludes(), prefix + name, false);

        /*
        The logic for this is a little complex. We're dealing with ternary logic to
        properly handle wildcard matches. We want matches made with wildcards to be
        overruled by matches without them.
         */
        if(included == 1) {
          return true;
        } else if(excluded == 1) {
          return false;
        } else if(included == 0) {
          return true;
        } else if(excluded == 0) {
          return false;
        } else {
          return !annotatedWithIgnore(property);
        }
      } else {
        //else, respect JsonIgnore only
        return !annotatedWithIgnore(property);
      }
    }

    MatchPrefixTuple getMatchPrefix(Class declaringClass) {
      String prefix = currentPath.length() > 0 ? currentPath + "." : "";

      // Determine matcher behavior
      MatcherBehavior currentBehavior = result.matcherBehavior;
      if(currentBehavior == null) {
        currentBehavior = JsonViewSerializer.this.defaultMatcherBehavior;
      }

      //search for matching class
      Match match = null;
      if(currentBehavior == CLASS_FIRST) {
        match = classMatchSearch(declaringClass).orElse(null);
        if(match == null) {
          match = currentMatch;
        } else {
          prefix = "";
        }
      } else if(currentBehavior == PATH_FIRST) {
        if(currentMatch != null) {
          match = currentMatch;
        } else {
          match = classMatchSearch(declaringClass).orElse(null);
          prefix = "";
        }
      }

      return new MatchPrefixTuple(match, prefix);
    }

    Object readField(Object obj, AccessibleProperty field) throws IllegalAccessException {
      MatchPrefixTuple tuple = getMatchPrefix(obj.getClass());
      if(tuple.match != null && tuple.match.getTransforms().containsKey(tuple.prefix + field.name)) {
        return tuple.match.getTransforms().get(tuple.prefix + field.name).apply(obj, field.get(obj));
      } else {
        return field.get(obj);
      }
    }

    void write(String fieldName, Object value) throws IOException {
      if(fieldName != null) {
        path.push(fieldName);
        updateCurrentPath();
      }

      //try to handle all primitives/special cases before treating this as json object
      if(!writePrimitive(value) && !writeSpecial(value) && !writeEnum(value) && !writeList(value) && !writeMap(value)) {
        writeObject(value);
      }

      if(fieldName != null) {
        path.pop();
        updateCurrentPath();
      }
    }

    void updateCurrentPath() {
      StringBuilder builder = new StringBuilder();
      for(String s : path) {
        builder.append(".");
        builder.append(s);
      }
      currentPath = builder.length() > 0 ? builder.toString().substring(1) : "";
    }

    @SuppressWarnings("unchecked")
    <E> E readClassAnnotation(Class cls, Class annotationType, String methodName) {
      try {
        for(Annotation an : getAnnotations(cls)) {
          Class<? extends Annotation> type = an.annotationType();
          if(an.annotationType().equals(annotationType)) {
            for(Method method : type.getDeclaredMethods()) {
              if(method.getName().equals(methodName)) {
                return (E) method.invoke(an, (Object[]) null);
              }
            }
            throw new IllegalArgumentException("Method " + methodName + " not found on annotation " + annotationType);
          }
        }
        throw new IllegalArgumentException("Annotation " + annotationType + " not found on class " + cls);
      } catch(InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Returns one of the following values:
     * <pre>
     * -1: No match found
     *  0: Wildcard-based match
     *  1: Non-wildcard match
     * </pre>
     * <p>
     * This method is memoized to speed up execution time
     */
    int containsMatchingPattern(Set<String> values, String pattern, boolean matchPrefix) {
      return memoizer.matches(values, pattern, matchPrefix, () -> {
        int match = -1;
        for(String val : values) {
          String replaced = val.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
          if(Pattern.compile(replaced).matcher(pattern).matches() || (matchPrefix && val.startsWith(pattern + "."))) {
            match = replaced.contains("*") ? 0 : 1;
            break;
          }
        }
        return match;
      });
    }

    /**
     * Returns a boolean indicating whether the provided field is annotated with
     * some form of ignore. This method is memoized to speed up execution time
     */
    boolean annotatedWithIgnore(AccessibleProperty f) {
      return memoizer.annotatedWithIgnore(f, () -> {
        JsonIgnore jsonIgnore = getAnnotation(f, JsonIgnore.class);
        JsonIgnoreProperties classIgnoreProperties = getAnnotation(f.declaringClass, JsonIgnoreProperties.class);
        JsonIgnoreProperties fieldIgnoreProperties = null;
        boolean backReferenced = false;

        //make sure the referring field didn't specify properties to ignore
        if(referringField != null) {
          fieldIgnoreProperties = getAnnotation(referringField, JsonIgnoreProperties.class);
        }

        //make sure the referring field didn't specify a backreference annotation
        if(getAnnotation(f, JsonBackReference.class) != null && referringField != null) {
          for(AccessibleProperty lastField : getAccessibleProperties(referringField.declaringClass)) {
            JsonManagedReference fieldManagedReference = getAnnotation(lastField, JsonManagedReference.class);
            if(fieldManagedReference != null && lastField.type.equals(f.declaringClass)) {
              backReferenced = true;
              break;
            }
          }
        }

        return (jsonIgnore != null && jsonIgnore.value()) ||
            (classIgnoreProperties != null && asList(classIgnoreProperties.value()).contains(f.name)) ||
            (fieldIgnoreProperties != null && asList(fieldIgnoreProperties.value()).contains(f.name)) ||
            backReferenced;
      });
    }

    JsonSerializer annotatedWithJsonSerialize(AccessibleProperty property) {
      JsonSerialize jsonSerialize = getAnnotation(property, JsonSerialize.class);
      if(jsonSerialize != null) {
        if(!jsonSerialize.using().equals(JsonSerializer.None.class)) {
          try {
            return jsonSerialize.using().newInstance();
          } catch(InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      }
      return null;
    }

    private Class<?>[] getInterfaces(Class cls) {
      return cls.getInterfaces();
    }

    private List<AccessibleProperty> getAccessibleProperties(Class cls) {
      return memoizer.accessibleProperty(cls, () -> {
        // Gather all fields and methods
        Map<String, AccessibleProperty> accessibleProperties = new LinkedHashMap<>();
        Predicate<Field> shouldProcessField = fieldVisibilityAllowed(cls);
        Predicate<Method> shouldProcessMethod = getterVisibilityAllowed(cls);
        Predicate<Object> visible = (o) -> {
          if(o instanceof Field) {
            return shouldProcessField.test((Field) o);
          } else if(o instanceof Method) {
            return shouldProcessMethod.test((Method) o);
          } else {
            throw new RuntimeException("Could not process property of type " + o.getClass());
          }
        };

        getDeclaredFields(cls).stream()
            .map(f -> new AccessibleProperty(f.getName(), f.getAnnotations(), f))
            .forEach(p -> accessibleProperties.put(p.name, p));
        getDeclaredMethods(cls).stream()
            .filter(m -> m.getName().startsWith("get") && !m.getReturnType().equals(Void.class) && m.getParameters().length == 0)
            .map(m -> new AccessibleProperty(getFieldNameFromGetter(m), m.getAnnotations(), m))
            .forEach(p -> {
              AccessibleProperty field = accessibleProperties.get(p.name);

              // Combine annotations from the getter and the field
              if(field != null) {
                Set<Annotation> annotations = new HashSet<Annotation>(asList(field.annotations));
                annotations.addAll(asList(p.annotations));
                p = new AccessibleProperty(p.name, annotations.toArray(new Annotation[0]), p.property);
              }

              // TODO: Makes sure combined annotations are applied to the field when method visibility is disallowed
              if(shouldProcessMethod.test((Method) p.property)) {
                accessibleProperties.put(p.name, p);
              }
            });

        return accessibleProperties.values().stream()
            .filter(p -> visible.test(p.property))
            .collect(Collectors.toList());
      });
    }

    private List<Field> getDeclaredFields(Class cls) {
      List<Field> fields = new ArrayList<>();
      Stack<Class> parents = new Stack<>();
      parents.push(cls);

      while(!parents.isEmpty()) {
        Class c = parents.pop();

        Stream.of(c.getDeclaredFields()).forEach(f -> fields.add(f));

        if(c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
          parents.push(c.getSuperclass());
        }
      }

      return fields;
    }

    private List<Method> getDeclaredMethods(Class cls) {
      List<Method> methods = new ArrayList<>();
      Stack<Class> parents = new Stack<>();
      parents.push(cls);

      while(!parents.isEmpty()) {
        Class c = parents.pop();

        Stream.of(c.getDeclaredMethods()).forEach(m -> methods.add(m));

        if(c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
          parents.push(c.getSuperclass());
        }

        if(c.getInterfaces() != null) {
          Stream.of(c.getInterfaces()).forEach(i -> parents.push(i));
        }
      }

      return methods;
    }

    private Annotation[] getAnnotations(Class cls) {
      return memoizer.annotations(cls, () -> {
        return cls.getAnnotations();
      });
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getAnnotation(Class cls, Class<T> annotation) {
      Annotation[] annotations = getAnnotations(cls);
      if(annotations != null) {
        for(Annotation a : annotations) {
          if(a.annotationType().equals(annotation)) {
            return (T) a;
          }
        }
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> T getAnnotation(AccessibleProperty property, Class<T> annotation) {
      if(property.annotations != null) {
        for(Annotation a : property.annotations) {
          if(a.annotationType().equals(annotation)) {
            return (T) a;
          }
        }
      }
      return null;
    }

    private Predicate<Field> fieldVisibilityAllowed(Class cls) {
      JsonAutoDetect autoDetect = getAnnotation(cls, JsonAutoDetect.class);

      if(autoDetect == null) {
        return f -> false;
      } else {
        switch(autoDetect.fieldVisibility()) {
          case ANY:
            return f -> true;
          case PUBLIC_ONLY:
            return f -> Modifier.isPublic(f.getModifiers());
          case PROTECTED_AND_PUBLIC:
            return f -> Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers());
          case NON_PRIVATE:
            return f -> !Modifier.isPrivate(f.getModifiers());
          case DEFAULT:
          case NONE:
            return f -> false;
          default:
            throw new RuntimeException("No support for field visibility " + autoDetect.fieldVisibility());
        }
      }
    }

    private Predicate<Method> getterVisibilityAllowed(Class cls) {
      JsonAutoDetect autoDetect = getAnnotation(cls, JsonAutoDetect.class);

      if(autoDetect == null) {
        return m -> true;
      } else {
        switch(autoDetect.getterVisibility()) {
          case DEFAULT:
          case ANY:
            return m -> true;
          case PUBLIC_ONLY:
            return m -> Modifier.isPublic(m.getModifiers());
          case PROTECTED_AND_PUBLIC:
            return m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers());
          case NON_PRIVATE:
            return m -> !Modifier.isPrivate(m.getModifiers());
          case NONE:
            return m -> false;
          default:
            throw new RuntimeException("No support for field visibility " + autoDetect.fieldVisibility());
        }
      }
    }

    private String getFieldName(AccessibleProperty property) {
      JsonProperty jsonProperty = getAnnotation(property, JsonProperty.class);
      if(jsonProperty != null && jsonProperty.value().length() > 0) {
        return jsonProperty.value();
      } else {
        return property.name;
      }
    }

    private String getFieldNameFromGetter(Method method) {
      if (method.getName().equals("get")) {
    	  return method.getName();
      }
      String name = method.getName().replaceFirst("get", "");
      return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

  }

  private static class MatchPrefixTuple {
    private final Match match;
    private final String prefix;

    public MatchPrefixTuple(Match match, String prefix) {
      this.match = match;
      this.prefix = prefix;
    }
  }

  static class AccessibleProperty {
    public final Class declaringClass;
    public final String name;
    public final Class type;
    public final Annotation[] annotations;
    public final int modifiers;
    public final Object property;
    private final Function<Object, Object> getter;

    public AccessibleProperty(String name, Annotation[] annotations, Object property) {
      this.name = name;
      this.annotations = annotations;
      this.property = property;

      if(property instanceof Field) {
        this.declaringClass = ((Field) property).getDeclaringClass();
        this.type = ((Field) property).getType();
        this.modifiers = ((Field) property).getModifiers();
        this.getter = this::getFromField;
      } else if(property instanceof Method) {
        this.declaringClass = ((Method) property).getDeclaringClass();
        this.type = ((Method) property).getReturnType();
        this.modifiers = ((Method) property).getModifiers();
        this.getter = this::getFromMethod;
      } else {
        throw new RuntimeException("Unable to access property from " + property);
      }
    }

    public Object get(Object obj) {
      return getter.apply(obj);
    }

    private Object getFromField(Object obj) {
      try {
        ((Field) property).setAccessible(true);
        return ((Field) property).get(obj);
      } catch(IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    private Object getFromMethod(Object obj) {
      try {
        ((Method) property).setAccessible(true);
        return ((Method) property).invoke(obj);
      } catch(IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean equals(Object o) {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;
      AccessibleProperty that = (AccessibleProperty) o;
      return Objects.equals(declaringClass, that.declaringClass) &&
          Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

      return Objects.hash(declaringClass, name);
    }
  }
}
