package com.aeolyn.better_experience.common.config.cache;

/**
 * 缓存统计信息
 */
public class CacheStats {
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
    
    public long getHits() {
        return hits;
    }
    
    public long getMisses() {
        return misses;
    }
    
    public int getConfigCacheSize() {
        return configCacheSize;
    }
    
    public int getEnabledItemsSize() {
        return enabledItemsSize;
    }
    
    public double getHitRate() {
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    public long getTotalRequests() {
        return hits + misses;
    }
    
    @Override
    public String toString() {
        return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, configCache=%d, enabledItems=%d}", 
            hits, misses, getHitRate() * 100, configCacheSize, enabledItemsSize);
    }
}
