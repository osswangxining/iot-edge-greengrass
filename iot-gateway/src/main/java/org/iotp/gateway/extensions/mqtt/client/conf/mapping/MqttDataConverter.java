package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotp.gateway.service.data.DeviceData;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = MqttJsonConverter.class, name = "json") })
public interface MqttDataConverter {

  List<DeviceData> convert(String topic, MqttMessage msg) throws Exception;

}
