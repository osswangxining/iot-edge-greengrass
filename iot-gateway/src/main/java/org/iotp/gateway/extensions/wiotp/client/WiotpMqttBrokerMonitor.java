package org.iotp.gateway.extensions.wiotp.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.iotp.gateway.extensions.mqtt.client.MqttAttributeRequestsMessageListener;
import org.iotp.gateway.extensions.mqtt.client.MqttDeviceStateChangeMessageListener;
import org.iotp.gateway.extensions.mqtt.client.MqttRpcResponseMessageListener;
import org.iotp.gateway.extensions.mqtt.client.MqttTelemetryMessageListener;
import org.iotp.gateway.extensions.mqtt.client.conf.MqttBrokerConfiguration;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AttributeRequestsMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.AttributeUpdatesMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.DeviceStateChangeMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.MqttTopicMapping;
import org.iotp.gateway.extensions.mqtt.client.conf.mapping.ServerSideRpcMapping;
import org.iotp.gateway.service.AttributesUpdateListener;
import org.iotp.gateway.service.GatewayService;
import org.iotp.gateway.service.RpcCommandListener;
import org.iotp.gateway.service.data.AttributeRequest;
import org.iotp.gateway.service.data.AttributeResponse;
import org.iotp.gateway.service.data.AttributesUpdateSubscription;
import org.iotp.gateway.service.data.DeviceData;
import org.iotp.gateway.service.data.RpcCommandData;
import org.iotp.gateway.service.data.RpcCommandResponse;
import org.iotp.gateway.service.data.RpcCommandSubscription;
import org.iotp.infomgt.data.kv.KvEntry;
import org.springframework.util.StringUtils;

import com.ibm.iotf.client.app.ApplicationClient;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WiotpMqttBrokerMonitor implements MqttCallback, AttributesUpdateListener, RpcCommandListener {
    private ApplicationClient myClient = null;    
  
    private final UUID clientId = UUID.randomUUID();
    private final GatewayService gateway;
    private final MqttBrokerConfiguration configuration;
    private final Set<String> devices;
    private final AtomicInteger msgIdSeq = new AtomicInteger();

    private MqttAsyncClient client;
    private MqttConnectOptions clientOptions;
    private Object connectLock = new Object();

    //TODO: probably use newScheduledThreadPool(int threadSize) to improve performance in heavy load cases
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> deviceKeepAliveTimers = new ConcurrentHashMap<>();

    public WiotpMqttBrokerMonitor(GatewayService gateway, MqttBrokerConfiguration configuration) {
        this.gateway = gateway;
        this.configuration = configuration;
        this.devices = new HashSet<>();
    }

    public void connect() {
        try {
            client = new MqttAsyncClient((configuration.isSsl() ? "ssl" : "tcp") + "://" + configuration.getHost() + ":" + configuration.getPort(),
                    getClientId(), new MemoryPersistence());
            client.setCallback(this);
            clientOptions = new MqttConnectOptions();
//            clientOptions.setUserName("a-u88ncg-1qo8utyxwr");
//            clientOptions.setPassword("KB?fB1sG-1GFb?wLvx".toCharArray());
            clientOptions.setCleanSession(true);
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            clientOptions.setSocketFactory(sslContext.getSocketFactory());
            
            if (configuration.isSsl() && !StringUtils.isEmpty(configuration.getTruststore())) {
                Properties sslProperties = new Properties();
                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, configuration.getTruststore());
                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, configuration.getTruststorePassword());
                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
                sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, false);
                clientOptions.setSSLProperties(sslProperties);
            }
            configuration.getCredentials().configure(clientOptions);
          
//          Properties props = new Properties();
//          props.put("id", "appId001");
//          props.put("Organization-ID", "u88ncg");
//          props.put("Authentication-Method", "apikey");
//          props.put("API-Key", "a-u88ncg-1qo8utyxwr");
//          props.put("Authentication-Token", "KB?fB1sG-1GFb?wLvx");
//          props.put("Device-Type", "DC_SensorType");
//          props.put("Device-ID", "my-device-001");
//          props.put("Shared-Subscription", "false");
//          props.put("Clean-Session", "true");
//          
//          myClient = new ApplicationClient(props);
          
            client.connect(clientOptions).waitForCompletion(1000 * 60);
            if (client.isConnected()) {
              System.out.println("Successfully connected " + "to the IBM Watson IoT Platform");
            }
            
            checkConnection();
            if (configuration.getAttributeUpdates() != null) {
                configuration.getAttributeUpdates().forEach(mapping ->
                        gateway.subscribe(new AttributesUpdateSubscription(mapping.getDeviceNameFilter(), this))
                );
            }
            if (configuration.getServerSideRpc() != null) {
                configuration.getServerSideRpc().forEach(mapping ->
                        gateway.subscribe(new RpcCommandSubscription(mapping.getDeviceNameFilter(), this))
                );
            }
        } catch (Exception e) {
            log.error("[{}:{}] MQTT broker connection failed!", configuration.getHost(), configuration.getPort(), e);
            throw new RuntimeException("MQTT broker connection failed!", e);
        }
    }

    private String getClientId() {
        return StringUtils.isEmpty(configuration.getClientId()) ? clientId.toString() : configuration.getClientId();
    }

    public void disconnect() {
        devices.forEach(gateway::onDeviceDisconnect);
        scheduler.shutdownNow();
    }

    private void checkConnection() {
        if (!client.isConnected()) {
            synchronized (connectLock) {
                while (!client.isConnected()) {
                    log.debug("[{}:{}] WIOTP MQTT broker connection attempt!", configuration.getHost(), configuration.getPort());
                    try {
                        client.connect(clientOptions, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken iMqttToken) {
                                log.info("[{}:{}] WIOTP MQTT broker connection established!", configuration.getHost(), configuration.getPort());
                            }

                            @Override
                            public void onFailure(IMqttToken iMqttToken, Throwable e) {
                            }
                        }).waitForCompletion();
                        subscribeToTopics();
                    } catch (MqttException e) {
                        log.warn("[{}:{}] WIOTP MQTT broker connection failed!", configuration.getHost(), configuration.getPort(), e);
                        if (!client.isConnected()) {
                            try {
                                Thread.sleep(configuration.getRetryInterval());
                            } catch (InterruptedException e1) {
                                log.trace("Failed to wait for retry interval!", e);
                            }
                        }
                    }
                }
            }

        }
    }

    private void subscribeToTopics() throws MqttException {
        List<IMqttToken> tokens = new ArrayList<>();
        for (MqttTopicMapping mapping : configuration.getMapping()) {
            tokens.add(client.subscribe(mapping.getTopicFilter(), 1, new MqttTelemetryMessageListener(this::onDeviceData, mapping.getConverter())));
        }
        if (configuration.getConnectRequests() != null) {
            for (DeviceStateChangeMapping mapping : configuration.getConnectRequests()) {
                tokens.add(client.subscribe(mapping.getTopicFilter(), 1, new MqttDeviceStateChangeMessageListener(mapping, this::onDeviceConnect)));
            }
        }
        if (configuration.getDisconnectRequests() != null) {
            for (DeviceStateChangeMapping mapping : configuration.getDisconnectRequests()) {
                tokens.add(client.subscribe(mapping.getTopicFilter(), 1, new MqttDeviceStateChangeMessageListener(mapping, this::onDeviceDisconnect)));
            }
        }
        if (configuration.getAttributeRequests() != null) {
            for (AttributeRequestsMapping mapping : configuration.getAttributeRequests()) {
                tokens.add(client.subscribe(mapping.getTopicFilter(), 1, new MqttAttributeRequestsMessageListener(this::onAttributeRequest, mapping)));
            }
        }
        for (IMqttToken token : tokens) {
            token.waitForCompletion();
        }
    }

    private void onDeviceConnect(String deviceName) {
        log.info("[{}] Device connected!", deviceName);
        gateway.onDeviceConnect(deviceName);
    }

    private void onDeviceDisconnect(String deviceName) {
        log.info("[{}] Device disconnected!", deviceName);
        gateway.onDeviceDisconnect(deviceName);
        log.debug("[{}] Will Topic Msg Received. Disconnecting device...", deviceName);
        cleanUpKeepAliveTimes(deviceName);
    }

    private void onDeviceData(List<DeviceData> data) {
        for (DeviceData dd : data) {
            if (devices.add(dd.getName())) {
                gateway.onDeviceConnect(dd.getName());
            }
            if (!dd.getAttributes().isEmpty()) {
                gateway.onDeviceAttributesUpdate(dd.getName(), dd.getAttributes());
            }
            if (!dd.getTelemetry().isEmpty()) {
                gateway.onDeviceTelemetry(dd.getName(), dd.getTelemetry());
            }
            if (dd.getTimeout() != 0) {
                ScheduledFuture<?> future = deviceKeepAliveTimers.get(dd.getName());
                if (future != null) {
                    log.debug("Re-scheduling keep alive timer for device {} with timeout = {}", dd.getName(), dd.getTimeout());
                    future.cancel(true);
                    deviceKeepAliveTimers.remove(dd.getName());
                    scheduleDeviceKeepAliveTimer(dd);
                } else {
                    log.debug("Scheduling keep alive timer for device {} with timeout = {}", dd.getName(), dd.getTimeout());
                    scheduleDeviceKeepAliveTimer(dd);
                }
            }
        }
    }

    private void onAttributeRequest(AttributeRequest attributeRequest) {
        gateway.onDeviceAttributeRequest(attributeRequest, this::onAttributeResponse);
    }

    private void onAttributeResponse(AttributeResponse response) {
        if (response.getData().isPresent()) {
            KvEntry attribute = response.getData().get();
            String topic = replace(response.getTopicExpression(), Integer.toString(response.getRequestId()), response.getDeviceName(), attribute);
            String body = replace(response.getValueExpression(), Integer.toString(response.getRequestId()), response.getDeviceName(), attribute);
            publish(response.getDeviceName(), topic, new MqttMessage(body.getBytes(StandardCharsets.UTF_8)));
        } else {
            log.warn("[{}] {} attribute [{}] not found", response.getDeviceName(), response.isClientScope() ? "Client" : "Shared", response.getKey());
        }
    }

    private void cleanUpKeepAliveTimes(String deviceName) {
        ScheduledFuture<?> future = deviceKeepAliveTimers.get(deviceName);
        if (future != null) {
            future.cancel(true);
            deviceKeepAliveTimers.remove(deviceName);
        }
    }

    private void scheduleDeviceKeepAliveTimer(DeviceData dd) {
        ScheduledFuture<?> f = scheduler.schedule(
                () -> {
                    log.warn("[{}] Device is going to be disconnected because of timeout! timeout = {} milliseconds", dd.getName(), dd.getTimeout());
                    deviceKeepAliveTimers.remove(dd.getName());
                    gateway.onDeviceDisconnect(dd.getName());
                },
                dd.getTimeout(),
                TimeUnit.MILLISECONDS
        );
        deviceKeepAliveTimers.put(dd.getName(), f);
    }

    @Override
    public void onAttributesUpdated(String deviceName, List<KvEntry> attributes) {
        List<AttributeUpdatesMapping> mappings = configuration.getAttributeUpdates().stream()
                .filter(mapping -> deviceName.matches(mapping.getDeviceNameFilter())).collect(Collectors.toList());

        for (AttributeUpdatesMapping mapping : mappings) {
            List<KvEntry> affected = attributes.stream().filter(attribute -> attribute.getKey()
                    .matches(mapping.getAttributeFilter())).collect(Collectors.toList());
            for (KvEntry attribute : affected) {
                String topic = replace(mapping.getTopicExpression(), deviceName, attribute);
                String body = replace(mapping.getValueExpression(), deviceName, attribute);
                MqttMessage msg = new MqttMessage(body.getBytes(StandardCharsets.UTF_8));
                publish(deviceName, topic, msg);
            }
        }
    }

    @Override
    public void onRpcCommand(String deviceName, RpcCommandData command) {
        int requestId = command.getRequestId();

        List<ServerSideRpcMapping> mappings = configuration.getServerSideRpc().stream()
                .filter(mapping -> deviceName.matches(mapping.getDeviceNameFilter()))
                .filter(mapping -> command.getMethod().matches(mapping.getMethodFilter())).collect(Collectors.toList());

        mappings.forEach(mapping -> {
            String requestTopic = replace(mapping.getRequestTopicExpression(), deviceName, command);
            String body = replace(mapping.getValueExpression(), deviceName, command);

            boolean oneway = StringUtils.isEmpty(mapping.getResponseTopicExpression());
            if (oneway) {
                publish(deviceName, requestTopic, new MqttMessage(body.getBytes(StandardCharsets.UTF_8)));
            } else {
                String responseTopic = replace(mapping.getResponseTopicExpression(), deviceName, command);
                try {
                    log.info("[{}] Temporary subscribe to RPC response topic [{}]", deviceName, responseTopic);
                    client.subscribe(responseTopic, 1,
                            new MqttRpcResponseMessageListener(requestId, deviceName, this::onRpcCommandResponse)
                    ).waitForCompletion();
                    scheduler.schedule(() -> {
                        unsubscribe(deviceName, requestId, responseTopic);
                    }, mapping.getResponseTimeout(), TimeUnit.MILLISECONDS);
                    publish(deviceName, requestTopic, new MqttMessage(body.getBytes(StandardCharsets.UTF_8)));
                } catch (MqttException e) {
                    log.warn("[{}] Failed to subscribe to response topic and push RPC command [{}]", deviceName, requestId, e);
                }
            }
        });
    }

    private void onRpcCommandResponse(String topic, RpcCommandResponse rpcResponse) {
        log.info("[{}] Un-subscribe from RPC response topic [{}]", rpcResponse.getDeviceName(), topic);
        gateway.onDeviceRpcResponse(rpcResponse);
        unsubscribe(rpcResponse.getDeviceName(), rpcResponse.getRequestId(), topic);
    }

    private void unsubscribe(String deviceName, int requestId, String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            log.warn("[{}][{}] Failed to unsubscribe from RPC reply topic [{}]", deviceName, requestId, topic, e);
        }
    }

    private void publish(final String deviceName, String topic, MqttMessage msg) {
        try {
            client.publish(topic, msg, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    log.info("[{}] Successfully published to topic [{}]", deviceName, topic);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable e) {
                    log.warn("[{}] Failed to publish to topic [{}]", deviceName, topic, e);
                }
            });
        } catch (MqttException e) {
            log.warn("[{}] Failed to publish to topic [{}] ", deviceName, topic, e);
        }
    }

    private static String replace(String expression, String deviceName, KvEntry attribute) {
        return replace(expression, "", deviceName, attribute);
    }

    private static String replace(String expression, String deviceName, RpcCommandData command) {
        return expression.replace("${deviceName}", deviceName)
                .replace("${methodName}", command.getMethod())
                .replace("${requestId}", Integer.toString(command.getRequestId()))
                .replace("${params}", command.getParams());
    }

    private static String replace(String expression, String requestId, String deviceName, KvEntry attribute) {
        return expression.replace("${deviceName}", deviceName)
                .replace("${requestId}", requestId)
                .replace("${attributeKey}", attribute.getKey())
                .replace("${attributeValue}", attribute.getValueAsString());
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("[{}:{}] MQTT broker connection lost!", configuration.getHost(), configuration.getPort());
        devices.forEach(gateway::onDeviceDisconnect);
        checkConnection();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
