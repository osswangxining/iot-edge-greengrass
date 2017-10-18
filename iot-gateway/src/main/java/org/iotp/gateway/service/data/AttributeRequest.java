package org.iotp.gateway.service.data;

import lombok.Builder;
import lombok.Data;

/**
 */
@Data
@Builder
public class AttributeRequest {

    private final int requestId;
    private final String deviceName;
    private final String attributeKey;

    private final boolean clientScope;
    private final String topicExpression;
    private final String valueExpression;
}
