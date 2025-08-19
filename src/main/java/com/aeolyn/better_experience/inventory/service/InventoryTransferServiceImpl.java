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
            LogUtil.info("Transfer", "=== 开始智能转移物品 ===");
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                LogUtil.warn("Transfer", "客户端或玩家不存在");
                return;
            }
            
            LogUtil.info("Transfer", "当前界面: " + (client.currentScreen != null ? client.currentScreen.getClass().getSimpleName() : "null"));
            
            // 检查是否在容器界面
            if (!(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
                LogUtil.info("Transfer", "不在容器界面，跳过智能转移");
                return;
            }
            
            // 获取当前屏幕
            net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
            
            LogUtil.info("Transfer", "容器界面类型: " + handledScreen.getClass().getSimpleName());
            
            // 获取鼠标下的槽位
            Slot slot = getSlotAtMouse(handledScreen);
            if (slot == null) {
                LogUtil.warn("Transfer", "无法获取鼠标下的槽位");
                return;
            }
            
            LogUtil.info("Transfer", "获取到槽位: ID=" + slot.id + ", Index=" + slot.getIndex() + 
                ", Inventory=" + slot.inventory.getClass().getSimpleName());
            
            // 判断鼠标在哪个区域
            boolean isPlayerInventory = slot.inventory == client.player.getInventory();
            LogUtil.info("Transfer", "鼠标位置检测: 槽位ID=" + slot.id + ", 是否玩家背包=" + isPlayerInventory);
            
            if (isPlayerInventory) {
                // 鼠标在背包区域，执行存入操作（背包 -> 容器）
                LogUtil.info("Transfer", "鼠标在背包区域，执行存入操作");
                depositAllFromPlayerInventory();
            } else {
                // 鼠标在容器区域，执行取出操作（容器 -> 背包）
                LogUtil.info("Transfer", "鼠标在容器区域，执行取出操作");
                withdrawAllFromContainer(slot.inventory);
            }
            
            LogUtil.info("Transfer", "=== 智能转移完成 ===");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "智能转移失败", e);
        }
    }
    
    @Override
    public void depositToContainer(Inventory container) {
        depositAllFromPlayerInventory();
    }
    
    @Override
    public void withdrawFromContainer(Inventory container) {
        withdrawAllFromContainer(container);
    }
    
    /**
     * 获取鼠标下的槽位
     * 使用与背包排序相同的逻辑
     */
    private Slot getSlotAtMouse(net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return null;
            }
            
            // 获取缩放后的鼠标位置
            double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
            
            // 使用与背包排序相同的槽位检测逻辑
            return getSlotAtPosition(handledScreen, mouseX, mouseY);
        } catch (Exception e) {
            LogUtil.warn("Transfer", "获取鼠标槽位失败", e);
            return null;
        }
    }
    
    /**
     * 获取指定位置的槽位
     * 复制自 InventorySortServiceImpl 的 getSlotAtPosition 方法
     */
    private net.minecraft.screen.slot.Slot getSlotAtPosition(net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen, double mouseX, double mouseY) {
        try {
            // 尝试通过反射获取槽位
            Class<?> currentClass = handledScreen.getClass();
            while (currentClass != null) {
                String[] possibleMethodNames = {"getSlotAt", "method_5452", "method_2385", "method_1542", "method_64240", "method_2383", "method_64241", "method_2381", "method_2378"};
                for (String methodName : possibleMethodNames) {
                    try {
                        java.lang.reflect.Method getSlotAtMethod = currentClass.getDeclaredMethod(methodName, double.class, double.class);
                        getSlotAtMethod.setAccessible(true);
                        net.minecraft.screen.slot.Slot slot = (net.minecraft.screen.slot.Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                        if (slot != null) {
                            LogUtil.info("Transfer", "成功调用 " + methodName + "，找到槽位: " + slot.id);
                            return slot;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个方法
                    }
                }
                if (currentClass != null) {
                    currentClass = currentClass.getSuperclass();
                }
            }
        } catch (Exception e) {
            LogUtil.warn("Transfer", "获取槽位失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 将背包中的所有物品存入容器
     */
    private void depositAllFromPlayerInventory() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (client == null || player == null || client.interactionManager == null) {
                LogUtil.warn("Transfer", "客户端或交互管理器不可用");
                return;
            }
            
            ScreenHandler handler = player.currentScreenHandler;
            int syncId = handler.syncId;
            
            LogUtil.info("Transfer", "=== 开始将背包物品存入容器 ===");
            LogUtil.info("Transfer", "ScreenHandler类型: " + handler.getClass().getSimpleName());
            LogUtil.info("Transfer", "同步ID: " + syncId);
            
            int processedSlots = 0;
            
            // 遍历背包槽位（9-35），对每个非空槽位执行QUICK_MOVE
            for (Slot slot : handler.slots) {
                if (slot.inventory == player.getInventory() && 
                    slot.getIndex() >= 9 && slot.getIndex() < 36 &&
                    !slot.getStack().isEmpty()) {
                    
                    LogUtil.info("Transfer", "处理背包槽位 " + slot.getIndex() + " (ID: " + slot.id + "): " + slot.getStack().getName().getString());
                    
                    // 执行QUICK_MOVE（从背包到容器）
                    client.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, player);
                    processedSlots++;
                }
            }
            
            LogUtil.info("Transfer", "背包物品存入完成，处理了 " + processedSlots + " 个槽位");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "存入容器失败", e);
        }
    }
    
    /**
     * 将容器中的所有物品取出到背包
     */
    private void withdrawAllFromContainer(Inventory container) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (client == null || player == null || client.interactionManager == null) {
                LogUtil.warn("Transfer", "客户端或交互管理器不可用");
                return;
            }
            
            ScreenHandler handler = player.currentScreenHandler;
            int syncId = handler.syncId;
            
            LogUtil.info("Transfer", "开始将容器物品取出到背包");
            
            // 遍历容器槽位，对每个非空槽位执行QUICK_MOVE
            for (Slot slot : handler.slots) {
                if (slot.inventory == container && !slot.getStack().isEmpty()) {
                    // 执行QUICK_MOVE（从容器到背包）
                    client.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, player);
                    LogUtil.info("Transfer", "QUICK_MOVE 容器槽位 " + slot.getIndex() + ": " + slot.getStack().getName().getString());
                }
            }
            
            LogUtil.info("Transfer", "容器物品取出完成");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "从容器取出失败", e);
        }
    }
}
