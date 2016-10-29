package com.monitorjbl.json.server.controller;

import com.monitorjbl.json.DefaultView;
import com.monitorjbl.json.model.TestDefaultViewObject;

import static com.monitorjbl.json.Match.match;

public class DefaultViewConfig {
  private static final DefaultView defaultView = DefaultView.create().onClass(TestDefaultViewObject.class, match().exclude("ignoredString"));

  public static DefaultView instance() {
    return defaultView;
  }
}
