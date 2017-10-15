package org.iotp.gateway.extensions.mqtt.client;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.iotp.gateway.extensions.mqtt.client.conf.MqttClientConfiguration;
import org.iotp.gateway.service.GatewayService;
import org.iotp.gateway.util.ConfigurationTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Service
@ConditionalOnProperty(prefix = "mqtt", value = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DefaultMqttClientService implements MqttClientService {

  @Autowired
  private GatewayService service;

  @Value("${mqtt.configuration}")
  private String configurationFile;

  private List<MqttBrokerMonitor> brokers;

  @PostConstruct
  public void init() throws Exception {
    log.info("Initializing MQTT client service!");
    MqttClientConfiguration configuration;
    try {
      configuration = ConfigurationTools.readConfiguration(configurationFile, MqttClientConfiguration.class);
    } catch (Exception e) {
      log.error("MQTT client service configuration failed!", e);
      throw e;
    }

    try {
      brokers = configuration.getBrokers().stream().map(c -> new MqttBrokerMonitor(service, c))
          .collect(Collectors.toList());
      brokers.forEach(MqttBrokerMonitor::connect);
    } catch (Exception e) {
      log.error("MQTT client service initialization failed!", e);
      throw e;
    }
  }

  @PreDestroy
  public void preDestroy() {
    if (brokers != null) {
      brokers.forEach(MqttBrokerMonitor::disconnect);
    }
  }

}
