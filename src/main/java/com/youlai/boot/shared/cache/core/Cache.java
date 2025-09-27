package com.youlai.boot.shared.cache.core;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 通用缓存容器接口
 *
 * @author hjz
 * @since 2025/3/11
 */
interface Cache<K, V> extends Serializable {

    void put(K key, V value);

    void put(K key, V value, long timeout);

    V get(K key);

    V get(K key, Function<K, V> func);

    void remove(K key);

    void clear();

    boolean hasKey(K key);

}