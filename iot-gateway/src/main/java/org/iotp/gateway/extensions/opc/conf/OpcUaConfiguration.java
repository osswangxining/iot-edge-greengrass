package org.iotp.gateway.extensions.opc.conf;

import java.util.List;

import lombok.Data;

/**
 */
@Data
public class OpcUaConfiguration {

    List<OpcUaServerConfiguration> servers;
}
