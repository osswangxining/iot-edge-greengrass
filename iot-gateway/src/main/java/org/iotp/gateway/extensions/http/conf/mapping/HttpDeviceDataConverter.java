package org.iotp.gateway.extensions.http.conf.mapping;

import java.util.List;
import java.util.regex.Pattern;

import org.iotp.gateway.service.data.DeviceData;
import org.iotp.gateway.util.converter.BasicJsonConverter;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class HttpDeviceDataConverter extends BasicJsonConverter {

    public static final Pattern TAG_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    public boolean isApplicable(String body) {
        if (filterExpression == null || filterExpression.isEmpty()) {
            return true;
        } else {
            try {
                List jsonArray = JsonPath.parse(body).read(filterExpression);
                return !jsonArray.isEmpty();
            } catch (RuntimeException e) {
                log.debug("Failed to apply filter expression: {}", filterExpression, e);
                throw new RuntimeException("Failed to apply filter expression " + filterExpression, e);
            }
        }
    }

    public DeviceData parseBody(String body) {
        try {
            if (filterExpression != null && !filterExpression.isEmpty()) {
                DocumentContext document = JsonPath.parse(body);
                try {
                    log.debug("Data before filtering {}", body);
                    List jsonArray = document.read(filterExpression);
                    Object jsonObj = jsonArray.get(0); // take 1st element from filtered array (jayway jsonpath library limitation)
                    document = JsonPath.parse(jsonObj);
                    body = document.jsonString();
                    log.debug("Data after filtering {}", body);
                } catch (RuntimeException e) {
                    log.debug("Failed to apply filter expression: {}", filterExpression, e);
                    throw new RuntimeException("Failed to apply filter expression " + filterExpression, e);
                }
            }

            return parseDeviceData(JsonPath.parse(body));
        } catch (Exception e) {
            log.error("Exception occurred while parsing json request body [{}]", body, e);
            throw new RuntimeException("Exception occurred while parsing json request body [" + body + "]", e);
        }
    }
}
