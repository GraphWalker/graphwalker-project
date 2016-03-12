package org.graphwalker.studio;

import org.graphwalker.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Nils Olsson
 */
@ComponentScan
@EnableAutoConfiguration
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) throws UnknownHostException {
    WebSocketServer gwSocketServer = new WebSocketServer(9999);
    gwSocketServer.start();


    SpringApplication application = new SpringApplication(Application.class);
    application.setShowBanner(false);
    Environment environment = application.run(args).getEnvironment();
    log.info("Access URLs:\n----------------------------------------------------------\n\t" +
        "Local: \t\thttp://127.0.0.1:{}\n\t" +
        "External: \thttp://{}:{}\n----------------------------------------------------------",
      environment.getProperty("server.port"),
      InetAddress.getLocalHost().getHostAddress(),
      environment.getProperty("server.port"));
  }
}
