package com.youlai.boot.shared.cache;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存适配器，提供统一的缓存访问方法
 *
 * @author hjz
 * @since 2025/3/11
 */
@Component
public class CacheAdapter {

    @Resource
    private ICacheService cacheService;

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return cacheService.hasKey(key);
    }

    /**
     * 删除 key
     */
    public Boolean delete(String key) {
        return cacheService.delete(key);
    }

    /**
     * 批量删除 key
     */
    public Long delete(Collection<String> keys) {
        return cacheService.delete(keys);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return cacheService.expire(key, timeout, unit);
    }

    /**
     * 获取字符串值
     */
    public String get(String key) {
        return cacheService.get(key);
    }

    /**
     * 获取对象值
     */
    public Object getObject(String key) {
        return cacheService.getObject(key);
    }

    /**
     * 批量获取字符串值
     */
    public List<String> multiGet(List<String> keys) {
        return cacheService.multiGet(keys);
    }

    /**
     * 设置字符串值
     */
    public void set(String key, String value) {
        cacheService.set(key, value);
    }

    /**
     * 设置字符串值及过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        cacheService.set(key, value, timeout, unit);
    }

    /**
     * 设置对象值
     */
    public void setObject(String key, Object value) {
        cacheService.setObject(key, value);
    }

    /**
     * 设置对象值及过期时间
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        cacheService.setObject(key, value, timeout, unit);
    }

    /**
     * 递增
     */
    public Long increment(String key) {
        return cacheService.increment(key);
    }

    /**
     * 递增指定数量
     */
    public Long increment(String key, long delta) {
        return cacheService.increment(key, delta);
    }

    /**
     * 获取哈希表中指定字段的值
     */
    public Object hashGet(String key, String field) {
        return cacheService.hashGet(key, field);
    }

    /**
     * 获取哈希表中多个字段的值
     */
    public List<Object> hashMultiGet(String key, Collection<Object> fields) {
        return cacheService.hashMultiGet(key, fields);
    }

    /**
     * 获取哈希表中所有字段和值
     */
    public Map<Object, Object> hashEntries(String key) {
        return cacheService.hashEntries(key);
    }

    /**
     * 设置哈希表字段的值
     */
    public void hashPut(String key, String field, Object value) {
        cacheService.hashPut(key, field, value);
    }

    /**
     * 批量设置哈希表字段的值（字符串类型）
     */
    public void hashPutAllString(String key, Map<String, String> map) {
        cacheService.hashPutAllString(key, map);
    }

    /**
     * 批量设置哈希表字段的值（对象类型）
     */
    public void hashPutAll(String key, Map<String, Object> map) {
        cacheService.hashPutAll(key, map);
    }

    /**
     * 哈希表中字段是否存在
     */
    public Boolean hashHasKey(String key, String field) {
        return cacheService.hashHasKey(key, field);
    }

    /**
     * 删除哈希表中的字段
     */
    public Long hashDelete(String key, Object... fields) {
        return cacheService.hashDelete(key, fields);
    }
} 