package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import lombok.Data;

/**
 */
@Data
public class AttributeUpdatesMapping {

  private String deviceNameFilter;
  private String attributeFilter;
  private String topicExpression;
  private String valueExpression;

}
