package com.aeolyn.better_experience.inventory.strategy;

import com.aeolyn.better_experience.inventory.config.InventorySortConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 排序策略工厂
 */
public class SortStrategyFactory {
    
    private static final Map<InventorySortConfig.SortMode, SortStrategy> strategies = new HashMap<>();
    
    static {
        // 注册默认的策略
        strategies.put(InventorySortConfig.SortMode.NAME, new NameSortStrategy());
        strategies.put(InventorySortConfig.SortMode.QUANTITY, new QuantitySortStrategy());
    }
    
    /**
     * 获取排序策略
     * @param sortMode 排序模式
     * @return 排序策略
     */
    public static SortStrategy getStrategy(InventorySortConfig.SortMode sortMode) {
        return strategies.getOrDefault(sortMode, new NameSortStrategy());
    }
    
    /**
     * 注册自定义策略
     * @param sortMode 排序模式
     * @param strategy 策略
     */
    public static void registerStrategy(InventorySortConfig.SortMode sortMode, SortStrategy strategy) {
        strategies.put(sortMode, strategy);
    }
    
    /**
     * 获取所有可用的策略
     * @return 策略映射
     */
    public static Map<InventorySortConfig.SortMode, SortStrategy> getAllStrategies() {
        return new HashMap<>(strategies);
    }
}
