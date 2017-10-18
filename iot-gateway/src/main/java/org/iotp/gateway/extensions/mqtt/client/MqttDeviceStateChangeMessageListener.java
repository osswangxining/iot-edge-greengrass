package org.iotp.gateway.extensions.mqtt.client;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.util.StringUtils;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.DeviceStateChangeMapping;
import org.iotp.gateway.util.converter.AbstractJsonConverter;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class MqttDeviceStateChangeMessageListener extends AbstractJsonConverter implements IMqttMessageListener {

    private final DeviceStateChangeMapping mapping;
    private final BiConsumer<String, String> deviceNameConsumer;
    private Pattern deviceNameTopicPattern;

    @Override
    public void messageArrived(String topic, MqttMessage msg) throws Exception {
        try {
            String deviceName = null;
            String deviceType = null;
            if (!StringUtils.isEmpty(mapping.getDeviceNameTopicExpression())) {
                deviceName = eval(topic);
            } else {
                String data = new String(msg.getPayload(), StandardCharsets.UTF_8);
                DocumentContext document = JsonPath.parse(data);
                deviceName = eval(document, mapping.getDeviceNameJsonExpression());
            }

            if (!StringUtils.isEmpty(mapping.getDeviceTypeJsonExpression())) {
                deviceType = eval(topic);
            } else if (!StringUtils.isEmpty(mapping.getDeviceTypeTopicExpression())) {
                String data = new String(msg.getPayload(), StandardCharsets.UTF_8);
                DocumentContext document = JsonPath.parse(data);
                deviceType = eval(document, mapping.getDeviceTypeTopicExpression());
            }

            if (deviceName != null) {
                deviceNameConsumer.accept(deviceName, deviceType);
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
