package org.iotp.gateway.extensions.opc.conf;

import lombok.Data;
import org.iotp.gateway.extensions.opc.conf.identity.IdentityProviderConfiguration;
import org.iotp.gateway.util.KeystoreConfiguration;
import org.iotp.gateway.extensions.opc.conf.mapping.DeviceMapping;

import java.util.List;

/**
 */
@Data
public class OpcUaServerConfiguration {

    private String applicationName;
    private String applicationUri;
    private String host;
    private int port;
    private int scanPeriodInSeconds;
    private int timeoutInMillis;
    private String security;
    private IdentityProviderConfiguration identity;
    private KeystoreConfiguration keystore;
    private List<DeviceMapping> mapping;

}
