package org.iotp.gateway.service.data;

import java.util.Optional;

import org.iotp.infomgt.data.kv.KvEntry;

import lombok.Builder;
import lombok.Data;

/**
 */
@Data
@Builder
public class AttributeResponse {

  private final int requestId;
  private final String deviceName;
  private final String key;
  private final boolean clientScope;
  private final Optional<KvEntry> data;

  private final String topicExpression;
  private final String valueExpression;
}
