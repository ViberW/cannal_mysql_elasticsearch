package com.dadaabc.sync.filter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: veelur
 * @date: 18-10-8
 * @Description: {相关描述}
 */
public abstract class BaseFilter implements Filter {

    protected void responseEnd(HttpServletResponse response, String msg) throws IOException {
        this.responseEnd(response, 200, msg);
    }

    protected void responseEnd(HttpServletResponse response, Integer httpStatus, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(httpStatus);
        response.getWriter().append(msg).flush();
    }
}
