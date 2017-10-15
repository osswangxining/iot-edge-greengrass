package org.iotp.gateway.extensions.mqtt.client;

import java.util.Arrays;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AttributeRequestsMapping;
import org.iotp.gateway.service.data.AttributeRequest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Data
@Slf4j
public class MqttAttributeRequestsMessageListener implements IMqttMessageListener {

  private final Consumer<AttributeRequest> consumer;
  private final AttributeRequestsMapping converter;

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    try {
      consumer.accept(converter.convert(topic, message));
    } catch (Exception e) {
      log.info("[{}] Failed to decode message: {}", topic, Arrays.toString(message.getPayload()), e);
    }
  }
}
