package org.iotp.gateway.conf.mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

/**
 */
@Data
public class DeviceMapping {

  public static final Pattern TAG_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");
  private final String deviceNodePattern;
  private final String deviceNamePattern;
  private final List<AttributesMapping> attributes;
  private final List<TimeseriesMapping> timeseries;

  public Set<String> getDeviceNameTags() {
    Set<String> tags = new HashSet<>();
    addTags(tags, TAG_PATTERN, deviceNamePattern);
    return tags;
  }

  public Set<String> getAllTags() {
    Set<String> tags = new HashSet<>();
    addTags(tags, TAG_PATTERN, deviceNamePattern);
    attributes.forEach(mapping -> addTags(tags, TAG_PATTERN, mapping.getValue()));
    timeseries.forEach(mapping -> addTags(tags, TAG_PATTERN, mapping.getValue()));
    return tags;
  }

  private void addTags(Set<String> tags, Pattern pattern, String expression) {
    Matcher matcher = pattern.matcher(expression);
    while (matcher.find()) {
      String tag = matcher.group();
      tags.add(tag.substring(2, tag.length() - 1));
    }
  }
}
