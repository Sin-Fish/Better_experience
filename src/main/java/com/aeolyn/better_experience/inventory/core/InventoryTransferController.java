package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能转移控制器
 * 专门负责物品的智能转移功能，包括一键存入/取出容器
 */
public class InventoryTransferController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Transfer");
    private static volatile InventoryTransferController instance;
    
    private InventoryTransferController() {}
    
    /**
     * 获取单例实例
     */
    public static InventoryTransferController getInstance() {
        if (instance == null) {
            synchronized (InventoryTransferController.class) {
                if (instance == null) {
                    instance = new InventoryTransferController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化智能转移控制器
     */
    public static void initialize() {
        LogUtil.info("Transfer", "初始化智能转移控制器");
        getInstance();
    }
    
    /**
     * 智能一键存入/取出功能（Shift+R）
     * 自动判断是存入还是取出，并修复堆叠bug
     */
    public void smartTransferItems() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) {
                LogUtil.warn("Transfer", "玩家不存在，无法执行智能转移");
                return;
            }
            
            // 检查是否在容器界面
            if (!(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
                LogUtil.info("Transfer", "不在容器界面，跳过智能转移");
                return;
            }
            
            net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
            
            // 获取容器库存
            net.minecraft.inventory.Inventory containerInventory = getContainerInventory(handledScreen);
            if (containerInventory == null) {
                LogUtil.warn("Transfer", "无法获取容器库存");
                return;
            }
            
            // 判断是存入还是取出
            boolean shouldDeposit = shouldDepositToContainer(containerInventory);
            
            if (shouldDeposit) {
                LogUtil.info("Transfer", "执行智能存入操作");
                smartDepositToContainer(containerInventory);
            } else {
                LogUtil.info("Transfer", "执行智能取出操作");
                smartWithdrawFromContainer(containerInventory);
            }
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "智能转移失败", e);
        }
    }
    
    /**
     * 判断是否应该存入容器
     */
    private boolean shouldDepositToContainer(Inventory container) {
        // 简单的启发式判断：如果容器空位较多，倾向于存入
        int containerEmptySlots = 0;
        int playerEmptySlots = 0;
        
        // 统计容器空位
        for (int i = 0; i < container.size(); i++) {
            if (container.getStack(i).isEmpty()) {
                containerEmptySlots++;
            }
        }
        
        // 统计玩家背包空位
        Inventory playerInventory = MinecraftClient.getInstance().player.getInventory();
        for (int i = 9; i < 36; i++) {
            if (playerInventory.getStack(i).isEmpty()) {
                playerEmptySlots++;
            }
        }
        
        // 如果容器空位比玩家背包空位多，倾向于存入
        return containerEmptySlots > playerEmptySlots;
    }
    
    /**
     * 智能存入容器（使用服务端同步的 clickSlot）
     */
    private void smartDepositToContainer(Inventory container) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (player == null || client.interactionManager == null) return;
            
            ScreenHandler handler = player.currentScreenHandler;
            int syncId = handler.syncId;
            
            // 获取玩家背包槽位ID（9-35）
            List<Integer> playerSlotIds = new ArrayList<>();
            for (Slot s : handler.slots) {
                if (s.inventory == player.getInventory() && s.getIndex() >= 9 && s.getIndex() < 36) {
                    playerSlotIds.add(s.id);
                }
            }
            
            // 获取容器槽位ID
            List<Integer> containerSlotIds = getSlotIndicesForInventory(handler, container);
            
            if (containerSlotIds.size() != container.size()) {
                LogUtil.warn("Transfer", "容器槽位数量不匹配: " + containerSlotIds.size() + " vs " + container.size());
                return;
            }
            
            LogUtil.info("Transfer", "开始智能存入容器，使用服务端同步");
            
            // 从背包中查找可以存入的物品
            for (int i = 0; i < playerSlotIds.size(); i++) {
                int playerSlotId = playerSlotIds.get(i);
                ItemStack playerStack = player.getInventory().getStack(i + 9); // 转换为背包索引
                
                if (playerStack.isEmpty()) continue;
                
                LogUtil.info("Transfer", "处理背包槽位 " + (i + 9) + " 的物品: " + playerStack.getName().getString() + " x" + playerStack.getCount());
                
                // 优先查找相同物品的堆叠
                for (int j = 0; j < containerSlotIds.size(); j++) {
                    int containerSlotId = containerSlotIds.get(j);
                    ItemStack containerStack = container.getStack(j);
                    
                    if (canStack(playerStack, containerStack)) {
                        // 相同物品，尝试堆叠
                        int maxStack = Math.min(playerStack.getMaxCount(), containerStack.getMaxCount());
                        int space = maxStack - containerStack.getCount();
                        
                        if (space > 0) {
                            // 使用 QUICK_MOVE 进行堆叠
                            client.interactionManager.clickSlot(syncId, playerSlotId, 0, SlotActionType.QUICK_MOVE, player);
                            LogUtil.info("Transfer", "QUICK_MOVE 堆叠物品: " + playerStack.getName().getString() + " 到容器槽位 " + j);
                            break; // 移动到下一个背包物品
                        }
                    }
                }
                
                // 如果没有找到堆叠位置，查找空位
                boolean movedToEmpty = false;
                for (int j = 0; j < containerSlotIds.size() && !movedToEmpty; j++) {
                    int containerSlotId = containerSlotIds.get(j);
                    ItemStack containerStack = container.getStack(j);
                    
                    if (containerStack.isEmpty()) {
                        // 空位，使用 QUICK_MOVE 移动
                        client.interactionManager.clickSlot(syncId, playerSlotId, 0, SlotActionType.QUICK_MOVE, player);
                        LogUtil.info("Transfer", "QUICK_MOVE 存入物品: " + playerStack.getName().getString() + " 到容器空位 " + j);
                        movedToEmpty = true;
                    }
                }
            }
            
            LogUtil.info("Transfer", "智能存入完成");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "智能存入失败", e);
        }
    }
    
    /**
     * 智能从容器取出（使用服务端同步的 clickSlot）
     */
    private void smartWithdrawFromContainer(Inventory container) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (player == null || client.interactionManager == null) return;
            
            ScreenHandler handler = player.currentScreenHandler;
            int syncId = handler.syncId;
            
            // 获取玩家背包槽位ID（9-35）
            List<Integer> playerSlotIds = new ArrayList<>();
            for (Slot s : handler.slots) {
                if (s.inventory == player.getInventory() && s.getIndex() >= 9 && s.getIndex() < 36) {
                    playerSlotIds.add(s.id);
                }
            }
            
            // 获取容器槽位ID
            List<Integer> containerSlotIds = getSlotIndicesForInventory(handler, container);
            
            if (containerSlotIds.size() != container.size()) {
                LogUtil.warn("Transfer", "容器槽位数量不匹配: " + containerSlotIds.size() + " vs " + container.size());
                return;
            }
            
            LogUtil.info("Transfer", "开始智能从容器取出，使用服务端同步");
            
            // 从容器中查找可以拿取的物品
            for (int i = 0; i < containerSlotIds.size(); i++) {
                int containerSlotId = containerSlotIds.get(i);
                ItemStack containerStack = container.getStack(i);
                
                if (containerStack.isEmpty()) continue;
                
                LogUtil.info("Transfer", "处理容器槽位 " + i + " 的物品: " + containerStack.getName().getString() + " x" + containerStack.getCount());
                
                // 优先查找相同物品的堆叠
                boolean stacked = false;
                for (int j = 0; j < playerSlotIds.size() && !stacked; j++) {
                    int playerSlotId = playerSlotIds.get(j);
                    ItemStack playerStack = player.getInventory().getStack(j + 9); // 转换为背包索引
                    
                    if (canStack(containerStack, playerStack)) {
                        // 相同物品，尝试堆叠
                        int maxStack = Math.min(containerStack.getMaxCount(), playerStack.getMaxCount());
                        int space = maxStack - playerStack.getCount();
                        
                        if (space > 0) {
                            // 使用 QUICK_MOVE 进行堆叠
                            client.interactionManager.clickSlot(syncId, containerSlotId, 0, SlotActionType.QUICK_MOVE, player);
                            LogUtil.info("Transfer", "QUICK_MOVE 堆叠物品: " + containerStack.getName().getString() + " 到背包槽位 " + (j + 9));
                            stacked = true;
                        }
                    }
                }
                
                // 如果没有找到堆叠位置，查找空位
                if (!stacked) {
                    for (int j = 0; j < playerSlotIds.size(); j++) {
                        int playerSlotId = playerSlotIds.get(j);
                        ItemStack playerStack = player.getInventory().getStack(j + 9); // 转换为背包索引
                        
                        if (playerStack.isEmpty()) {
                            // 空位，使用 QUICK_MOVE 移动
                            client.interactionManager.clickSlot(syncId, containerSlotId, 0, SlotActionType.QUICK_MOVE, player);
                            LogUtil.info("Transfer", "QUICK_MOVE 取出物品: " + containerStack.getName().getString() + " 到背包空位 " + (j + 9));
                            break;
                        }
                    }
                }
            }
            
            LogUtil.info("Transfer", "智能取出完成");
            
        } catch (Exception e) {
            LogUtil.error("Transfer", "智能取出失败", e);
        }
    }
    
    /**
     * 检查两个物品是否可以堆叠
     */
    private boolean canStack(ItemStack stack1, ItemStack stack2) {
        return stack1.isOf(stack2.getItem());
    }
    
    /**
     * 获取容器库存
     */
    private net.minecraft.inventory.Inventory getContainerInventory(net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen) {
        try {
            // 方法1: 从 ScreenHandler 获取第一个非玩家背包的库存
            net.minecraft.screen.ScreenHandler handler = handledScreen.getScreenHandler();
            if (handler != null) {
                for (net.minecraft.screen.slot.Slot slot : handler.slots) {
                    if (slot.inventory != null && slot.inventory != MinecraftClient.getInstance().player.getInventory()) {
                        LogUtil.info("Transfer", "从 ScreenHandler 找到容器库存");
                        return slot.inventory;
                    }
                }
            }
            
            // 方法2: 尝试通过反射获取容器库存
            Class<?> screenClass = handledScreen.getClass();
            
            // 常见的容器库存字段名
            String[] possibleFieldNames = {"inventory", "container", "blockInventory", "chestInventory"};
            
            for (String fieldName : possibleFieldNames) {
                try {
                    java.lang.reflect.Field field = screenClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(handledScreen);
                    
                    if (value instanceof net.minecraft.inventory.Inventory) {
                        net.minecraft.inventory.Inventory inventory = (net.minecraft.inventory.Inventory) value;
                        // 确保不是玩家背包
                        if (inventory != MinecraftClient.getInstance().player.getInventory()) {
                            LogUtil.info("Transfer", "找到容器库存字段: " + fieldName);
                            return inventory;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个字段
                }
            }
            
        } catch (Exception e) {
            LogUtil.warn("Transfer", "获取容器库存失败: " + e.getMessage());
        }
        
        LogUtil.warn("Transfer", "无法获取容器库存");
        return null;
    }
    
    /**
     * Get list of slot indices for a specific inventory in the current screen handler.
     */
    private List<Integer> getSlotIndicesForInventory(ScreenHandler handler, Inventory targetInventory) {
        List<Integer> indices = new ArrayList<>();
        for (Slot s : handler.slots) {
            if (s.inventory == targetInventory) {
                indices.add(s.id);
            }
        }
        return indices;
    }
    
    /**
     * 一键存入容器（兼容旧版本调用）
     */
    public void depositToContainer(Inventory container) {
        LogUtil.info("Transfer", "调用兼容方法 depositToContainer，转发到智能转移");
        smartTransferItems();
    }
    
    /**
     * 一键从容器取出（兼容旧版本调用）
     */
    public void withdrawFromContainer(Inventory container) {
        LogUtil.info("Transfer", "调用兼容方法 withdrawFromContainer，转发到智能转移");
        smartTransferItems();
    }
}
