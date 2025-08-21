package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.util.InventoryStatsUtil;
import com.aeolyn.better_experience.inventory.util.MouseSlotUtil;
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
    
    private final ConfigManager configManager;
    
    public InventoryTransferServiceImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
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
            
            // 获取智能转移配置
            InventorySortConfig.SmartTransferLogic transferLogic = getSmartTransferLogic();
            LogUtil.info("Transfer", "智能转移逻辑: " + transferLogic.getDisplayName());
            
            boolean shouldDeposit;
            Inventory containerInventory = InventoryStatsUtil.getContainerInventory(client.player);
            
            // 记录库存统计信息
            InventoryStatsUtil.logInventoryStats(client.player, containerInventory, "Transfer");
            
            switch (transferLogic) {
                case EMPTY_SLOTS:
                    // 根据空位数量决定转移方向
                    shouldDeposit = shouldDepositByEmptySlots(client.player, containerInventory);
                    LogUtil.info("Transfer", "根据空位数量判断: " + (shouldDeposit ? "存入容器" : "取出到背包"));
                    break;
                    
                case ITEM_COUNT:
                    // 根据物品数量决定转移方向
                    shouldDeposit = shouldDepositByItemCount(client.player, containerInventory);
                    LogUtil.info("Transfer", "根据物品数量判断: " + (shouldDeposit ? "存入容器" : "取出到背包"));
                    break;
                    
                default:
                case MOUSE_POSITION:
                    // 根据鼠标位置决定转移方向
                    Slot slot = MouseSlotUtil.getSlotAtMouse(handledScreen);
                    if (slot == null) {
                        LogUtil.warn("Transfer", "无法获取鼠标下的槽位");
                        return;
                    }
                    
                    LogUtil.info("Transfer", "获取到槽位: ID=" + slot.id + ", Index=" + slot.getIndex() + 
                        ", Inventory=" + slot.inventory.getClass().getSimpleName());
                    
                    // 判断鼠标在哪个区域
                    boolean isPlayerInventory = slot.inventory == client.player.getInventory();
                    shouldDeposit = isPlayerInventory;
                    LogUtil.info("Transfer", "根据鼠标位置判断: 槽位ID=" + slot.id + ", 是否玩家背包=" + isPlayerInventory);
                    break;
            }
            
            if (shouldDeposit) {
                // 执行存入操作（背包 -> 容器）
                LogUtil.info("Transfer", "执行存入操作：背包 -> 容器");
                depositAllFromPlayerInventory();
            } else {
                // 执行取出操作（容器 -> 背包）
                LogUtil.info("Transfer", "执行取出操作：容器 -> 背包");
                withdrawAllFromContainer(containerInventory);
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
     * 获取智能转移配置逻辑
     */
    private InventorySortConfig.SmartTransferLogic getSmartTransferLogic() {
        try {
            InventorySortConfig config = configManager.getConfig(InventorySortConfig.class);
            if (config != null && config.getSmartTransferLogic() != null) {
                return config.getSmartTransferLogic();
            }
        } catch (Exception e) {
            LogUtil.warn("Transfer", "获取智能转移配置失败，使用默认值: " + e.getMessage());
        }
        // 默认值
        return InventorySortConfig.SmartTransferLogic.MOUSE_POSITION;
    }
    
    /**
     * 根据空位数量决定是否应该存入容器
     * @param player 玩家
     * @param containerInventory 容器库存
     * @return true 如果应该存入容器，false 如果应该取出到背包
     */
    private boolean shouldDepositByEmptySlots(ClientPlayerEntity player, Inventory containerInventory) {
        if (containerInventory == null) {
            return false; // 没有容器，默认不存入
        }
        
        int playerEmptySlots = InventoryStatsUtil.countPlayerEmptySlots(player);
        int containerEmptySlots = InventoryStatsUtil.countContainerEmptySlots(containerInventory);
        
        LogUtil.info("Transfer", "空位数量比较 - 背包空位: " + playerEmptySlots + ", 容器空位: " + containerEmptySlots);
        
        // 如果容器空位更多，存入容器；如果背包空位更多，取出到背包
        return containerEmptySlots > playerEmptySlots;
    }
    
    /**
     * 根据物品数量决定是否应该存入容器
     * @param player 玩家
     * @param containerInventory 容器库存
     * @return true 如果应该存入容器，false 如果应该取出到背包
     */
    private boolean shouldDepositByItemCount(ClientPlayerEntity player, Inventory containerInventory) {
        if (containerInventory == null) {
            return false; // 没有容器，默认不存入
        }
        
        int playerItemCount = InventoryStatsUtil.countPlayerItems(player);
        int containerItemCount = InventoryStatsUtil.countContainerItems(containerInventory);
        
        LogUtil.info("Transfer", "物品数量比较 - 背包物品: " + playerItemCount + ", 容器物品: " + containerItemCount);
        
        // 如果背包物品更多，存入容器；如果容器物品更多，取出到背包
        return playerItemCount > containerItemCount;
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
     * @param container 容器库存
     * 
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
