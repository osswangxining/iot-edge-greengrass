package org.iotp.gateway.extensions.mqtt.client.conf;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import lombok.Data;

@Data
public class CertJKSClientCredentials implements MqttClientCredentials {
  @Override
  public void configure(MqttConnectOptions clientOptions) {
    throw new RuntimeException("JKS client credentials are not supported yet!");
  }
}
