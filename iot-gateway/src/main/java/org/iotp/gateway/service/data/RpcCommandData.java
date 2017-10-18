package org.iotp.gateway.service.data;

import lombok.Data;

/**
 */
@Data
public class RpcCommandData {

    private int requestId;
    private String method;
    private String params;

}
