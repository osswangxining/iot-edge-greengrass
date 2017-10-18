package org.iotp.gateway.extensions.http.conf;

import java.util.List;

import org.iotp.gateway.extensions.http.conf.mapping.HttpDeviceDataConverter;

import lombok.Data;

@Data
public class HttpConverterConfiguration {

    private String converterId;
    private String deviceTypeId;
    private String token;
    private List<HttpDeviceDataConverter> converters;
}
