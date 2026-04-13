package com.alioth.lingtu.common;

import com.alioth.lingtu.exception.ResponseStatus;

/**
 * 响应结果类
 *
 * @author alioth
 */
public class ResultUtils {
    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<T>(0, true, "success", data);
    }

    public static BaseResult<?> error(ResponseStatus status) {
        return new BaseResult<>(status);
    }

    public static <T> BaseResult<T> error(int code, String msg) {
        return new BaseResult<>(code, false, msg, null);
    }
}
