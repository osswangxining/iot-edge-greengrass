package org.iotp.gateway.service.data;

import java.util.function.Consumer;

import lombok.Data;

/**
 */
@Data
public class AttributeRequestListener {
    private final AttributeRequest request;
    private final Consumer<AttributeResponse> listener;
}
