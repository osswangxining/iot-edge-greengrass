package org.iotp.gateway.extensions.mqtt.client.conf.mapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotp.gateway.conf.mapping.AttributesMapping;
import org.iotp.gateway.conf.mapping.KVMapping;
import org.iotp.gateway.conf.mapping.TimeseriesMapping;
import org.iotp.gateway.service.data.DeviceData;
import org.iotp.infomgt.data.kv.BasicTsKvEntry;
import org.iotp.infomgt.data.kv.BooleanDataEntry;
import org.iotp.infomgt.data.kv.DoubleDataEntry;
import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.LongDataEntry;
import org.iotp.infomgt.data.kv.StringDataEntry;
import org.iotp.infomgt.data.kv.TsKvEntry;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class MqttJsonConverter extends AbstractJsonConverter implements MqttDataConverter {

  private String filterExpression;
  private String deviceNameJsonExpression;
  private String deviceNameTopicExpression;
  private Pattern deviceNameTopicPattern;
  private int timeout;
  private final List<AttributesMapping> attributes;
  private final List<TimeseriesMapping> timeseries;

  @Override
  public List<DeviceData> convert(String topic, MqttMessage msg) throws Exception {
    String data = new String(msg.getPayload(), StandardCharsets.UTF_8);
    log.trace("Parsing json message: {}", data);

    if (!filterExpression.isEmpty()) {
      try {
        log.debug("Data before filtering {}", data);
        DocumentContext document = JsonPath.parse(data);
        document = JsonPath.parse((Object) document.read(filterExpression));
        data = document.jsonString();
        log.debug("Data after filtering {}", data);
      } catch (RuntimeException e) {
        log.debug("Failed to apply filter expression: {}", filterExpression);
        throw new RuntimeException("Failed to apply filter expression " + filterExpression);
      }
    }

    JsonNode node = mapper.readTree(data);
    List<String> srcList;
    if (node.isArray()) {
      srcList = new ArrayList<>(node.size());
      for (int i = 0; i < node.size(); i++) {
        srcList.add(mapper.writeValueAsString(node.get(i)));
      }
    } else {
      srcList = Collections.singletonList(data);
    }

    return parse(topic, srcList);
  }

  private List<DeviceData> parse(String topic, List<String> srcList) {
    List<DeviceData> result = new ArrayList<>(srcList.size());
    for (String src : srcList) {
      DocumentContext document = JsonPath.parse(src);
      long ts = System.currentTimeMillis();
      String deviceName;
      if (!StringUtils.isEmpty(deviceNameTopicExpression)) {
        deviceName = eval(topic);
      } else {
        deviceName = eval(document, deviceNameJsonExpression);
      }
      if (!StringUtils.isEmpty(deviceName)) {
        List<KvEntry> attrData = getKvEntries(document, attributes);
        List<TsKvEntry> tsData = getKvEntries(document, timeseries).stream().map(kv -> new BasicTsKvEntry(ts, kv))
            .collect(Collectors.toList());
        result.add(new DeviceData(deviceName, attrData, tsData, timeout));
      }
    }
    return result;
  }

  private List<KvEntry> getKvEntries(DocumentContext document, List<? extends KVMapping> mappings) {
    List<KvEntry> result = new ArrayList<>();
    if (mappings != null) {
      for (KVMapping mapping : mappings) {
        String key = eval(document, mapping.getKey());
        String strVal = eval(document, mapping.getValue());
        switch (mapping.getType().getDataType()) {
        case STRING:
          result.add(new StringDataEntry(key, strVal));
          break;
        case BOOLEAN:
          result.add(new BooleanDataEntry(key, Boolean.valueOf(strVal)));
          break;
        case DOUBLE:
          result.add(new DoubleDataEntry(key, Double.valueOf(strVal)));
          break;
        case LONG:
          result.add(new LongDataEntry(key, Long.valueOf(strVal)));
          break;
        }
      }
    }
    return result;
  }

  private String eval(String topic) {
    if (deviceNameTopicPattern == null) {
      deviceNameTopicPattern = Pattern.compile(deviceNameTopicExpression);
    }
    Matcher matcher = deviceNameTopicPattern.matcher(topic);
    while (matcher.find()) {
      return matcher.group();
    }
    return null;
  }
}
