package com.aeolyn.better_experience.common.util.RustErrTool;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Result类型，表示可能成功(T)或失败(E)的操作结果。
 * 这是一个密封类，只能通过Ok或Err子类实例化。
 *
 * @param <T> 成功值的类型
 * @param <E> 错误值的类型
 */
public abstract sealed class Result<T, E> permits Result.Ok, Result.Err {
    private Result() {}
    
    /**
     * 成功情况的包装类
     */
    public static final class Ok<T, E> extends Result<T, E> {
        private final T value;
        
        public Ok(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
        
        @Override
        public boolean isOk() {
            return true;
        }
        
        @Override
        public boolean isErr() {
            return false;
        }
        
        @Override
        public T unwrap() {
            return value;
        }
        
        @Override
        public E unwrapErr() {
            throw new NoSuchElementException("Called unwrapErr() on an Ok value");
        }
        
        @Override
        public String toString() {
            return "Ok(" + value + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Ok<?, ?> ok = (Ok<?, ?>) obj;
            return Objects.equals(value, ok.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
    
    /**
     * 错误情况的包装类
     */
    public static final class Err<T, E> extends Result<T, E> {
        private final E error;
        
        public Err(E error) {
            this.error = error;
        }
        
        public E getError() {
            return error;
        }
        
        @Override
        public boolean isOk() {
            return false;
        }
        
        @Override
        public boolean isErr() {
            return true;
        }
        
        @Override
        public T unwrap() {
            throw new NoSuchElementException("Called unwrap() on an Err value: " + error);
        }
        
        @Override
        public E unwrapErr() {
            return error;
        }
        
        @Override
        public String toString() {
            return "Err(" + error + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Err<?, ?> err = (Err<?, ?>) obj;
            return Objects.equals(error, err.error);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(error);
        }
    }
    
    // 工厂方法
    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }
    
    public static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }
    
    // 核心方法
    public abstract boolean isOk();
    public abstract boolean isErr();
    public abstract T unwrap();
    public abstract E unwrapErr();
    
    // 功能方法 (类似Rust的API)
    public T unwrapOr(T defaultValue) {
        return isOk() ? unwrap() : defaultValue;
    }
    
    public T unwrapOrElse(Function<E, T> op) {
        return isOk() ? unwrap() : op.apply(unwrapErr());
    }
    
    public <U> Result<U, E> map(Function<T, U> op) {
        if (isOk()) {
            return Result.ok(op.apply(unwrap()));
        } else {
            return Result.err(unwrapErr());
        }
    }
    
    public <F> Result<T, F> mapErr(Function<E, F> op) {
        if (isOk()) {
            return Result.ok(unwrap());
        } else {
            return Result.err(op.apply(unwrapErr()));
        }
    }
    
    public <U> Result<U, E> andThen(Function<T, Result<U, E>> op) {
        if (isOk()) {
            return op.apply(unwrap());
        } else {
            return Result.err(unwrapErr());
        }
    }
    
    public Result<T, E> orElse(Function<E, Result<T, E>> op) {
        if (isOk()) {
            return this;
        } else {
            return op.apply(unwrapErr());
        }
    }
    
    public void match(Consumer<T> okConsumer, Consumer<E> errConsumer) {
        if (isOk()) {
            okConsumer.accept(unwrap());
        } else {
            errConsumer.accept(unwrapErr());
        }
    }

    /**
     * 如果结果为Ok且值满足谓词，则返回Ok，否则返回Err
     */
    public Result<T, E> filter(Predicate<T> predicate, E error) {
        if (isOk() && predicate.test(unwrap())) {
            return this;
        } else {
            return Result.err(error);
        }
    }
    
    /**
     * 如果结果为Ok且值满足谓词，则返回Ok，否则使用函数生成Err
     */
    public Result<T, E> filterOrElse(Predicate<T> predicate, Function<T, E> errorFn) {
        if (isOk() && predicate.test(unwrap())) {
            return this;
        } else if (isOk()) {
            return Result.err(errorFn.apply(unwrap()));
        } else {
            return this;
        }
    }
    
    /**
     * 将Result<T, E>转换为Option<T>，丢弃错误信息
     */
    public Option<T> ok() {
        return isOk() ? Option.some(unwrap()) : Option.none();
    }
    
    /**
     * 将Result<T, E>转换为Option<E>，丢弃成功值
     */
    public Option<E> err() {
        return isErr() ? Option.some(unwrapErr()) : Option.none();
    }
    
    /**
     * 如果结果是Ok，返回包含值的Option，否则返回提供的默认Option
     */
    public Option<T> okOr(Option<T> defaultOption) {
        return isOk() ? Option.some(unwrap()) : defaultOption;
    }
    
    /**
     * 如果结果是Ok，返回包含值的Option，否则使用函数生成默认Option
     */
    public Option<T> okOrElse(Supplier<Option<T>> defaultSupplier) {
        return isOk() ? Option.some(unwrap()) : defaultSupplier.get();
    }
    
    /**
     * 将Result<Result<T, E>, E>扁平化为Result<T, E>
     */
    public <U> Result<U, E> flatten() {
        if (isOk()) {
            @SuppressWarnings("unchecked")
            Result<U, E> inner = (Result<U, E>) unwrap();
            return inner;
        } else {
            return Result.err(unwrapErr());
        }
    }
    
    /**
     * 转换为字符串，用于调试
     */
    @Override
    public String toString() {
        if (isOk()) {
            return "Ok(" + unwrap() + ")";
        } else {
            return "Err(" + unwrapErr() + ")";
        }
    }
}