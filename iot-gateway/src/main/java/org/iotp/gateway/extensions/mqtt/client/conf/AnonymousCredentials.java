package org.iotp.gateway.extensions.mqtt.client.conf;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 */
public class AnonymousCredentials implements MqttClientCredentials {

  @Override
  public void configure(MqttConnectOptions clientOptions) {

  }
}
