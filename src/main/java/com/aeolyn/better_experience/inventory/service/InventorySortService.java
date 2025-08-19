package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

/**
 * 背包排序服务接口
 * 定义背包和容器排序的核心方法，支持自定义比较器
 */
public interface InventorySortService {
    
    /**
     * 整理背包（使用默认比较器）
     * @param sortMode 排序模式
     */
    void sortInventory(InventorySortConfig.SortMode sortMode);
    
    /**
     * 整理背包（使用默认比较器，支持合并模式）
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 整理背包（支持自定义比较器）
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     * @param comparator 自定义比较器
     */
    void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator);
    
    /**
     * 整理容器（使用默认比较器，支持合并模式）
     * @param container 容器
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 整理容器（支持自定义比较器）
     * @param container 容器
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     * @param comparator 自定义比较器
     */
    void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator);
    
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
     * 简单的选择排序算法（使用默认比较器）
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 简单的选择排序算法（支持自定义比较器）
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     * @param comparator 自定义比较器
     */
    void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator);
    

}
