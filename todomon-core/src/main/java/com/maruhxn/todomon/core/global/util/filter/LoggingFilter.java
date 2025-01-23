package com.maruhxn.todomon.core.global.util.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (this.checkIsMultipartRequest(httpServletRequest)) {
                this.logMultipartRequest(httpServletRequest);
                chain.doFilter(request, response); // 원본 요청 그대로 전달
            } else {
                CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);
                this.logRegularRequest(wrappedRequest);
                chain.doFilter(wrappedRequest, response);
            }

            // 응답 로그 기록
            this.logResponse(httpResponse);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void logResponse(HttpServletResponse httpResponse) {
        int status = httpResponse.getStatus();
        log.debug("Response: Status={}", status);
    }

    private boolean checkIsMultipartRequest(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getContentType() != null
                && httpServletRequest.getContentType().startsWith("multipart/form-data");
    }

    private void logRegularRequest(CachedBodyHttpServletRequest request) throws IOException {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String queryParams = request.getQueryString();
        String requestBody = request.getReader().lines().reduce("", String::concat);
        String clientIp = request.getRemoteAddr();

        log.debug("Incoming Request: Path={}, Method={}, QueryParams={}, ClientIP={}, Body={}",
                requestPath, method, queryParams, clientIp, requestBody);
    }

    private void logMultipartRequest(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String queryParams = request.getQueryString();
        String clientIp = request.getRemoteAddr();

        log.debug("Incoming Multipart Request: Path={}, Method={}, QueryParams={}, ClientIP={}",
                requestPath, method, queryParams, clientIp);
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // No implementation needed
                }

                @Override
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
    }
}
