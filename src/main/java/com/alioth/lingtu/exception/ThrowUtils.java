package com.alioth.lingtu.exception;

/**
 * 异常工具类
 *
 * @author alioth
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param status    错误码
     */
    public static void throwIf(boolean condition, ResponseStatus status) {
        throwIf(condition, new BusinessException(status));
    }
}
