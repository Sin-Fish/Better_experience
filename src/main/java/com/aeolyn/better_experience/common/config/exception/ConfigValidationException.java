package com.aeolyn.better_experience.common.config.exception;

/**
 * 配置验证异常
 */
public class ConfigValidationException extends ConfigException {
    
    public ConfigValidationException(String message) {
        super(message);
    }
    
    public ConfigValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
