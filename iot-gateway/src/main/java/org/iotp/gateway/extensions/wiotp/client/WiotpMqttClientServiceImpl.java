package org.iotp.gateway.extensions.wiotp.client;

import lombok.extern.slf4j.Slf4j;

import org.iotp.gateway.extensions.mqtt.client.conf.MqttClientConfiguration;
import org.iotp.gateway.service.GatewayService;
import org.iotp.gateway.util.ConfigurationTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;


@Service
@ConditionalOnProperty(prefix = "wiotp", value = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class WiotpMqttClientServiceImpl implements WiotpMqttClientService {

    @Autowired
    private GatewayService service;

    @Value("${wiotp.configuration}")
    private String configurationFile;

    private List<WiotpMqttBrokerMonitor> brokers;

    @PostConstruct
    public void init() throws Exception {
        log.info("Initializing WIOTP MQTT client service!");
        MqttClientConfiguration configuration;
        try {
            configuration = ConfigurationTools.readConfiguration(configurationFile, MqttClientConfiguration.class);
        } catch (Exception e) {
            log.error("WIOTP MQTT client service configuration failed!", e);
            throw e;
        }

        try {
            brokers = configuration.getBrokers().stream().map(c -> new WiotpMqttBrokerMonitor(service, c)).collect(Collectors.toList());
            brokers.forEach(WiotpMqttBrokerMonitor::connect);
        } catch (Exception e) {
            log.error("WIOTP MQTT client service initialization failed!", e);
            throw e;
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (brokers != null) {
            brokers.forEach(WiotpMqttBrokerMonitor::disconnect);
        }
    }


}
