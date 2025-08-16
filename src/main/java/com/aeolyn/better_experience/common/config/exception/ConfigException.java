package com.aeolyn.better_experience.common.config.exception;

/**
 * 配置异常基类
 */
public abstract class ConfigException extends Exception {
    
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
