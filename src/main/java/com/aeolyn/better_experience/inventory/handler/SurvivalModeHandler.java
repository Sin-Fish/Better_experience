package com.aeolyn.better_experience.inventory.handler;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategy;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategyFactory;
import com.aeolyn.better_experience.inventory.strategy.SortStrategy;
import com.aeolyn.better_experience.inventory.strategy.SortStrategyFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * 生存模式处理器
 * 实现生存模式特有的排序逻辑
 */
public class SurvivalModeHandler implements GameModeHandler {
    
    @Override
    public void performSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        LogUtil.info("Inventory", "生存模式：使用PICKUP优化排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        if (mergeFirst) {
            // 生存模式合并：使用PICKUP的堆叠特性
            performMergeSort(player, strategy, mainSlots, sortMode);
        } else {
            // 生存模式排序：使用PICKUP的交换特性
            performSimpleSort(player, strategy, mainSlots, sortMode);
        }
    }
    
    @Override
    public void performMergeSort(ClientPlayerEntity player, List<net.minecraft.item.ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始合并排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 第一步：使用PICKUP的堆叠特性合并相同物品
        // 从前往后遍历，将相同物品堆叠到前面的槽位
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            net.minecraft.item.ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            // 从当前位置开始，向后查找相同物品进行堆叠
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                net.minecraft.item.ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 检查是否是相同物品且可以堆叠
                if (stackI.isOf(stackJ.getItem()) && strategy.canStackItems(stackI, stackJ)) {
                    strategy.stackItem(player, slotJ, slotI);
                    LogUtil.info("Inventory", "生存模式合并: 槽位 " + j + " -> " + i);
                }
            }
        }
        
        // 第二步：对合并后的物品进行排序
        performSimpleSort(player, strategy, mainSlots, sortMode);
        
        LogUtil.info("Inventory", "生存模式合并排序完成");
    }
    
    @Override
    public void performSimpleSort(ClientPlayerEntity player, List<net.minecraft.item.ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始选择排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 获取排序比较器
        SortStrategy sortStrategy = SortStrategyFactory.getStrategy(sortMode);
        LogUtil.info("Inventory", "生存模式使用排序策略: " + sortStrategy.getName());
        
        // 使用选择排序算法，但优化PICKUP操作
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
                    LogUtil.info("Inventory", "生存模式移动: 槽位 " + bestEmptyIndex + " -> " + i + ": " + bestEmptyStack.getName().getString());
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
                LogUtil.info("Inventory", "生存模式交换: 槽位 " + i + " <-> " + bestIndex + ": " + 
                    stackI.getName().getString() + " <-> " + bestStack.getName().getString());
            }
        }
        
        LogUtil.info("Inventory", "生存模式选择排序完成");
    }
    
    /**
     * 生存模式合并排序：利用PICKUP的堆叠特性
     */
    private void performMergeSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始合并排序");
        
        // 第一步：使用PICKUP的堆叠特性合并相同物品
        // 从前往后遍历，将相同物品堆叠到前面的槽位
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            net.minecraft.item.ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            // 从当前位置开始，向后查找相同物品进行堆叠
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                net.minecraft.item.ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 检查是否是相同物品且可以堆叠
                if (stackI.isOf(stackJ.getItem()) && strategy.canStackItems(stackI, stackJ)) {
                    strategy.stackItem(player, slotJ, slotI);
                    LogUtil.info("Inventory", "生存模式合并: 槽位 " + j + " -> " + i);
                }
            }
        }
        
        // 第二步：对合并后的物品进行排序
        performSimpleSort(player, strategy, mainSlots, sortMode);
        
        LogUtil.info("Inventory", "生存模式合并排序完成");
    }
    
    /**
     * 生存模式选择排序：利用PICKUP的交换特性
     */
    private void performSimpleSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始选择排序");
        
        // 获取排序比较器
        SortStrategy sortStrategy = SortStrategyFactory.getStrategy(sortMode);
        LogUtil.info("Inventory", "生存模式使用排序策略: " + sortStrategy.getName());
        
        // 使用选择排序算法，但优化PICKUP操作
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
                    LogUtil.info("Inventory", "生存模式移动: 槽位 " + bestEmptyIndex + " -> " + i + ": " + bestEmptyStack.getName().getString());
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
                LogUtil.info("Inventory", "生存模式交换: 槽位 " + i + " <-> " + bestIndex + ": " + 
                    stackI.getName().getString() + " <-> " + bestStack.getName().getString());
            }
        }
        
        LogUtil.info("Inventory", "生存模式选择排序完成");
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
