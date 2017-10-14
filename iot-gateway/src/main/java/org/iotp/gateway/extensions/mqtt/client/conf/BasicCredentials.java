package org.iotp.gateway.extensions.mqtt.client.conf;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 */
@Data
public class BasicCredentials implements MqttClientCredentials {

  private String username;
  private String password;

  @Override
  public void configure(MqttConnectOptions clientOptions) {
    clientOptions.setUserName(username);
    if (!StringUtils.isEmpty(password)) {
      clientOptions.setPassword(password.toCharArray());
    }
  }
}
