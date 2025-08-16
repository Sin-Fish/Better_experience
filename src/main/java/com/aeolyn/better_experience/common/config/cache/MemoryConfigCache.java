package com.aeolyn.better_experience.common.config.cache;

import com.aeolyn.better_experience.render3d.config.ItemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存配置缓存实现
 */
public class MemoryConfigCache implements ConfigCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Cache");
    
    // 物品配置缓存
    private final Map<String, ItemConfig> configCache = new ConcurrentHashMap<>();
    
    // 启用物品缓存
    private final Set<String> enabledItems = ConcurrentHashMap.newKeySet();
    
    // 缓存有效性标志
    private volatile boolean valid = false;
    
    // 缓存统计
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    
    @Override
    public ItemConfig getItemConfig(String itemId) {
        if (!valid) {
            misses.incrementAndGet();
            return null;
        }
        
        ItemConfig config = configCache.get(itemId);
        if (config != null) {
            hits.incrementAndGet();
        } else {
            misses.incrementAndGet();
        }
        
        return config;
    }
    
    @Override
    public boolean isItemEnabled(String itemId) {
        if (!valid) {
            misses.incrementAndGet();
            return false;
        }
        
        boolean enabled = enabledItems.contains(itemId);
        if (enabled) {
            hits.incrementAndGet();
        } else {
            misses.incrementAndGet();
        }
        
        return enabled;
    }
    
    @Override
    public Set<String> getEnabledItems() {
        if (!valid) {
            return ConcurrentHashMap.newKeySet();
        }
        return new java.util.HashSet<>(enabledItems);
    }
    
    @Override
    public void put(String itemId, ItemConfig config) {
        if (config != null) {
            configCache.put(itemId, config);
            LOGGER.debug("配置已缓存: {}", itemId);
        }
    }
    
    @Override
    public void putEnabled(String itemId, boolean enabled) {
        if (enabled) {
            enabledItems.add(itemId);
        } else {
            enabledItems.remove(itemId);
        }
        LOGGER.debug("启用状态已缓存: {} = {}", itemId, enabled);
    }
    
    @Override
    public void remove(String itemId) {
        configCache.remove(itemId);
        enabledItems.remove(itemId);
        LOGGER.debug("配置已从缓存移除: {}", itemId);
    }
    
    @Override
    public void invalidate() {
        configCache.clear();
        enabledItems.clear();
        valid = false;
        LOGGER.info("配置缓存已失效");
    }
    
    @Override
    public CacheStats getStats() {
        return new CacheStats(
            hits.get(),
            misses.get(),
            configCache.size(),
            enabledItems.size()
        );
    }
    
    @Override
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 设置缓存为有效状态
     */
    public void setValid(boolean valid) {
        this.valid = valid;
        if (valid) {
            LOGGER.debug("配置缓存已设置为有效状态");
        }
    }
    
    /**
     * 批量添加启用的物品
     */
    public void addAllEnabledItems(Set<String> itemIds) {
        enabledItems.addAll(itemIds);
        LOGGER.debug("批量添加启用物品到缓存: {} 个", itemIds.size());
    }
    
    /**
     * 获取缓存大小信息
     */
    public String getCacheInfo() {
        return String.format("ConfigCache{configs=%d, enabledItems=%d, valid=%s, hitRate=%.2f%%}", 
            configCache.size(), enabledItems.size(), valid, 
            getStats().getHitRate() * 100);
    }
}
