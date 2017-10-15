package org.iotp.gateway.extensions.mqtt.client;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AbstractJsonConverter;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.DeviceStateChangeMapping;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class MqttDeviceStateChangeMessageListener extends AbstractJsonConverter implements IMqttMessageListener {

  private final DeviceStateChangeMapping mapping;
  private final Consumer<String> deviceNameConsumer;
  private Pattern deviceNameTopicPattern;

  @Override
  public void messageArrived(String topic, MqttMessage msg) throws Exception {
    try {
      if (!StringUtils.isEmpty(mapping.getDeviceNameTopicExpression())) {
        deviceNameConsumer.accept(eval(topic));
      } else {
        String data = new String(msg.getPayload(), StandardCharsets.UTF_8);
        DocumentContext document = JsonPath.parse(data);
        deviceNameConsumer.accept(eval(document, mapping.getDeviceNameJsonExpression()));
      }
    } catch (Exception e) {
      log.error("Failed to convert msg", e);
    }
  }

  private String eval(String topic) {
    if (deviceNameTopicPattern == null) {
      deviceNameTopicPattern = Pattern.compile(mapping.getDeviceNameTopicExpression());
    }
    Matcher matcher = deviceNameTopicPattern.matcher(topic);
    while (matcher.find()) {
      return matcher.group();
    }
    return null;
  }
}
