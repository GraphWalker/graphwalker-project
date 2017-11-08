package org.graphwalker.studio;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.graphwalker.studio.util.LoggerUtil;
import org.graphwalker.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import static org.graphwalker.io.common.Util.printVersionInformation;

/**
 * @author Nils Olsson
 */
@ComponentScan
@EnableAutoConfiguration
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) throws UnknownHostException {
    Application application = new Application();
    try {
      application.run(args);
    } catch (Exception e) {
      // We should have caught all exceptions up until here, but there
      // might have been problems with the command parser for instance...
      System.err.println(e + System.lineSeparator());
      logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
    }
  }

  private void run(String[] args) {
    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName("java -jar graphwalker.jar");
    try {
      jc.parseWithoutValidation(args);
    } catch (Exception e) {
      // ignore
    }

    try {
      setLogLevel(options);

      if (options.help) {
        options = new Options();
        jc = new JCommander(options);
        jc.parse(args);
        jc.usage();
        return;
      } else if (options.version) {
        System.out.println(printVersionInformation());
        return;
      }

      WebSocketServer gwSocketServer = new WebSocketServer(options.wsPort);
      gwSocketServer.start();

      Properties props = System.getProperties();
      props.setProperty("server.port", String.valueOf(options.browserPort));

      SpringApplication application = new SpringApplication(Application.class);
      Environment environment = application.run(args).getEnvironment();
      logger.info("Access URLs:\n----------------------------------------------------------\n" +
        "  Local web service:          http://127.0.0.1:" + options.browserPort + "\n" +
        "  External web service:       http://" + InetAddress.getLocalHost().getHostAddress() + ":" + options.browserPort + "\n" +
        "  Local websocket service:    http://127.0.0.1:" + options.wsPort + "\n" +
        "  External websocket service: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + options.wsPort
        + "\n----------------------------------------------------------");

    } catch (ParameterException e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage() + System.lineSeparator());
      if (jc.getParsedCommand() != null) {
        jc.usage(jc.getParsedCommand());
      }
    } catch (Exception e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage() + System.lineSeparator());
      logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
    }
  }

  private void setLogLevel(Options options) {
    // OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
    try {
      LoggerUtil.setLogLevel(LoggerUtil.Level.valueOf(options.debug.toUpperCase()));
    } catch (Throwable e) {
      throw new ParameterException("Incorrect argument to --debug");
    }
  }
}
