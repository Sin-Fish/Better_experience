// File: RustStyle.java
package com.aeolyn.better_experience.common.util.RustErrTool;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 提供类似Rust问号操作符(?功能的方法
 * 
 * 注意：这个类使用异常来实现控制流，适用于简化错误处理代码，
 * 但在高性能场景中可能需要考虑异常创建的开销。
 */
public final class RustStyle {
    private RustStyle() {} 
    
    /**
     * 用于提前返回的异常（非泛型版本）
     */
    public static class EarlyReturnException extends RuntimeException {
        private final Object errorValue;
        
        public EarlyReturnException(Object errorValue) {
            super("Early return with error: " + errorValue);
            this.errorValue = errorValue;
        }
        
        @SuppressWarnings("unchecked")
        public <E> E getErrorValue(Class<E> errorType) {
            if (errorType.isInstance(errorValue)) {
                return (E) errorValue;
            }
            throw new ClassCastException("Expected error type: " + errorType.getName() + 
                                       ", but got: " + errorValue.getClass().getName());
        }
        
        public Object getErrorValue() {
            return errorValue;
        }
    }
    
    /**
     * 如果结果是Err则提前返回
     * 使用类型安全的辅助方法处理返回
     */
    public static <T, E> T unwrapOrReturn(Result<T, E> result) {
        if (result.isErr()) {
            throw new EarlyReturnException(result.unwrapErr());
        }
        return result.unwrap();
    }
    
    /**
     * 处理可能抛出EarlyReturnException的代码块，将其转换为Result
     * 需要显式指定错误类型
     */
    public static <T, E> Result<T, E> catchEarlyReturn(
        Supplier<T> supplier, 
        Class<E> errorType
    ) {
        try {
            T value = supplier.get();
            return Result.ok(value);
        } catch (EarlyReturnException e) {
            try {
                E error = e.getErrorValue(errorType);
                return Result.err(error);
            } catch (ClassCastException ex) {
                throw new RuntimeException("Type mismatch in catchEarlyReturn", ex);
            }
        }
    }
    
    /**
     * 处理可能抛出EarlyReturnException的代码块，使用转换函数处理错误
     */
    public static <T, E> Result<T, E> catchEarlyReturn(
        Supplier<T> supplier, 
        Function<Object, E> errorConverter
    ) {
        try {
            T value = supplier.get();
            return Result.ok(value);
        } catch (EarlyReturnException e) {
            E error = errorConverter.apply(e.getErrorValue());
            return Result.err(error);
        }
    }
    
    /**
     * 包装可能抛出异常的操作，将其转换为Result
     */
    public static <T> Result<T, Exception> catchExceptions(Supplier<T> operation) {
        try {
            return Result.ok(operation.get());
        } catch (Exception e) {
            return Result.err(e);
        }
    }
    
    /**
     * 将Java异常转换为AppError
     * 需要显式指定错误类型
     */
    public static <T, E extends AppError> Result<T, E> catchAsAppError(
        Supplier<T> supplier, 
        Function<Exception, E> errorCreator
    ) {
        try {
            return Result.ok(supplier.get());
        } catch (Exception e) {
            return Result.err(errorCreator.apply(e));
        }
    }
    
    /**
     * 将Result转换为另一种错误类型
     */
    public static <T, E, F> Result<T, F> mapError(
        Result<T, E> result, 
        Function<E, F> errorMapper
    ) {
        return result.mapErr(errorMapper);
    }
}