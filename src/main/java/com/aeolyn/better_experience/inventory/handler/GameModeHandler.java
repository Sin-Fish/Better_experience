package com.aeolyn.better_experience.inventory.handler;

import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;

/**
 * 游戏模式处理器接口
 * 定义不同游戏模式的排序逻辑
 */
public interface GameModeHandler {
    
    /**
     * 执行排序
     * @param player 玩家
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     */
    void performSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst);
    
    /**
     * 执行排序（带比较器）
     * @param player 玩家
     * @param sortMode 排序模式
     * @param mergeFirst 是否先合并
     * @param comparator 自定义比较器
     */
    void performSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator);
    
    /**
     * 执行合并排序
     * @param player 玩家
     * @param currentItems 当前物品列表
     * @param sortMode 排序模式
     */
    void performMergeSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode);
    
    /**
     * 执行合并排序（带比较器）
     * @param player 玩家
     * @param currentItems 当前物品列表
     * @param sortMode 排序模式
     * @param comparator 自定义比较器
     */
    void performMergeSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode, Comparator<ItemStack> comparator);
    
    /**
     * 执行简单排序
     * @param player 玩家
     * @param currentItems 当前物品列表
     * @param sortMode 排序模式
     */
    void performSimpleSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode);
    
    /**
     * 执行简单排序（带比较器）
     * @param player 玩家
     * @param currentItems 当前物品列表
     * @param sortMode 排序模式
     * @param comparator 自定义比较器
     */
    void performSimpleSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode, Comparator<ItemStack> comparator);
}
