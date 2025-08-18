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
}
