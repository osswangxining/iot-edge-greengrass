package org.iotp.gateway.extensions.common.conf.mapping;

import lombok.Data;

/**
 * Created by ashvayka on 16.01.17.
 */
@Data
public class KVMapping {
    private String key;
    private DataTypeMapping type;
    private String value;
    private String ts;
    private String tsFormat;
}
