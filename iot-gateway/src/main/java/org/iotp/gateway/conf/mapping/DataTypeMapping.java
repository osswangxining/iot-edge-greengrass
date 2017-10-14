package org.iotp.gateway.conf.mapping;

import org.iotp.infomgt.data.kv.DataType;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
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
