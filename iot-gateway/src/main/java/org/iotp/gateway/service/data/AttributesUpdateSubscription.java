package org.iotp.gateway.service.data;

import org.iotp.gateway.service.AttributesUpdateListener;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 */
@Data
@AllArgsConstructor
public class AttributesUpdateSubscription {

  private String deviceNameFilter;
  private AttributesUpdateListener listener;

  public boolean matches(String deviceName) {
    return deviceName.matches(deviceNameFilter);
  }
}
