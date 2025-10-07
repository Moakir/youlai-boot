package com.youlai.boot.shared.lock;

import java.util.UUID;

/**
 * 锁持有者信息管理类
 * <p>
 * 用于生成和管理锁持有者标识，格式为 uuid:threadId
 *
 * @author hejz
 * @since 2025/9/27
 */
public class LockHolder {
    
    private final String uuid;
    private final String threadId;
    private final String holder;
    
    public LockHolder() {
        this.uuid = UUID.randomUUID().toString().replace("-", "");
        this.threadId = String.valueOf(Thread.currentThread().getId());
        this.holder = uuid + ":" + threadId;
    }
    
    public LockHolder(String holder) {
        this.holder = holder;
        String[] parts = holder.split(":");
        this.uuid = parts.length > 0 ? parts[0] : "";
        this.threadId = parts.length > 1 ? parts[1] : "";
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public String getThreadId() {
        return threadId;
    }
    
    public String getHolder() {
        return holder;
    }
    
    /**
     * 检查是否为当前线程持有
     */
    public boolean isCurrentThread() {
        return threadId.equals(String.valueOf(Thread.currentThread().getId()));
    }
    
    @Override
    public String toString() {
        return holder;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LockHolder that = (LockHolder) obj;
        return holder != null ? holder.equals(that.holder) : that.holder == null;
    }
    
    @Override
    public int hashCode() {
        return holder != null ? holder.hashCode() : 0;
    }
}
