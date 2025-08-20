package com.aeolyn.better_experience.common.util.RustErrTool;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;

/**
 * Option类型，表示一个可能存在的值。
 *
 * @param <T> 值的类型
 */
public abstract sealed class Option<T> permits Option.Some, Option.None {
    private Option() {} // 防止外部继承
    
    /**
     * 有值的情况
     */
    public static final class Some<T> extends Option<T> {
        private final T value;
        
        public Some(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
        
        @Override
        public boolean isSome() {
            return true;
        }
        
        @Override
        public boolean isNone() {
            return false;
        }
        
        @Override
        public T unwrap() {
            return value;
        }
        
        @Override
        public String toString() {
            return "Some(" + value + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Some<?> some = (Some<?>) obj;
            return Objects.equals(value, some.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
    
    /**
     * 无值的情况
     */
    public static final class None<T> extends Option<T> {
        private static final None<?> INSTANCE = new None<>();
        
        private None() {}
        
        @SuppressWarnings("unchecked")
        public static <T> None<T> getInstance() {
            return (None<T>) INSTANCE;
        }
        
        @Override
        public boolean isSome() {
            return false;
        }
        
        @Override
        public boolean isNone() {
            return true;
        }
        
        @Override
        public T unwrap() {
            throw new NoSuchElementException("Called unwrap() on a None value");
        }
        
        @Override
        public String toString() {
            return "None";
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof None;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
    }
    
    // 工厂方法
    public static <T> Option<T> some(T value) {
        return new Some<>(value);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return (Option<T>) None.getInstance();
    }
    
    // 核心方法
    public abstract boolean isSome();
    public abstract boolean isNone();
    public abstract T unwrap();
    
    // 功能方法 (类似Rust的API)
    public T unwrapOr(T defaultValue) {
        return isSome() ? unwrap() : defaultValue;
    }
    
    public T unwrapOrElse(Supplier<T> op) {
        return isSome() ? unwrap() : op.get();
    }
    
    public <U> Option<U> map(Function<T, U> op) {
        if (isSome()) {
            return Option.some(op.apply(unwrap()));
        } else {
            return Option.none();
        }
    }
    
    public Option<T> or(Option<T> opt) {
        return isSome() ? this : opt;
    }
    
    public Option<T> orElse(Supplier<Option<T>> op) {
        return isSome() ? this : op.get();
    }
    
    public void match(Consumer<T> someConsumer, Runnable noneRunnable) {
        if (isSome()) {
            someConsumer.accept(unwrap());
        } else {
            noneRunnable.run();
        }
    }
    
    public Result<T, String> okOr(String error) {
        if (isSome()) {
            return Result.ok(unwrap());
        } else {
            return Result.err(error);
        }
    }
    
    /**
     * 如果值为Some且满足谓词，返回Some，否则返回None
     */
    public Option<T> filter(Predicate<T> predicate) {
        return isSome() && predicate.test(unwrap()) ? this : Option.none();
    }
    
    /**
     * 将Option<T>转换为Result<T, E>，如果为None则提供错误
     */
    public <E> Result<T, E> okOr(E err) {
        return isSome() ? Result.ok(unwrap()) : Result.err(err);
    }
    
    /**
     * 将Option<T>转换为Result<T, E>，如果为None则使用函数生成错误
     */
    public <E> Result<T, E> okOrElse(Supplier<E> errSupplier) {
        return isSome() ? Result.ok(unwrap()) : Result.err(errSupplier.get());
    }
    
    /**
     * 如果值为Some，应用函数并返回新的Option，否则返回None
     */
    public <U> Option<U> andThen(Function<T, Option<U>> fn) {
        return isSome() ? fn.apply(unwrap()) : Option.none();
    }
    
    /**
     * 如果值为Some，应用函数并返回，否则返回提供的默认值
     */
    public <U> U mapOr(U defaultValue, Function<T, U> fn) {
        return isSome() ? fn.apply(unwrap()) : defaultValue;
    }
    
    /**
     * 如果值为Some，应用函数并返回，否则使用函数生成默认值
     */
    public <U> U mapOrElse(Supplier<U> defaultSupplier, Function<T, U> fn) {
        return isSome() ? fn.apply(unwrap()) : defaultSupplier.get();
    }
    
    /**
     * 如果值为Some，应用可能返回Option的函数并扁平化结果
     */
    public <U> Option<U> flatMap(Function<T, Option<U>> fn) {
        return isSome() ? fn.apply(unwrap()) : Option.none();
    }
    
    /**
     * 获取值或抛出带有自定义消息的异常
     */
    public T expect(String message) {
        if (isSome()) {
            return unwrap();
        } else {
            throw new NoSuchElementException(message);
        }
    }
    
    /**
     * 转换为字符串，用于调试
     */
    @Override
    public String toString() {
        if (isSome()) {
            return "Some(" + unwrap() + ")";
        } else {
            return "None";
        }
    }
}