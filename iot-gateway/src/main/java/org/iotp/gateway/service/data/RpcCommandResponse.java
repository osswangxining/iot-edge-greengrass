package org.iotp.gateway.service.data;

import lombok.Data;

/**
 */
@Data
public class RpcCommandResponse {

  private int requestId;
  private String deviceName;
  private String data;

}
