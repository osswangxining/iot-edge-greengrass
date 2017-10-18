package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import lombok.Data;
import lombok.ToString;

/**
 */
@Data
@ToString
public class DeviceStateChangeMapping {
    private String topicFilter;
    private String deviceNameJsonExpression;
    private String deviceNameTopicExpression;

    private String deviceTypeJsonExpression;
    private String deviceTypeTopicExpression;

}
