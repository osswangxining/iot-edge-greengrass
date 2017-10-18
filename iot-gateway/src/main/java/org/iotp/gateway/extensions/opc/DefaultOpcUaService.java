package org.iotp.gateway.extensions.opc;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.iotp.gateway.extensions.opc.conf.OpcUaConfiguration;
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
@ConditionalOnProperty(prefix = "opc", value = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class DefaultOpcUaService implements OpcUaService {

    @Autowired
    private GatewayService service;

    @Value("${opc.configuration}")
    private String configurationFile;

    private List<OpcUaServerMonitor> monitors;

    @PostConstruct
    public void init() throws Exception {
        log.info("Initializing OPC-UA service!");
        OpcUaConfiguration configuration;
        try {
            configuration = ConfigurationTools.readConfiguration(configurationFile, OpcUaConfiguration.class);
        } catch (Exception e) {
            log.error("OPC-UA service configuration failed!", e);
            throw e;
        }

        try {
            monitors = configuration.getServers().stream().map(c -> new OpcUaServerMonitor(service, c)).collect(Collectors.toList());
            monitors.forEach(OpcUaServerMonitor::connect);
        } catch (Exception e) {
            log.error("OPC-UA service initialization failed!", e);
            throw e;
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (monitors != null) {
            monitors.forEach(OpcUaServerMonitor::disconnect);
        }
    }
}
