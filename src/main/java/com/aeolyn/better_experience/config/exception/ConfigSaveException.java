package com.aeolyn.better_experience.config.exception;

/**
 * 配置保存异常
 */
public class ConfigSaveException extends ConfigException {
    
    public ConfigSaveException(String message) {
        super(message);
    }
    
    public ConfigSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
