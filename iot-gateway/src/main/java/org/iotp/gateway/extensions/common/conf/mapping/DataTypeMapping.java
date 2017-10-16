package org.iotp.gateway.extensions.common.conf.mapping;

import org.iotp.infomgt.data.kv.DataType;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * Created by ashvayka on 17.01.17.
 */
@Data
@AllArgsConstructor
public class DataTypeMapping {

    private DataType dataType;

    @JsonCreator
    public static DataTypeMapping forValue(String value) {
        return new DataTypeMapping(DataType.valueOf(value.toUpperCase()));
    }

}
