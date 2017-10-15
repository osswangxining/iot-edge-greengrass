package org.iotp.gateway.extensions.mqtt.client;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotp.gateway.service.data.RpcCommandResponse;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Data
@Slf4j
public class MqttRpcResponseMessageListener implements IMqttMessageListener {

  private final int requestId;
  private final String deviceName;
  private final BiConsumer<String, RpcCommandResponse> consumer;

  @Override
  public void messageArrived(String topic, MqttMessage msg) throws Exception {
    RpcCommandResponse response = new RpcCommandResponse();
    response.setRequestId(requestId);
    response.setDeviceName(deviceName);
    response.setData(new String(msg.getPayload(), StandardCharsets.UTF_8));
    consumer.accept(topic, response);
  }
}
