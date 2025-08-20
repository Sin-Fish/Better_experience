package com.aeolyn.better_experience.common.util.RustErrTool;

/**
 * 验证错误类
 */
public final class ValidationError extends AppError {
    private final String field;
    
    public ValidationError(String message, String field) {
        super(message);
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
}