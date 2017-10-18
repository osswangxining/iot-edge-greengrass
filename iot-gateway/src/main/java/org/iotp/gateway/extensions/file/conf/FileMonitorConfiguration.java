package org.iotp.gateway.extensions.file.conf;

import lombok.Data;

import java.util.List;

/**
 */
@Data
public class FileMonitorConfiguration {

    private String file;
    private int skipLines;
    private int updateInterval;
    private String[] csvColumns;
    private CsvDeviceDataConverter converter;
}
