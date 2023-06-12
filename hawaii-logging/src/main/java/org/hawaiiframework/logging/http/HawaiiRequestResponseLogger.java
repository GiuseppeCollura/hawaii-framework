package org.hawaiiframework.logging.http;

import org.hawaiiframework.logging.web.filter.ContentCachingWrappedResponse;
import org.hawaiiframework.logging.web.filter.ResettableHttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * Responsible for logging Http requests and responses.
 */
public interface HawaiiRequestResponseLogger {

    /**
     * Log the request.
     *
     * @param request The request.
     * @param body    The body.
     */
    void logRequest(HttpRequest request, byte[] body);

    /**
     * Log the request.
     *
     * @param wrappedRequest The request.
     * @throws IOException in case of an error.
     */
    void logRequest(ResettableHttpServletRequest wrappedRequest) throws IOException;

    /**
     * Log the response.
     *
     * @param response The response to log.
     * @throws IOException in case of an error.
     */
    void logResponse(ClientHttpResponse response) throws IOException;

    /**
     * Log the response.
     *
     * @param servletRequest  The request.
     * @param wrappedResponse The response.
     * @throws IOException in case of an error.
     */
    void logResponse(HttpServletRequest servletRequest, ContentCachingWrappedResponse wrappedResponse) throws IOException;

}
