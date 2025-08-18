package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 生存模式物品移动策略
 * 使用PICKUP点击进行物品移动，优化操作减少延迟
 */
public class SurvivalItemMoveStrategy implements ItemMoveStrategy {
    
    @Override
    public void swapSlots(ClientPlayerEntity player, Slot slotA, Slot slotB) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            LogUtil.warn("Inventory", "interactionManager 不可用，无法执行槽位交换");
            return;
        }
        
        int syncId = player.currentScreenHandler.syncId;
        
        // 使用三次PICKUP点击完成交换，不添加等待
        client.interactionManager.clickSlot(syncId, slotA.id, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, slotB.id, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, slotA.id, 0, SlotActionType.PICKUP, player);
        
        LogUtil.info("Inventory", "生存模式交换槽位: " + slotA.id + " <-> " + slotB.id);
    }
    
    @Override
    public void moveItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            LogUtil.warn("Inventory", "interactionManager 不可用，无法执行物品移动");
            return;
        }
        
        int syncId = player.currentScreenHandler.syncId;
        
        // 使用两次PICKUP点击完成移动，不添加等待
        client.interactionManager.clickSlot(syncId, sourceSlot.id, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, targetSlot.id, 0, SlotActionType.PICKUP, player);
        
        LogUtil.info("Inventory", "生存模式移动物品: " + sourceSlot.id + " -> " + targetSlot.id);
    }
    
    @Override
    public boolean stackItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            LogUtil.warn("Inventory", "interactionManager 不可用，无法执行物品堆叠");
            return false;
        }
        
        // 检查是否可以堆叠
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack targetStack = targetSlot.getStack();
        
        if (sourceStack.isEmpty() || targetStack.isEmpty()) {
            return false;
        }
        
        if (!sourceStack.isOf(targetStack.getItem())) {
            return false;
        }
        
        int maxStack = Math.min(sourceStack.getMaxCount(), targetStack.getMaxCount());
        if (targetStack.getCount() >= maxStack) {
            return false;
        }
        
        int syncId = player.currentScreenHandler.syncId;
        
        // 使用PICKUP点击进行堆叠，不添加等待
        client.interactionManager.clickSlot(syncId, sourceSlot.id, 0, SlotActionType.PICKUP, player);
        client.interactionManager.clickSlot(syncId, targetSlot.id, 0, SlotActionType.PICKUP, player);
        
        LogUtil.info("Inventory", "生存模式堆叠物品: " + sourceSlot.id + " -> " + targetSlot.id);
        return true;
    }
    
    @Override
    public void clearSlot(ClientPlayerEntity player, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            LogUtil.warn("Inventory", "interactionManager 不可用，无法清空槽位");
            return;
        }
        
        if (slot.getStack().isEmpty()) {
            return; // 槽位已经是空的
        }
        
        int syncId = player.currentScreenHandler.syncId;
        
        // 使用PICKUP点击清空槽位，不添加等待
        client.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.PICKUP, player);
        
        LogUtil.info("Inventory", "生存模式清空槽位: " + slot.id);
    }
    
    @Override
    public void setSlotStack(ClientPlayerEntity player, Slot slot, ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            LogUtil.warn("Inventory", "interactionManager 不可用，无法设置槽位物品");
            return;
        }
        
        int syncId = player.currentScreenHandler.syncId;
        
        // 如果槽位有物品，先清空
        if (!slot.getStack().isEmpty()) {
            client.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.PICKUP, player);
        }
        
        // 如果鼠标上有物品，先放下
        ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
        if (!cursorStack.isEmpty()) {
            // 找到空槽位放下鼠标上的物品
            for (Slot emptySlot : player.currentScreenHandler.slots) {
                if (emptySlot.getStack().isEmpty()) {
                    client.interactionManager.clickSlot(syncId, emptySlot.id, 0, SlotActionType.PICKUP, player);
                    break;
                }
            }
        }
        
        // 注意：生存模式下直接设置物品比较复杂，这里可能需要特殊处理
        LogUtil.warn("Inventory", "生存模式不支持直接设置槽位物品，请使用其他方法");
    }
}
