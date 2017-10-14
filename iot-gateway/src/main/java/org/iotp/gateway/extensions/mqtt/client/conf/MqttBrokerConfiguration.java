package org.iotp.gateway.extensions.mqtt.client.conf;

import java.util.List;

import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AttributeRequestsMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AttributeUpdatesMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.DeviceConnectMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.DeviceDisconnectMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.MqttTopicMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.ServerSideRpcMapping;

import lombok.Data;

/**
 */
@Data
public class MqttBrokerConfiguration {
  private String host;
  private int port;
  private boolean ssl;
  private String clientId;
  private String truststore;
  private String truststorePassword;
  private long retryInterval;
  private MqttClientCredentials credentials;
  private List<MqttTopicMapping> mapping;
  private List<DeviceConnectMapping> connectRequests;
  private List<DeviceDisconnectMapping> disconnectRequests;
  private List<AttributeRequestsMapping> attributeRequests;
  private List<AttributeUpdatesMapping> attributeUpdates;
  private List<ServerSideRpcMapping> serverSideRpc;
}
