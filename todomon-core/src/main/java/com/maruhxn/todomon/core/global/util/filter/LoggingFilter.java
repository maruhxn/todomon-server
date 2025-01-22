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
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 요청 정보 수집
            String requestPath = wrappedRequest.getRequestURI();
            String method = wrappedRequest.getMethod();
            String queryParams = wrappedRequest.getQueryString();
            String requestBody = wrappedRequest.getReader().lines().reduce("", String::concat);
            String clientIp = wrappedRequest.getRemoteAddr();

            log.debug("Incoming Request: Path={}, Method={}, QueryParams={}, ClientIP={}, Body={}",
                    requestPath, method, queryParams, clientIp, requestBody);

            // 필터 체인 실행
            chain.doFilter(wrappedRequest, response);

            // 응답 정보 수집
            int status = httpResponse.getStatus();
            log.debug("Response: Path={}, Status={}", requestPath, status);
        } else {
            chain.doFilter(request, response);
        }
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
