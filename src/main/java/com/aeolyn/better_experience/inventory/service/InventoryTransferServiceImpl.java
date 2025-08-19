package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包转移服务实现
 * 实现物品转移功能
 */
public class InventoryTransferServiceImpl implements InventoryTransferService {
    
    @Override
    public void smartTransferItems() {
        try {
            LogUtil.info("Transfer", "开始智能转移物品");
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                LogUtil.warn("Transfer", "客户端或玩家不存在");
                return;
            }
            
            // 检查是否在容器界面
            if (!(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
                LogUtil.info("Transfer", "不在容器界面，跳过智能转移");
                return;
            }
            
            // 获取当前屏幕
            net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
            
            // 获取容器
            Inventory container = getContainerInventory(handledScreen);
            if (container == null) {
                LogUtil.warn("Transfer", "无法获取容器");
                return;
            }
            
            // 根据鼠标位置决定转移方向
            if (isMouseOverPlayerInventory(handledScreen)) {
                // 鼠标在背包区域，执行存入操作
                LogUtil.info("Transfer", "鼠标在背包区域，执行存入操作");
                depositToContainer(container);
            } else {
                // 鼠标在容器区域，执行取出操作
                LogUtil.info("Transfer", "鼠标在容器区域，执行取出操作");
                withdrawFromContainer(container);
            }
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "智能转移失败", e);
        }
    }
    
    @Override
    public void depositToContainer(Inventory container) {
        try {
            LogUtil.info("Transfer", "开始存入容器");
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Transfer", "玩家不存在");
                return;
            }
            
            // 获取背包中的物品
            List<ItemStack> playerItems = new ArrayList<>();
            for (int i = 9; i < 36; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    playerItems.add(stack);
                }
            }
            
            LogUtil.info("Transfer", "背包中有 " + playerItems.size() + " 个物品需要转移");
            
            // 尝试将物品存入容器
            for (ItemStack item : playerItems) {
                if (canDepositToContainer(container, item)) {
                    // 执行转移
                    transferItemToContainer(player, container, item);
                }
            }
            
            LogUtil.info("Transfer", "存入容器完成");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "存入容器失败", e);
        }
    }
    
    @Override
    public void withdrawFromContainer(Inventory container) {
        try {
            LogUtil.info("Transfer", "开始从容器取出");
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Transfer", "玩家不存在");
                return;
            }
            
            // 获取容器中的物品
            List<ItemStack> containerItems = new ArrayList<>();
            for (int i = 0; i < container.size(); i++) {
                ItemStack stack = container.getStack(i);
                if (!stack.isEmpty()) {
                    containerItems.add(stack);
                }
            }
            
            LogUtil.info("Transfer", "容器中有 " + containerItems.size() + " 个物品可以取出");
            
            // 尝试将物品取出到背包
            for (ItemStack item : containerItems) {
                if (canWithdrawFromContainer(player, item)) {
                    // 执行转移
                    transferItemFromContainer(player, container, item);
                }
            }
            
            LogUtil.info("Transfer", "从容器取出完成");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "从容器取出失败", e);
        }
    }
    
    /**
     * 检查鼠标是否在背包区域
     */
    private boolean isMouseOverPlayerInventory(net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen) {
        // 这里需要根据具体的界面类型来判断
        // 暂时返回false，后续可以根据实际界面布局来实现
        return false;
    }
    
    /**
     * 获取容器库存
     */
    private Inventory getContainerInventory(net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen) {
        ScreenHandler handler = handledScreen.getScreenHandler();
        for (Slot slot : handler.slots) {
            if (slot.inventory != MinecraftClient.getInstance().player.getInventory()) {
                return slot.inventory;
            }
        }
        return null;
    }
    
    /**
     * 检查是否可以存入容器
     */
    private boolean canDepositToContainer(Inventory container, ItemStack item) {
        // 检查容器是否有空间
        for (int i = 0; i < container.size(); i++) {
            ItemStack existing = container.getStack(i);
            if (existing.isEmpty() || (existing.isOf(item.getItem()) && existing.getCount() < existing.getMaxCount())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否可以从容器取出
     */
    private boolean canWithdrawFromContainer(ClientPlayerEntity player, ItemStack item) {
        // 检查背包是否有空间
        for (int i = 9; i < 36; i++) {
            ItemStack existing = player.getInventory().getStack(i);
            if (existing.isEmpty() || (existing.isOf(item.getItem()) && existing.getCount() < existing.getMaxCount())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 将物品转移到容器
     */
    private void transferItemToContainer(ClientPlayerEntity player, Inventory container, ItemStack item) {
        // 这里需要实现具体的转移逻辑
        // 暂时记录日志
        LogUtil.info("Transfer", "转移物品到容器: " + item.getName().getString());
    }
    
    /**
     * 从容器取出物品
     */
    private void transferItemFromContainer(ClientPlayerEntity player, Inventory container, ItemStack item) {
        // 这里需要实现具体的转移逻辑
        // 暂时记录日志
        LogUtil.info("Transfer", "从容器取出物品: " + item.getName().getString());
    }
}
