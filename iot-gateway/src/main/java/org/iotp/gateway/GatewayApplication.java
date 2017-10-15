package org.iotp.gateway;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class GatewayApplication {

  private static final String SPRING_CONFIG_NAME_KEY = "--spring.config.name";
  public static final String DEFAULT_SPRING_CONFIG_PARAM = SPRING_CONFIG_NAME_KEY + "=" + "iot-gateway";

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, updateArguments(args));
  }

  private static String[] updateArguments(String[] args) {
    if (Arrays.stream(args).noneMatch(arg -> arg.startsWith(SPRING_CONFIG_NAME_KEY))) {
      String[] modifiedArgs = new String[args.length + 1];
      System.arraycopy(args, 0, modifiedArgs, 0, args.length);
      modifiedArgs[args.length] = DEFAULT_SPRING_CONFIG_PARAM;
      return modifiedArgs;
    }
    return args;
  }

}
