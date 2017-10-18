package org.iotp.gateway.extensions.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.iotp.gateway.extensions.http.conf.HttpConfiguration;
import org.iotp.gateway.extensions.http.conf.HttpConverterConfiguration;
import org.iotp.gateway.extensions.http.conf.mapping.HttpDeviceDataConverter;
import org.iotp.gateway.service.GatewayService;
import org.iotp.gateway.service.MqttDeliveryFuture;
import org.iotp.gateway.service.data.DeviceData;
import org.iotp.gateway.util.ConfigurationTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "http", value = "enabled", havingValue = "true")
public class DefaultHttpService implements HttpService {

    private static final int OPERATION_TIMEOUT_IN_SEC = 10;

    @Value("${http.configuration}")
    private String configurationFile;

    @Autowired
    private GatewayService gateway;

    private Map<String, HttpConverterConfiguration> httpConverterConfigurations;

    @PostConstruct
    public void init() throws IOException {
        HttpConfiguration configuration;
        try {
            configuration = ConfigurationTools.readConfiguration(configurationFile, HttpConfiguration.class);
            if (configuration.getConverterConfigurations() != null) {
                httpConverterConfigurations = configuration
                        .getConverterConfigurations()
                        .stream()
                        .collect(Collectors.toMap(HttpConverterConfiguration::getConverterId, Function.identity()));
            } else {
                httpConverterConfigurations = configuration
                        .getDeviceTypeConfigurations()
                        .stream()
                        .collect(Collectors.toMap(HttpConverterConfiguration::getDeviceTypeId, Function.identity()));
            }
        } catch (IOException e) {
            log.error("Http service configuration failed!", e);
            throw e;
        }
    }

    @Override
    public void processRequest(String converterId, String token, String body) throws Exception {
        log.trace("Processing request body [{}] for converterId [{}] and token [{}]", body, converterId, token);
        HttpConverterConfiguration configuration = httpConverterConfigurations.get(converterId);
        if (configuration != null) {
            if (configuration.getToken().equals(token)) {
                processBody(body, configuration);
            }
            else {
                log.error("Request token [{}] for converter id [{}] doesn't match configuration token!", token, converterId);
                throw new SecurityException("Request token [" + token + "] for converter id [" + converterId + "] doesn't match configuration token!");
            }
        } else {
            log.error("No configuration found for converter id [{}]. Please add configuration for this converter id!", converterId);
            throw new IllegalArgumentException("No configuration found for converter id [" + converterId + "]. Please add configuration for this converter id!");
        }
    }

    private void processBody(String body, HttpConverterConfiguration configuration) throws Exception {
        for (HttpDeviceDataConverter converter : configuration.getConverters()) {
            if (converter.isApplicable(body)) {
                DeviceData dd = converter.parseBody(body);
                if (dd != null) {
                    waitWithTimeout(gateway.onDeviceConnect(dd.getName(), dd.getType()));
                    List<MqttDeliveryFuture> futures = new ArrayList<>();
                    if (!dd.getAttributes().isEmpty()) {
                        futures.add(gateway.onDeviceAttributesUpdate(dd.getName(), dd.getAttributes()));
                    }
                    if (!dd.getTelemetry().isEmpty()) {
                        futures.add(gateway.onDeviceTelemetry(dd.getName(), dd.getTelemetry()));
                    }
                    for (Future future : futures) {
                        waitWithTimeout(future);
                    }
                    Optional<MqttDeliveryFuture> future = gateway.onDeviceDisconnect(dd.getName());
                    if (future.isPresent()) {
                        waitWithTimeout(future.get());
                    }
                } else {
                    log.error("DeviceData is null. Body [{}] was not parsed successfully!", body);
                    throw new IllegalArgumentException("Device Data is null. Body [" + body + "] was not parsed successfully!");
                }
            }
        }
    }

    private void waitWithTimeout(Future future) throws Exception {
        future.get(OPERATION_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
    }
}
