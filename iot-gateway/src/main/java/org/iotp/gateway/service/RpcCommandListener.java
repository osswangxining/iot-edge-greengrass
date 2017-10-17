package org.iotp.gateway.service;

import org.iotp.gateway.service.data.RpcCommandData;

/**
 */
public interface RpcCommandListener {

  void onRpcCommand(String deviceName, RpcCommandData command);

}
