package com.aeolyn.better_experience.inventory.core;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * 物品移动策略接口
 * 用于抽象不同模式下的物品移动逻辑
 */
public interface ItemMoveStrategy {
    
    /**
     * 交换两个槽位的物品
     * @param player 玩家实体
     * @param slotA 槽位A
     * @param slotB 槽位B
     */
    void swapSlots(ClientPlayerEntity player, Slot slotA, Slot slotB);
    
    /**
     * 将物品从源槽位移动到目标槽位
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     */
    void moveItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot);
    
    /**
     * 将物品堆叠到目标槽位
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     * @return 是否成功堆叠
     */
    boolean stackItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot);
    
    /**
     * 清空指定槽位
     * @param player 玩家实体
     * @param slot 要清空的槽位
     */
    void clearSlot(ClientPlayerEntity player, Slot slot);
    
    /**
     * 设置槽位的物品
     * @param player 玩家实体
     * @param slot 目标槽位
     * @param stack 要设置的物品
     */
    void setSlotStack(ClientPlayerEntity player, Slot slot, ItemStack stack);
    
    /**
     * 统一的物品移动操作
     * 根据目标槽位状态自动选择移动或交换
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     * @return 操作是否成功
     */
    default boolean moveOrSwapItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        if (sourceSlot == targetSlot) {
            return true; // 相同槽位，无需操作
        }
        
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack targetStack = targetSlot.getStack();
        
        if (sourceStack.isEmpty()) {
            return true; // 源槽位为空，无需操作
        }
        
        if (targetStack.isEmpty()) {
            // 目标槽位为空，执行移动
            moveItem(player, sourceSlot, targetSlot);
            return true;
        }
        
        // 目标槽位有物品，执行交换
        swapSlots(player, sourceSlot, targetSlot);
        return true;
    }
    
    /**
     * 智能物品放置
     * 优先尝试堆叠，如果失败则交换
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     * @return 操作是否成功
     */
    default boolean smartPlaceItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        if (sourceSlot == targetSlot) {
            return true;
        }
        
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack targetStack = targetSlot.getStack();
        
        if (sourceStack.isEmpty()) {
            return true;
        }
        
        if (targetStack.isEmpty()) {
            // 目标槽位为空，直接移动
            moveItem(player, sourceSlot, targetSlot);
            return true;
        }
        
        // 目标槽位有物品，执行交换以保持索引正确性
        // 这样可以避免队列中索引位置错误的问题
        swapSlots(player, sourceSlot, targetSlot);
        return true;
    }
    
    /**
     * 检查两个物品是否可以堆叠
     * @param stack1 物品1
     * @param stack2 物品2
     * @return 是否可以堆叠
     */
    default boolean canStackItems(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return false;
        }
        
        if (!stack1.isOf(stack2.getItem())) {
            return false;
        }
        
        int maxStack = Math.min(stack1.getMaxCount(), stack2.getMaxCount());
        return stack2.getCount() < maxStack;
    }
    
    /**
     * 批量移动物品到指定位置
     * 使用队列方式处理，确保顺序正确
     * @param player 玩家实体
     * @param sourceSlots 源槽位列表
     * @param targetSlot 目标槽位
     * @return 成功移动的物品数量
     */
    default int batchMoveItems(ClientPlayerEntity player, java.util.List<Slot> sourceSlots, Slot targetSlot) {
        int movedCount = 0;
        
        for (Slot sourceSlot : sourceSlots) {
            if (sourceSlot.getStack().isEmpty()) {
                continue;
            }
            
            if (smartPlaceItem(player, sourceSlot, targetSlot)) {
                movedCount++;
            }
        }
        
        return movedCount;
    }
    
    /**
     * 队列安全的物品移动
     * 专门用于队列操作，确保索引正确性
     * 当目标槽位有物品时，总是执行交换而不是堆叠
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     * @return 操作是否成功
     */
    default boolean queueSafeMove(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        if (sourceSlot == targetSlot) {
            return true;
        }
        
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack targetStack = targetSlot.getStack();
        
        if (sourceStack.isEmpty()) {
            return true;
        }
        
        if (targetStack.isEmpty()) {
            // 目标槽位为空，执行移动
            moveItem(player, sourceSlot, targetSlot);
            return true;
        }
        
        // 目标槽位有物品，执行交换以保持队列索引正确性
        // 这样可以避免队列中记录的索引位置错误
        swapSlots(player, sourceSlot, targetSlot);
        return true;
    }
    
    /**
     * 队列安全的物品堆叠
     * 专门用于队列操作中的堆叠，确保索引正确性
     * @param player 玩家实体
     * @param sourceSlot 源槽位
     * @param targetSlot 目标槽位
     * @return 操作是否成功
     */
    default boolean queueSafeStack(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        if (sourceSlot == targetSlot) {
            return true;
        }
        
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack targetStack = targetSlot.getStack();
        
        if (sourceStack.isEmpty() || targetStack.isEmpty()) {
            return false;
        }
        
        // 检查是否可以堆叠
        if (!canStackItems(sourceStack, targetStack)) {
            return false;
        }
        
        // 执行堆叠操作
        return stackItem(player, sourceSlot, targetSlot);
    }
}
