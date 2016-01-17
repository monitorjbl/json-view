package com.monitorjbl.json;

import com.monitorjbl.json.server.XmlConfigServer;
import org.junit.BeforeClass;

public class XmlConfigurationTest extends ConfigTest {

  @BeforeClass()
  public static void init() throws Exception {
    server = new XmlConfigServer();
    ConfigTest.start();
  }

}
