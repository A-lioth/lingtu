package com.alioth.lingtu.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author alioth
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(ResponseStatus status) {
        super(status.getMessage());
        this.code = status.getCode();
    }

    public BusinessException(ResponseStatus status, String msg) {
        super(msg);
        this.code = status.getCode();
    }
}
