package com.aeolyn.better_experience.common.util.RustErrTool;

/**
 * 数据库错误类
 */
public final class DatabaseError extends AppError {
    public DatabaseError(String message) {
        super(message);
    }
}