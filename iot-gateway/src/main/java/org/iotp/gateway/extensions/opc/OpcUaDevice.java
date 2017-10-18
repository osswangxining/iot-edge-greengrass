package org.iotp.gateway.extensions.opc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.iotp.gateway.extensions.common.conf.mapping.KVMapping;
import org.iotp.gateway.extensions.opc.conf.mapping.AttributesMapping;
import org.iotp.gateway.extensions.opc.conf.mapping.DeviceMapping;
import org.iotp.gateway.extensions.opc.conf.mapping.TimeseriesMapping;
import org.iotp.infomgt.data.kv.BasicTsKvEntry;
import org.iotp.infomgt.data.kv.BooleanDataEntry;
import org.iotp.infomgt.data.kv.DoubleDataEntry;
import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.LongDataEntry;
import org.iotp.infomgt.data.kv.StringDataEntry;
import org.iotp.infomgt.data.kv.TsKvEntry;

import lombok.Data;

/**
 */
@Data
public class OpcUaDevice {

    private final NodeId nodeId;
    private final DeviceMapping mapping;
    private final Map<String, NodeId> tagKeysMap = new HashMap<>();
    private final Map<NodeId, String> tagIdsMap = new HashMap<>();
    private final Map<String, String> tagValues = new HashMap<>();
    private final Map<NodeId, List<AttributesMapping>> attributesMap = new HashMap<>();
    private final Map<NodeId, List<TimeseriesMapping>> timeseriesMap = new HashMap<>();

    private String deviceName;
    private long scanTs;

    public Map<String, NodeId> registerTags(Map<String, NodeId> newTagMap) {
        Map<String, NodeId> newTags = new HashMap<>();
        for (Map.Entry<String, NodeId> kv : newTagMap.entrySet()) {
            NodeId old = registerTag(kv);
            if (old == null) {
                newTags.put(kv.getKey(), kv.getValue());
            }
        }
        return newTags;
    }

    private NodeId registerTag(Map.Entry<String, NodeId> kv) {
        String tag = kv.getKey();
        NodeId tagId = kv.getValue();
        mapping.getAttributes().stream()
                .filter(attr -> attr.getValue().contains(escape(tag)))
                .forEach(attr -> attributesMap.computeIfAbsent(tagId, key -> new ArrayList<>()).add(attr));
        mapping.getTimeseries().stream()
                .filter(attr -> attr.getValue().contains(escape(tag)))
                .forEach(attr -> timeseriesMap.computeIfAbsent(tagId, key -> new ArrayList<>()).add(attr));
        tagIdsMap.putIfAbsent(kv.getValue(), kv.getKey());
        return tagKeysMap.put(kv.getKey(), kv.getValue());
    }

    public void calculateDeviceName(Map<String, String> deviceNameTagValues) {
        String deviceNameTmp = mapping.getDeviceNamePattern();
        for (Map.Entry<String, String> kv : deviceNameTagValues.entrySet()) {
            deviceNameTmp = deviceNameTmp.replace(escape(kv.getKey()), kv.getValue());
        }
        this.deviceName = deviceNameTmp;
    }

    public void updateTag(NodeId tagId, DataValue dataValue) {
        String tag = tagIdsMap.get(tagId);
        tagValues.put(tag, dataValue.getValue().getValue().toString());
    }

    public void updateScanTs() {
        scanTs = System.currentTimeMillis();
    }

    private List<AttributesMapping> getAttributesMapping(NodeId tag) {
        return attributesMap.getOrDefault(tag, Collections.emptyList());
    }

    private List<TimeseriesMapping> getTimeseriesMapping(NodeId tag) {
        return timeseriesMap.getOrDefault(tag, Collections.emptyList());
    }

    private String escape(String tag) {
        return "${" + tag + "}";
    }

    public List<KvEntry> getAffectedAttributes(NodeId tagId, DataValue dataValue) {
        List<AttributesMapping> attributes = getAttributesMapping(tagId);
        if (attributes.size() > 0) {
            return getKvEntries(attributes);
        } else {
            return Collections.emptyList();
        }
    }

    public List<TsKvEntry> getAffectedTimeseries(NodeId tagId, DataValue dataValue) {
        List<AttributesMapping> attributes = getAttributesMapping(tagId);
        if (attributes.size() > 0) {
            return getKvEntries(attributes).stream()
                    .map(kv -> new BasicTsKvEntry(dataValue.getSourceTime().getJavaTime(), kv))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<KvEntry> getKvEntries(List<? extends KVMapping> mappings) {
        List<KvEntry> result = new ArrayList<>();
        for (KVMapping mapping : mappings) {
            String strVal = mapping.getValue();
            for (Map.Entry<String, String> tagKV : tagValues.entrySet()) {
                strVal = strVal.replace(escape(tagKV.getKey()), tagKV.getValue());
            }
            switch (mapping.getType().getDataType()) {
                case STRING:
                    result.add(new StringDataEntry(mapping.getKey(), strVal));
                    break;
                case BOOLEAN:
                    result.add(new BooleanDataEntry(mapping.getKey(), Boolean.valueOf(strVal)));
                    break;
                case DOUBLE:
                    result.add(new DoubleDataEntry(mapping.getKey(), Double.valueOf(strVal)));
                    break;
                case LONG:
                    result.add(new LongDataEntry(mapping.getKey(), Long.valueOf(strVal)));
                    break;
            }
        }
        return result;
    }
}
