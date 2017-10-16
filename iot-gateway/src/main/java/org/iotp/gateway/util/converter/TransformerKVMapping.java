package org.iotp.gateway.util.converter;

import lombok.Data;
import org.iotp.gateway.extensions.common.conf.mapping.KVMapping;
import org.iotp.gateway.util.converter.transformer.DataValueTransformer;

@Data
public class TransformerKVMapping extends KVMapping {
    private DataValueTransformer transformer;
}
