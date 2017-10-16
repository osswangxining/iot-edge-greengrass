package org.iotp.gateway.service.data;

import java.util.List;

import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.TsKvEntry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DeviceData {

  private final String name;
  private final List<KvEntry> attributes;
  private final List<TsKvEntry> telemetry;
  private int timeout;
}
