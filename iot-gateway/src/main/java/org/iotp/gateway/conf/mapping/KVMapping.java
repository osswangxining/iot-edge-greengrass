package org.iotp.gateway.conf.mapping;

import lombok.Data;

/**
 */
@Data
public class KVMapping {
  private String key;
  private DataTypeMapping type;
  private String value;

}
