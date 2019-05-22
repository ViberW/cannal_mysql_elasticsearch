package com.veelur.sync.common.exception;

/**
 * @author: veelur
 * @date: 18-11-1
 * @Description: {相关描述}
 */
public class LogicErrorException extends RuntimeException {
    public LogicErrorException() {
    }

    public LogicErrorException(String message) {
        super(message);
    }
}
