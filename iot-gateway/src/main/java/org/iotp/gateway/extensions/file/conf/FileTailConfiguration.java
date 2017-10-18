package org.iotp.gateway.extensions.file.conf;

import lombok.Data;

import java.util.List;

/**
 */
@Data
public class FileTailConfiguration {

    List<FileMonitorConfiguration> fileMonitorConfigurations;
}
