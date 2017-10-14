package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import lombok.Data;

/**
 */
@Data
public class ServerSideRpcMapping {

  private String deviceNameFilter;
  private String methodFilter;
  private String requestTopicExpression;
  private String responseTopicExpression;
  private long responseTimeout;
  private String valueExpression;

}
