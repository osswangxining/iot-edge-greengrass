package org.iotp.gateway.extensions.mqtt.client.conf;

import java.util.List;

import lombok.Data;

/**
 */
@Data
public class MqttClientConfiguration {

  List<MqttBrokerConfiguration> brokers;

}
