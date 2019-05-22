package com.veelur.sync.common.exception;

/**
 * @author: veelur
 * @date: 18-11-20
 * @Description: {相关描述}
 */
public class SystemExecErrorException extends RuntimeException {

    public SystemExecErrorException() {
    }

    public SystemExecErrorException(String message) {
        super(message);
    }

    public SystemExecErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemExecErrorException(Throwable cause) {
        super(cause);
    }
}
