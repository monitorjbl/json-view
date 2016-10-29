package com.monitorjbl.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Configures default serializer settings in a Spring environment. This is configured
 * identically to a JsonView instance, simply use the onClass() method to set matchers.
 */
public class DefaultView {
  private final Map<Class, Match> matches;

  private DefaultView() {
    this.matches = new HashMap<>();
  }

  @SuppressWarnings("unchecked")
  public JsonView getMatch(Object obj) {
    if(obj == null) {
      return null;
    }

    JsonView<?> view = JsonView.with(obj);
    boolean found = false;
    if(obj instanceof Collection) {
      for(Object o : (Collection) obj) {
        Match m = contains(o.getClass());
        if(m != null) {
          view = view.onClass(o.getClass(), m);
          found = true;
        }
      }
    } else if(obj instanceof Map) {
      Map<Object, Object> map = (Map<Object, Object>) obj;
      for(Entry<Object, Object> e : map.entrySet()) {
        Match k = contains(e.getKey().getClass());
        Match v = contains(e.getValue().getClass());
        if(k != null) {
          view = view.onClass(e.getKey().getClass(), k);
          found = true;
        }
        if(v != null) {
          view = view.onClass(e.getValue().getClass(), v);
          found = true;
        }
      }
    } else {
      Match m = contains(obj.getClass());
      if(m != null) {
        view = view.onClass(obj.getClass(), m);
        found = true;
      }
    }

    if(found) {
      return view;
    } else {
      return null;
    }
  }

  public DefaultView onClass(Class cls, Match match) {
    matches.put(cls, match);
    return this;
  }

  private Match contains(Class cls) {
    if(cls == null) {
      return null;
    }

    Class current = cls;
    while(!current.equals(Object.class)) {
      //does current class exist in the map?
      if(matches.containsKey(current)) {
        return matches.get(current);
      }
      current = cls.getSuperclass();
    }
    return null;
  }

  public static DefaultView create() {
    return new DefaultView();
  }
}
