package org.iotp.gateway.service.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 */
@Configuration
@ConfigurationProperties(prefix = "gateway.reporting")
@Data
public class TbReportingConfiguration {

  private long interval;
  private int maxErrorsPerInterval;
}
