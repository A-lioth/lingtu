package com.alioth.lingtu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 邮件服务配置
 *
 * @author Alioth
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {
    /**
     * 邮件服务 IP 地址
     */
    private String host;
    /**
     * 邮件服务端口
     */
    private Integer port;
    /**
     * 邮件服务用户名
     */
    private String username;
    /**
     * 邮件服务密码
     */
    private String password;
    /**
     * 邮件服务配置
     */
    private Properties properties = new Properties();
}
