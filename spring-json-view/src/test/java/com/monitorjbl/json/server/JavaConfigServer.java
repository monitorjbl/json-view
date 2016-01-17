package com.monitorjbl.json.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class JavaConfigServer implements ConfigServer{
  public static final Logger log = LoggerFactory.getLogger(JavaConfigServer.class);
  private boolean running;
  private Thread thread;

  public synchronized void start(final int port) {
    if(thread != null) {
      throw new IllegalStateException("Server is already running");
    }

    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Server server = new Server(port);

          final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
          applicationContext.register(Context.class);

          final ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(applicationContext));
          final ServletContextHandler context = new ServletContextHandler();
          context.setContextPath("/");
          context.addServlet(servletHolder, "/*");

          server.setHandler(context);

          running = true;
          server.start();
          log.info("Server started");

          while(running) {
            Thread.sleep(1);
          }

          server.stop();
          log.info("Server stopped");
        } catch(Exception e) {
          log.error("Server exception", e);
          throw new RuntimeException(e);
        }
      }
    });
    thread.start();
  }

  public void stop() {
    running = false;
    try {
      thread.join();
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new JavaConfigServer().start(9090);
  }
}
