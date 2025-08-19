package com.aeolyn.better_experience.inventory.util;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * 库存统计工具类
 * 提供统计空位数量、物品数量等功能
 */
public class InventoryStatsUtil {
    
    /**
     * 统计背包空位数量（主背包，不包括快捷栏）
     * @param player 玩家
     * @return 空位数量
     */
    public static int countPlayerEmptySlots(ClientPlayerEntity player) {
        int emptySlots = 0;
        for (int i = 9; i < 36; i++) { // 背包槽位 9-35
            if (player.getInventory().getStack(i).isEmpty()) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
    
    /**
     * 统计容器空位数量
     * @param inventory 容器库存
     * @return 空位数量
     */
    public static int countContainerEmptySlots(Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        
        int emptySlots = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
    
    /**
     * 统计背包物品数量（主背包，不包括快捷栏）
     * @param player 玩家
     * @return 物品数量
     */
    public static int countPlayerItems(ClientPlayerEntity player) {
        int itemCount = 0;
        for (int i = 9; i < 36; i++) { // 背包槽位 9-35
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                itemCount++;
            }
        }
        return itemCount;
    }
    
    /**
     * 统计容器物品数量
     * @param inventory 容器库存
     * @return 物品数量
     */
    public static int countContainerItems(Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        
        int itemCount = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                itemCount++;
            }
        }
        return itemCount;
    }
    
    /**
     * 统计背包物品总数量（包括堆叠数量）
     * @param player 玩家
     * @return 物品总数量
     */
    public static int countPlayerTotalItems(ClientPlayerEntity player) {
        int totalCount = 0;
        for (int i = 9; i < 36; i++) { // 背包槽位 9-35
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }
    
    /**
     * 统计容器物品总数量（包括堆叠数量）
     * @param inventory 容器库存
     * @return 物品总数量
     */
    public static int countContainerTotalItems(Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        
        int totalCount = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }
    
    /**
     * 获取容器的库存对象
     * @param player 玩家
     * @return 容器库存对象，如果没有找到则返回null
     */
    public static Inventory getContainerInventory(ClientPlayerEntity player) {
        if (player == null || player.currentScreenHandler == null) {
            return null;
        }
        
        // 遍历所有槽位，找到非玩家背包的库存
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.inventory != player.getInventory()) {
                return slot.inventory;
            }
        }
        return null;
    }
    
    /**
     * 记录库存统计信息到日志
     * @param player 玩家
     * @param containerInventory 容器库存
     * @param logTag 日志标签
     */
    public static void logInventoryStats(ClientPlayerEntity player, Inventory containerInventory, String logTag) {
        int playerEmptySlots = countPlayerEmptySlots(player);
        int containerEmptySlots = countContainerEmptySlots(containerInventory);
        int playerItems = countPlayerItems(player);
        int containerItems = countContainerItems(containerInventory);
        
        LogUtil.info(logTag, "=== 库存统计 ===");
        LogUtil.info(logTag, "背包空位: " + playerEmptySlots + ", 背包物品: " + playerItems);
        LogUtil.info(logTag, "容器空位: " + containerEmptySlots + ", 容器物品: " + containerItems);
    }
}
