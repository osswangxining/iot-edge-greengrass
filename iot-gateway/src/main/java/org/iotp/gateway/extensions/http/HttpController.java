package org.iotp.gateway.extensions.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.iotp.gateway.extensions.http.conf.HttpRequestProcessingError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@ConditionalOnProperty(prefix = "http", value = "enabled", havingValue = "true")
@Slf4j
public class HttpController {
    private static final String TOKEN_HEADER = "Authorization";

    @Autowired
    private HttpService service;

    private ObjectMapper mapper = new ObjectMapper();


    @RequestMapping(value = "/sigfox/{deviceTypeId}", method = RequestMethod.POST)
    public void handleSigfoxRequest(@PathVariable String deviceTypeId,
                              @RequestHeader(TOKEN_HEADER) String token,
                              @RequestBody String body) throws Exception {
        service.processRequest(deviceTypeId, token, body);
    }

    @RequestMapping(value = "/uplink/{converterId}", method = RequestMethod.POST)
    public void handleRequest(@PathVariable String converterId,
                              @RequestHeader(TOKEN_HEADER) String token,
                              @RequestBody String body) throws Exception {
        service.processRequest(converterId, token, body);
    }

    @ExceptionHandler(Exception.class)
    public void handleThingsboardException(Exception exception, HttpServletResponse response) {
        log.debug("Processing exception {}", exception.getMessage(), exception);
        if (!response.isCommitted()) {
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                if (exception instanceof SecurityException) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    mapper.writeValue(response.getWriter(),
                            new HttpRequestProcessingError("You don't have permission to perform this operation!"));
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    mapper.writeValue(response.getWriter(), new HttpRequestProcessingError(exception.getMessage()));
                }
            } catch (IOException e) {
                log.error("Can't handle exception", e);
            }
        }
    }
}
