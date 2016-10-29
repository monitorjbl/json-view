package com.monitorjbl.json.server;

import com.monitorjbl.json.DefaultView;
import com.monitorjbl.json.model.TestDefaultViewObject;

import static com.monitorjbl.json.Match.match;

public class DefaultViewFactory {
  private static final DefaultView defaultView = DefaultView.create().onClass(TestDefaultViewObject.class, match().exclude("ignoredString"));

  public static DefaultView instance() {
    return defaultView;
  }
}
