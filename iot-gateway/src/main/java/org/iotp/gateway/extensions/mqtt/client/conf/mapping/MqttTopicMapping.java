package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import lombok.Data;

/**
 */
@Data
public class MqttTopicMapping {

  private String topicFilter;
  private MqttDataConverter converter;

}
