package com.github.unafraid.spring.bot.util;

import java.util.Objects;

/**
 * @param <T>
 * @param <R>
 * @author UnAfraid
 */
@FunctionalInterface
public interface IThrowableFunction<T, R> {
    R apply(T var1) throws Exception;

    default <V> IThrowableFunction<V, R> compose(IThrowableFunction<? super V, ? extends T> var1) {
        Objects.requireNonNull(var1);
        return (var2) -> {
            return this.apply(var1.apply(var2));
        };
    }

    default <V> IThrowableFunction<T, V> andThen(IThrowableFunction<? super R, ? extends V> var1) {
        Objects.requireNonNull(var1);
        return (var2) -> {
            return var1.apply(this.apply(var2));
        };
    }

    static <T> IThrowableFunction<T, T> identity() {
        return (var0) -> {
            return var0;
        };
    }
}
