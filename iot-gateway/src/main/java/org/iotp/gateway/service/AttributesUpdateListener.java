package org.iotp.gateway.service;

import java.util.List;

import org.iotp.infomgt.data.kv.KvEntry;

/**
 */
public interface AttributesUpdateListener {

  void onAttributesUpdated(String deviceName, List<KvEntry> attributes);

}
