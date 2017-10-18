package org.iotp.gateway.extensions.http;

public interface HttpService {

    void processRequest(String converterId, String token, String body) throws Exception;
}
