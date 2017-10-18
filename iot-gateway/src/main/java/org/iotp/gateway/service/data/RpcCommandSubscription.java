package org.iotp.gateway.service.data;

import org.iotp.gateway.service.RpcCommandListener;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 */
@Data
@AllArgsConstructor
public class RpcCommandSubscription {

    private String deviceNameFilter;
    private RpcCommandListener listener;

    public boolean matches(String deviceName) {
        return deviceName.matches(deviceNameFilter);
    }

}
