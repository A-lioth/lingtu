package com.alioth.lingtu.constant;

/**
 * 缓存 key
 *
 * @author Alioth
 */
public interface CacheKeyConstant {

    //用户 token
    String TOKEN_KEY_PREFIX = "lingtu:auth:token:";
    String USER_TOKENS_KEY_PREFIX = "lingtu:auth:user-tokens:";

    //邮件验证码
    String EMAIL_CODE_KEY_PREFIX = "lingtu:auth:email-code:";
    String EMAIL_THROTTLE_KEY_PREFIX = "lingtu:auth:email-throttle:";
}
