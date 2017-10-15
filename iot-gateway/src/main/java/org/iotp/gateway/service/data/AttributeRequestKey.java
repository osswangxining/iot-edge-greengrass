package org.iotp.gateway.service.data;

import lombok.Data;

/**
 */
@Data
public class AttributeRequestKey {
  private final int requestId;
  private final String deviceName;
}
