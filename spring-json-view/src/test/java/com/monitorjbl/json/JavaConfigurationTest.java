package com.monitorjbl.json;

import com.monitorjbl.json.server.JavaConfigServer;
import org.junit.BeforeClass;

public class JavaConfigurationTest extends ConfigTest {

  @BeforeClass()
  public static void init() throws Exception {
    server = new JavaConfigServer();
    ConfigTest.start();
  }

}
