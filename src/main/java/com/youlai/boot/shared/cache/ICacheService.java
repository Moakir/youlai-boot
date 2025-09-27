package com.youlai.boot.shared.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存服务接口
 *
 * @author hejz
 * @since 2025/9/27
 */
public interface ICacheService {

    // ========== 通用操作 ==========
    /**
     * 判断 key 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 删除 key
     */
    Boolean delete(String key);

    /**
     * 批量删除 key
     */
    Long delete(Collection<String> keys);

    /**
     * 设置过期时间
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    // ========== String 操作 ==========
    /**
     * 获取字符串值
     */
    String get(String key);

    /**
     * 获取对象值
     */
    Object getObject(String key);

    /**
     * 批量获取字符串值
     */
    List<String> multiGet(List<String> keys);

    /**
     * 设置字符串值
     */
    void set(String key, String value);

    /**
     * 设置字符串值及过期时间
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 设置对象值
     */
    void setObject(String key, Object value);

    /**
     * 设置对象值及过期时间
     */
    void setObject(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 递增
     */
    Long increment(String key);

    /**
     * 递增指定数量
     */
    Long increment(String key, long delta);

    // ========== Hash 操作 ==========
    /**
     * 获取哈希表中指定字段的值
     */
    Object hashGet(String key, String field);

    /**
     * 获取哈希表中多个字段的值
     */
    List<Object> hashMultiGet(String key, Collection<Object> fields);

    /**
     * 获取哈希表中所有字段和值
     */
    Map<Object, Object> hashEntries(String key);

    /**
     * 设置哈希表字段的值
     */
    void hashPut(String key, String field, Object value);

    /**
     * 批量设置哈希表字段的值（对象类型）
     */
    void hashPutAll(String key, Map<String, Object> map);

    /**
     * 批量设置哈希表字段的值（字符串类型）
     */
    void hashPutAllString(String key, Map<String, String> map);

    /**
     * 哈希表中字段是否存在
     */
    Boolean hashHasKey(String key, String field);

    /**
     * 删除哈希表中的字段
     */
    Long hashDelete(String key, Object... fields);
}
