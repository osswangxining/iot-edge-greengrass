package org.iotp.gateway.extensions.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.iotp.gateway.extensions.file.conf.FileTailConfiguration;
import org.iotp.gateway.service.GatewayService;
import org.iotp.gateway.util.ConfigurationTools;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
@Service
@ConditionalOnProperty(prefix = "file", value = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DefaultFileTailService {
    @Autowired
    private GatewayService service;

    @Value("${file.configuration}")
    private String configurationFile;

    private List<FileMonitor> brokers;

    @PostConstruct
    public void init() throws Exception {
        log.info("Initializing File Tail service!");
        FileTailConfiguration configuration;
        try {
            configuration = ConfigurationTools.readConfiguration(configurationFile, FileTailConfiguration.class);
        } catch (Exception e) {
            log.error("File Tail service configuration failed!", e);
            throw e;
        }

        try {
            brokers = configuration.getFileMonitorConfigurations().stream().map(c -> new FileMonitor(service, c)).collect(Collectors.toList());
            brokers.forEach(FileMonitor::init);
        } catch (Exception e) {
            log.error("File Tail service initialization failed!", e);
            throw e;
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (brokers != null) {
            brokers.forEach(FileMonitor::stop);
        }
    }

}
