package com.dadaabc.sync.elasticsearch.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: veelur
 * @date: 18-10-8
 * @Description: {相关描述}
 */
@Component
public class RequestURIFilter extends BaseFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestURIFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURI = httpServletRequest.getRequestURI();
        if ("/".equals(requestURI) || "/checkHealth".equals(requestURI)) {
            //心跳检测,直接返回
            this.responseEnd(httpServletResponse, "ok");
            return;
        }
        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {
    }
}
