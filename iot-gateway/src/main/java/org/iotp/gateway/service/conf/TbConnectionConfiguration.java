package org.iotp.gateway.service.conf;

import org.iotp.gateway.service.MqttGatewaySecurityConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 */
@Configuration
@ConfigurationProperties(prefix = "gateway.connection")
@Data
public class TbConnectionConfiguration {

  private String host;
  private int port;
  private long retryInterval;
  private int maxInFlight;
  private MqttGatewaySecurityConfiguration security;

}
