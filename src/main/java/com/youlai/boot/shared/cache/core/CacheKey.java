package com.youlai.boot.shared.cache.core;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存键封装对象
 *
 * @author hjz
 * @since 2025/3/11
 */
@Setter
@Getter
public class CacheKey<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private T value;

    public CacheKey(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (this.getClass() == obj.getClass()) {
            CacheKey<?> that = (CacheKey<?>) obj;
            return this.value.equals(that.value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.value == null ? 0 : this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value == null ? "null" : this.value.toString();
    }

}
