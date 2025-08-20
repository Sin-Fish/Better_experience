package com.aeolyn.better_experience.common.util.RustErrTool;


/**
 * 应用程序错误基类（密封类，限制可用的错误类型）
 */
public abstract sealed class AppError permits DatabaseError, ValidationError, NotFoundError, NetworkError {
    private final String message;
    
    protected AppError(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + message;
    }
}