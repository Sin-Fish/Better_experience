package com.aeolyn.better_experience.inventory.handler;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategy;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategyFactory;
import com.aeolyn.better_experience.inventory.strategy.SortStrategy;
import com.aeolyn.better_experience.inventory.strategy.SortStrategyFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * 创造模式处理器
 * 实现创造模式特有的排序逻辑
 */
public class CreativeModeHandler implements GameModeHandler {
    
    @Override
    public void performSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        Inventory inventory = player.getInventory();
        
        // 收集当前主背包状态（9-35槽位）
        List<net.minecraft.item.ItemStack> currentItems = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            currentItems.add(inventory.getStack(i).copy());
        }
        
        int nonEmptyCount = (int) currentItems.stream().filter(s -> !s.isEmpty()).count();
        LogUtil.info("Inventory", "创造模式：收集到 " + nonEmptyCount + " 个非空物品");

        if (nonEmptyCount == 0) {
            LogUtil.info("Inventory", "创造模式：背包为空，无需整理");
            return;
        }

        if (mergeFirst) {
            // 合并模式：先合并相同物品，再排序
            performMergeSort(player, currentItems, sortMode);
        } else {
            // 普通模式：直接排序
            performSimpleSort(player, currentItems, sortMode);
        }
    }
    
    @Override
    public void performMergeSort(ClientPlayerEntity player, List<net.minecraft.item.ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始执行创造模式合并排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 第一步：合并相同物品
        mergeSameItems(player, strategy, mainSlots);
        
        // 第二步：重新收集物品状态
        List<net.minecraft.item.ItemStack> mergedItems = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            mergedItems.add(player.getInventory().getStack(i).copy());
        }
        
        // 第三步：对合并后的物品进行排序
        performSimpleSort(player, mergedItems, sortMode);
        
        LogUtil.info("Inventory", "创造模式合并排序完成");
    }
    
    @Override
    public void performSimpleSort(ClientPlayerEntity player, List<net.minecraft.item.ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始执行创造模式简单排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 获取排序比较器
        SortStrategy sortStrategy = SortStrategyFactory.getStrategy(sortMode);
        LogUtil.info("Inventory", "使用排序策略: " + sortStrategy.getName());
        
        // 使用选择排序算法：找到整个背包中最应该靠前的物品
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            net.minecraft.item.ItemStack stackI = slotI.getStack();
            
            // 如果当前位置为空，找到后面最应该靠前的非空物品
            if (stackI.isEmpty()) {
                int bestEmptyIndex = -1;
                net.minecraft.item.ItemStack bestEmptyStack = null;
                
                // 找到后面最应该靠前的物品
                for (int j = i + 1; j < mainSlots.size(); j++) {
                    Slot slotJ = mainSlots.get(j);
                    net.minecraft.item.ItemStack stackJ = slotJ.getStack();
                    
                    if (!stackJ.isEmpty()) {
                        if (bestEmptyStack == null || sortStrategy.compare(stackJ, bestEmptyStack) < 0) {
                            bestEmptyIndex = j;
                            bestEmptyStack = stackJ;
                        }
                    }
                }
                
                // 如果找到了物品，移动到当前位置
                if (bestEmptyIndex != -1) {
                    Slot slotBest = mainSlots.get(bestEmptyIndex);
                    strategy.moveItem(player, slotBest, slotI);
                    LogUtil.info("Inventory", "移动槽位 " + bestEmptyIndex + " 到槽位 " + i + ": " + bestEmptyStack.getName().getString());
                }
                continue;
            }
            
            // 当前位置有物品，找到整个背包中最应该靠前的物品
            int bestIndex = i;
            net.minecraft.item.ItemStack bestStack = stackI;
            
            // 从当前位置开始，找到最应该靠前的物品
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                net.minecraft.item.ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 使用策略比较物品，找到最应该靠前的
                if (sortStrategy.compare(stackJ, bestStack) < 0) {
                    bestIndex = j;
                    bestStack = stackJ;
                }
            }
            
            // 如果找到了更靠前的物品，进行交换
            if (bestIndex != i) {
                Slot slotBest = mainSlots.get(bestIndex);
                strategy.swapSlots(player, slotI, slotBest);
                LogUtil.info("Inventory", "交换槽位 " + i + " 和 " + bestIndex + ": " + 
                    stackI.getName().getString() + " <-> " + bestStack.getName().getString());
            }
        }
        
        LogUtil.info("Inventory", "创造模式简单排序完成");
    }
    
    /**
     * 合并相同物品
     */
    private void mergeSameItems(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots) {
        LogUtil.info("Inventory", "开始合并相同物品");
        
        // 使用简单的双指针方法合并相同物品
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            net.minecraft.item.ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                net.minecraft.item.ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 检查是否是相同物品
                if (stackI.isOf(stackJ.getItem())) {
                    // 尝试堆叠
                    if (strategy.canStackItems(stackI, stackJ)) {
                        strategy.stackItem(player, slotJ, slotI);
                        LogUtil.info("Inventory", "合并槽位 " + j + " 到槽位 " + i);
                    }
                }
            }
        }
        
        LogUtil.info("Inventory", "相同物品合并完成");
    }
    
    /**
     * 获取主背包槽位列表
     */
    private List<Slot> getMainInventorySlots(ClientPlayerEntity player) {
        List<Slot> mainSlots = new ArrayList<>();
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.inventory == player.getInventory() && slot.getIndex() >= 9 && slot.getIndex() < 36) {
                mainSlots.add(slot);
            }
        }
        mainSlots.sort(java.util.Comparator.comparingInt(Slot::getIndex));
        return mainSlots;
    }
}
