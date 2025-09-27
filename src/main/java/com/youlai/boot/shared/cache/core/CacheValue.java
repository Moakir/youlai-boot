package com.youlai.boot.shared.cache.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存封装对象
 *
 * @author hjz
 * @since 2025/3/11
 */
public class CacheValue<K, V> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Setter
    @Getter
    protected K key;
    @Setter
    @Getter
    protected V obj;
    @Setter
    @Getter
    protected long ttl;
    protected volatile long lastAccess;
    @JsonIgnore
    protected AtomicLong accessCount = new AtomicLong();


    public CacheValue() {
    }

    protected CacheValue(K key, V obj) {
        this.key = key;
        this.obj = obj;
        this.ttl = -1;
        this.lastAccess = System.currentTimeMillis();
    }

    protected CacheValue(K key, V obj, long ttl) {
        this.key = key;
        this.obj = obj;
        this.ttl = ttl;
        this.lastAccess = System.currentTimeMillis();
    }

    @JsonIgnore
    public V getValue() {
        this.lastAccess = System.currentTimeMillis();
        this.accessCount.getAndIncrement();
        return this.obj;
    }

    @JsonIgnore
    public Date getExpiredTime() {
        return this.ttl > 0L ? new Date(this.lastAccess + this.ttl) : null;
    }

    @JsonIgnore
    public long getLastAccess() {
        return this.lastAccess;
    }

    @JsonIgnore
    public boolean isExpired() {
        if (this.ttl > 0L) {
            return System.currentTimeMillis() - this.lastAccess > this.ttl;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "CacheValue{" +
                "key=" + key +
                ", obj=" + obj +
                ", ttl=" + ttl +
                ", lastAccess=" + lastAccess +
                ", accessCount=" + accessCount +
                '}';
    }
}
