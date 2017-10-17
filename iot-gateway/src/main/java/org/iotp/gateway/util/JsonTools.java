package org.iotp.gateway.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iotp.infomgt.data.kv.BooleanDataEntry;
import org.iotp.infomgt.data.kv.DoubleDataEntry;
import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.LongDataEntry;
import org.iotp.infomgt.data.kv.StringDataEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 */
public class JsonTools {

  private static final ObjectMapper JSON = new ObjectMapper();

  public static ObjectNode newNode() {
    return JSON.createObjectNode();
  }

  public static byte[] toBytes(ObjectNode node) {
    return toString(node).getBytes(StandardCharsets.UTF_8);
  }

  public static JsonNode fromString(String data) {
    try {
      return JSON.readTree(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toString(JsonNode node) {
    try {
      return JSON.writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static void putToNode(ObjectNode node, KvEntry kv) {
    switch (kv.getDataType()) {
    case BOOLEAN:
      node.put(kv.getKey(), kv.getBooleanValue().get());
      break;
    case STRING:
      node.put(kv.getKey(), kv.getStrValue().get());
      break;
    case LONG:
      node.put(kv.getKey(), kv.getLongValue().get());
      break;
    case DOUBLE:
      node.put(kv.getKey(), kv.getDoubleValue().get());
      break;
    }
  }

  public static List<KvEntry> getKvEntries(JsonNode data) {
    List<KvEntry> attributes = new ArrayList<>();
    for (Iterator<Map.Entry<String, JsonNode>> it = data.fields(); it.hasNext();) {
      Map.Entry<String, JsonNode> field = it.next();
      String key = field.getKey();
      JsonNode value = field.getValue();
      if (value.isBoolean()) {
        attributes.add(new BooleanDataEntry(key, value.asBoolean()));
      } else if (value.isLong()) {
        attributes.add(new LongDataEntry(key, value.asLong()));
      } else if (value.isDouble()) {
        attributes.add(new DoubleDataEntry(key, value.asDouble()));
      } else {
        attributes.add(new StringDataEntry(key, value.asText()));
      }
    }
    return attributes;
  }
}
