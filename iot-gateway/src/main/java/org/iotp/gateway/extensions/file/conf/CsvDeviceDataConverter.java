package org.iotp.gateway.extensions.file.conf;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.gateway.util.converter.BasicJsonConverter;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class CsvDeviceDataConverter extends BasicJsonConverter {

}
