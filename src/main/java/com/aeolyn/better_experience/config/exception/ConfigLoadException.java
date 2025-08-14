package com.aeolyn.better_experience.config.exception;

/**
 * 配置加载异常
 */
public class ConfigLoadException extends ConfigException {
    
    public ConfigLoadException(String message) {
        super(message);
    }
    
    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
