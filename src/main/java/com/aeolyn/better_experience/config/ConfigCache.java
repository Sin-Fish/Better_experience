package com.aeolyn.better_experience.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置缓存管理器
 * 用于优化配置查询性能，减少重复的文件I/O操作
 */
public class ConfigCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Cache");
    
    // 物品配置缓存
    private static final Map<String, ItemConfig> itemConfigCache = new ConcurrentHashMap<>();
    
    // 启用物品缓存
    private static final Set<String> enabledItemsCache = ConcurrentHashMap.newKeySet();
    
    // 缓存有效性标记
    private static volatile boolean cacheValid = false;
    
    // 缓存统计
    private static volatile long cacheHits = 0;
    private static volatile long cacheMisses = 0;
    
    /**
     * 获取物品配置（带缓存）
     */
    public static ItemConfig getItemConfig(String itemId) {
        if (!cacheValid) {
            rebuildCache();
        }
        
        ItemConfig config = itemConfigCache.get(itemId);
        if (config != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        
        return config;
    }
    
    /**
     * 检查物品是否启用（带缓存）
     */
    public static boolean isItemEnabled(String itemId) {
        if (!cacheValid) {
            rebuildCache();
        }
        
        boolean enabled = enabledItemsCache.contains(itemId);
        if (enabled) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        
        return enabled;
    }
    
    /**
     * 获取所有启用的物品ID
     */
    public static Set<String> getEnabledItems() {
        if (!cacheValid) {
            rebuildCache();
        }
        return new HashSet<>(enabledItemsCache);
    }
    
    /**
     * 失效缓存
     */
    public static void invalidateCache() {
        cacheValid = false;
        itemConfigCache.clear();
        enabledItemsCache.clear();
        
        if (ConfigManager.getInstance().isConfigLogsEnabled()) {
            LOGGER.info("配置缓存已失效");
        }
    }
    
    /**
     * 重建缓存
     */
    private static void rebuildCache() {
        ConfigManager configManager = ConfigManager.getInstance();
        ItemsConfig itemsConfig = configManager.getItemsConfig();
        
        if (itemsConfig != null) {
            // 重建启用物品缓存
            enabledItemsCache.clear();
            enabledItemsCache.addAll(itemsConfig.getEnabledItems());
            
            // 重建配置缓存
            itemConfigCache.clear();
            for (String itemId : enabledItemsCache) {
                ItemConfig config = configManager.getItemConfigDirect(itemId);
                if (config != null) {
                    itemConfigCache.put(itemId, config);
                }
            }
            
            if (ConfigManager.getInstance().isConfigLogsEnabled()) {
                LOGGER.info("配置缓存已重建，启用物品数量: {}, 配置缓存数量: {}", 
                    enabledItemsCache.size(), itemConfigCache.size());
            }
        }
        
        cacheValid = true;
    }
    
    /**
     * 获取缓存统计信息
     */
    public static CacheStats getCacheStats() {
        return new CacheStats(cacheHits, cacheMisses, itemConfigCache.size(), enabledItemsCache.size());
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final long hits;
        private final long misses;
        private final int configCacheSize;
        private final int enabledItemsSize;
        
        public CacheStats(long hits, long misses, int configCacheSize, int enabledItemsSize) {
            this.hits = hits;
            this.misses = misses;
            this.configCacheSize = configCacheSize;
            this.enabledItemsSize = enabledItemsSize;
        }
        
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public int getConfigCacheSize() { return configCacheSize; }
        public int getEnabledItemsSize() { return enabledItemsSize; }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, configCache=%d, enabledItems=%d}", 
                hits, misses, getHitRate() * 100, configCacheSize, enabledItemsSize);
        }
    }
}
