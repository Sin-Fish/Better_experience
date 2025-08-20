package com.aeolyn.better_experience.common.util.RustErrTool;

/**
 * 未找到错误类
 */
public final class NotFoundError extends AppError {
    public NotFoundError(String message) {
        super(message);
    }
}