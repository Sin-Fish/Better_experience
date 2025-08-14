package com.aeolyn.better_experience.config.cache;

import com.aeolyn.better_experience.config.ItemConfig;

/**
 * 配置缓存接口
 */
public interface ConfigCache {
    
    /**
     * 获取物品配置
     */
    ItemConfig getItemConfig(String itemId);
    
    /**
     * 检查物品是否启用
     */
    boolean isItemEnabled(String itemId);
    
    /**
     * 获取所有启用的物品ID
     */
    java.util.Set<String> getEnabledItems();
    
    /**
     * 放入配置到缓存
     */
    void put(String itemId, ItemConfig config);
    
    /**
     * 放入启用状态到缓存
     */
    void putEnabled(String itemId, boolean enabled);
    
    /**
     * 从缓存中移除
     */
    void remove(String itemId);
    
    /**
     * 失效整个缓存
     */
    void invalidate();
    
    /**
     * 获取缓存统计信息
     */
    CacheStats getStats();
    
    /**
     * 检查缓存是否有效
     */
    boolean isValid();
}
