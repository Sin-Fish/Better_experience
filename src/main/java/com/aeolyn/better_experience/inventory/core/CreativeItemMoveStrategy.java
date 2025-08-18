package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * 创造模式物品移动策略
 * 使用直接操作库存 + 网络数据包同步服务端
 */
public class CreativeItemMoveStrategy implements ItemMoveStrategy {
    
    @Override
    public void swapSlots(ClientPlayerEntity player, Slot slotA, Slot slotB) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            LogUtil.warn("Inventory", "客户端或网络处理器不可用，无法执行槽位交换");
            return;
        }
        
        // 获取两个槽位的物品
        ItemStack stackA = slotA.getStack().copy();
        ItemStack stackB = slotB.getStack().copy();
        
        // 在创造模式下，直接操作库存
        if (slotA.inventory == player.getInventory()) {
            player.getInventory().setStack(slotA.getIndex(), stackB);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, slotA.getIndex(), stackB);
        }
        if (slotB.inventory == player.getInventory()) {
            player.getInventory().setStack(slotB.getIndex(), stackA);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, slotB.getIndex(), stackA);
        }
        
        LogUtil.info("Inventory", "创造模式交换槽位: " + slotA.id + " <-> " + slotB.id);
    }
    
    @Override
    public void moveItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            LogUtil.warn("Inventory", "客户端或网络处理器不可用，无法执行物品移动");
            return;
        }
        
        // 获取源槽位的物品
        ItemStack sourceStack = sourceSlot.getStack().copy();
        
        // 在创造模式下，直接操作库存
        if (targetSlot.inventory == player.getInventory()) {
            player.getInventory().setStack(targetSlot.getIndex(), sourceStack);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, targetSlot.getIndex(), sourceStack);
        }
        if (sourceSlot.inventory == player.getInventory()) {
            player.getInventory().setStack(sourceSlot.getIndex(), ItemStack.EMPTY);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, sourceSlot.getIndex(), ItemStack.EMPTY);
        }
        
        LogUtil.info("Inventory", "创造模式移动物品: " + sourceSlot.id + " -> " + targetSlot.id);
    }
    
    @Override
    public boolean stackItem(ClientPlayerEntity player, Slot sourceSlot, Slot targetSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            LogUtil.warn("Inventory", "客户端或网络处理器不可用，无法执行物品堆叠");
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
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, targetSlot.getIndex(), newStack);
        }
        
        // 处理源槽位剩余物品
        if (remainingCount > 0) {
            ItemStack remainingStack = sourceStack.copy();
            remainingStack.setCount(remainingCount);
            if (sourceSlot.inventory == player.getInventory()) {
                player.getInventory().setStack(sourceSlot.getIndex(), remainingStack);
                // 发送数据包同步服务端
                sendCreativeInventoryAction(client, sourceSlot.getIndex(), remainingStack);
            }
        } else {
            // 清空源槽位
            if (sourceSlot.inventory == player.getInventory()) {
                player.getInventory().setStack(sourceSlot.getIndex(), ItemStack.EMPTY);
                // 发送数据包同步服务端
                sendCreativeInventoryAction(client, sourceSlot.getIndex(), ItemStack.EMPTY);
            }
        }
        
        LogUtil.info("Inventory", "创造模式堆叠物品: " + sourceSlot.id + " -> " + targetSlot.id + ", 新数量: " + newCount);
        return true;
    }
    
    @Override
    public void clearSlot(ClientPlayerEntity player, Slot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            LogUtil.warn("Inventory", "客户端或网络处理器不可用，无法清空槽位");
            return;
        }
        
        // 直接清空槽位
        if (slot.inventory == player.getInventory()) {
            player.getInventory().setStack(slot.getIndex(), ItemStack.EMPTY);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, slot.getIndex(), ItemStack.EMPTY);
        }
        
        LogUtil.info("Inventory", "创造模式清空槽位: " + slot.id);
    }
    
    @Override
    public void setSlotStack(ClientPlayerEntity player, Slot slot, ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            LogUtil.warn("Inventory", "客户端或网络处理器不可用，无法设置槽位物品");
            return;
        }
        
        // 直接设置槽位物品
        if (slot.inventory == player.getInventory()) {
            player.getInventory().setStack(slot.getIndex(), stack);
            // 发送数据包同步服务端
            sendCreativeInventoryAction(client, slot.getIndex(), stack);
        }
        
        LogUtil.info("Inventory", "创造模式设置槽位物品: " + slot.id + " -> " + (stack.isEmpty() ? "空" : stack.getName().getString() + " x" + stack.getCount()));
    }
    
    /**
     * 发送创造模式库存操作数据包到服务端
     */
    private void sendCreativeInventoryAction(MinecraftClient client, int slotId, ItemStack stack) {
        try {
            // 创建创造模式库存操作数据包
            CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(slotId, stack);
            
            // 发送数据包到服务端
            client.getNetworkHandler().sendPacket(packet);
            
            LogUtil.info("Inventory", "发送创造模式库存数据包: 槽位 " + slotId + " -> " + (stack.isEmpty() ? "空" : stack.getName().getString() + " x" + stack.getCount()));
        } catch (Exception e) {
            LogUtil.warn("Inventory", "发送创造模式库存数据包失败: " + e.getMessage());
        }
    }
}
