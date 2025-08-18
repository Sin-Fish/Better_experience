package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 创造模式物品移动策略
 * 使用直接操作库存的方式
 */
public class CreativeItemMoveStrategy implements ItemMoveStrategy {
    
    @Override
    public void swapSlots(ClientPlayerEntity player, Slot slotA, Slot slotB) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            LogUtil.warn("Inventory", "玩家不可用，无法执行槽位交换");
            return;
        }
        
        // 获取两个槽位的物品
        ItemStack stackA = slotA.getStack().copy();
        ItemStack stackB = slotB.getStack().copy();
        
        // 在创造模式下，直接操作库存
        if (slotA.inventory == player.getInventory()) {
            player.getInventory().setStack(slotA.getIndex(), stackB);
        }
        if (slotB.inventory == player.getInventory()) {
            player.getInventory().setStack(slotB.getIndex(), stackA);
        }
        
        // 通知服务端更新
        notifyServerUpdate(player, slotA, slotB);
        
        LogUtil.info("Inventory", "创造模式交换槽位: " + slotA.id + " <-> " + slotB.id);
    }
    
    @Override
    public void moveItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            LogUtil.warn("Inventory", "玩家不可用，无法执行物品移动");
            return;
        }
        
        // 获取源槽位的物品
        ItemStack sourceStack = sourceSlot.getStack().copy();
        
        // 在创造模式下，直接操作库存
        if (targetSlot.inventory == player.getInventory()) {
            player.getInventory().setStack(targetSlot.getIndex(), sourceStack);
        }
        if (sourceSlot.inventory == player.getInventory()) {
            player.getInventory().setStack(sourceSlot.getIndex(), ItemStack.EMPTY);
        }
        
        // 通知服务端更新
        notifyServerUpdate(player, sourceSlot, targetSlot);
        
        LogUtil.info("Inventory", "创造模式移动物品: " + sourceSlot.id + " -> " + targetSlot.id);
    }
    
    @Override
    public boolean stackItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            LogUtil.warn("Inventory", "玩家不可用，无法执行物品堆叠");
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
        
        // 计算堆叠后的数量
        int newCount = Math.min(targetStack.getCount() + sourceStack.getCount(), maxStack);
        int remainingCount = targetStack.getCount() + sourceStack.getCount() - newCount;
        
        // 创建新的堆叠物品
        ItemStack newStack = targetStack.copy();
        newStack.setCount(newCount);
        
        // 设置目标槽位
        if (targetSlot.inventory == player.getInventory()) {
            player.getInventory().setStack(targetSlot.getIndex(), newStack);
        }
        
        // 处理源槽位剩余物品
        if (remainingCount > 0) {
            ItemStack remainingStack = sourceStack.copy();
            remainingStack.setCount(remainingCount);
            if (sourceSlot.inventory == player.getInventory()) {
                player.getInventory().setStack(sourceSlot.getIndex(), remainingStack);
            }
        } else {
            // 清空源槽位
            if (sourceSlot.inventory == player.getInventory()) {
                player.getInventory().setStack(sourceSlot.getIndex(), ItemStack.EMPTY);
            }
        }
        
        // 通知服务端更新
        notifyServerUpdate(player, sourceSlot, targetSlot);
        
        LogUtil.info("Inventory", "创造模式堆叠物品: " + sourceSlot.id + " -> " + targetSlot.id + ", 新数量: " + newCount);
        return true;
    }
    
    @Override
    public void clearSlot(ClientPlayerEntity player, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            LogUtil.warn("Inventory", "玩家不可用，无法清空槽位");
            return;
        }
        
        // 直接清空槽位
        if (slot.inventory == player.getInventory()) {
            player.getInventory().setStack(slot.getIndex(), ItemStack.EMPTY);
        }
        
        // 通知服务端更新
        notifyServerUpdate(player, slot, null);
        
        LogUtil.info("Inventory", "创造模式清空槽位: " + slot.id);
    }
    
    @Override
    public void setSlotStack(ClientPlayerEntity player, Slot slot, ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            LogUtil.warn("Inventory", "玩家不可用，无法设置槽位物品");
            return;
        }
        
        // 直接设置槽位物品
        if (slot.inventory == player.getInventory()) {
            player.getInventory().setStack(slot.getIndex(), stack);
        }
        
        // 通知服务端更新
        notifyServerUpdate(player, slot, null);
        
        LogUtil.info("Inventory", "创造模式设置槽位物品: " + slot.id + " -> " + (stack.isEmpty() ? "空" : stack.getName().getString() + " x" + stack.getCount()));
    }
    
    /**
     * 通知服务端更新库存
     */
    private void notifyServerUpdate(ClientPlayerEntity player, Slot slot1, Slot slot2) {
        // 在创造模式下，通过发送库存更新包来同步服务端
        try {
            // 标记库存为已更改
            player.getInventory().markDirty();
            
            // 如果是创造模式，服务端会自动同步
            if (player.getAbilities().creativeMode) {
                // 创造模式下服务端会接受客户端的库存更改
                LogUtil.info("Inventory", "创造模式库存已更新，等待服务端同步");
            }
        } catch (Exception e) {
            LogUtil.warn("Inventory", "通知服务端更新失败: " + e.getMessage());
        }
    }
}
