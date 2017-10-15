package org.iotp.gateway.service.conf;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Configuration
@ConfigurationProperties(prefix = "gateway.persistence")
@Data
@Slf4j
public class TbPersistenceConfiguration {

  private String type;
  private String path;
  private int bufferSize;

  public MqttClientPersistence getPersistence() {
    if (StringUtils.isEmpty(type) || type.equals("memory")) {
      log.info("Initializing default memory persistence!");
      return new MemoryPersistence();
    } else if (type.equals("file")) {
      if (StringUtils.isEmpty(path)) {
        log.info("Initializing default file persistence!");
        return new MqttDefaultFilePersistence();
      } else {
        log.info("Initializing file persistence using directory: {}", path);
        return new MqttDefaultFilePersistence(path);
      }
    } else {
      log.error("Unknown persistence option: {}. Only 'memory' and 'file' are supported at the moment!", type);
      throw new IllegalArgumentException("Unknown persistence option: " + type + "!");
    }
  }
}
