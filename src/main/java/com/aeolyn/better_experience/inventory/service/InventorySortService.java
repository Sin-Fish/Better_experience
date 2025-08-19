package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.inventory.Inventory;

/**
 * 背包排序服务接口
 * 定义背包和容器排序的核心方法
 */
public interface InventorySortService {
    
    /**
     * 整理背包
     * @param sortMode 排序模式
     */
    void sortInventory(InventorySortConfig.SortMode sortMode);
    
    /**
     * 整理背包（支持合并模式）
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 整理容器（支持合并模式）
     * @param container 容器
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 整理容器（默认不合并）
     * @param container 容器
     */
    void sortContainer(Inventory container);
    
    /**
     * 智能排序：根据鼠标位置决定排序背包还是容器
     */
    void smartSortByMousePosition();
    
    /**
     * 简单的选择排序算法
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 测试潜影盒支持
     */
    void testShulkerBoxSupport();
}
