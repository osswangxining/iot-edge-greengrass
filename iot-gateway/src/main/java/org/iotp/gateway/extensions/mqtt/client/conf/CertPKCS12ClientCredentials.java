package org.iotp.gateway.extensions.mqtt.client.conf;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import lombok.Data;

@Data
public class CertPKCS12ClientCredentials implements MqttClientCredentials {
  @Override
  public void configure(MqttConnectOptions clientOptions) {
    throw new RuntimeException("PKCS12 client credentials are not supported yet!");
  }
}
