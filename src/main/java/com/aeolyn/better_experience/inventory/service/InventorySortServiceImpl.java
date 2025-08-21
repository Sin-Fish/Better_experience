package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;

import com.aeolyn.better_experience.inventory.core.ItemMoveStrategy;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategyFactory;
import com.aeolyn.better_experience.inventory.core.SortComparatorFactory;
import com.aeolyn.better_experience.inventory.util.MouseSlotUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 背包排序服务实现
 * 将原有的完整逻辑迁移到新的架构中
 */
public class InventorySortServiceImpl implements InventorySortService {
    
    public InventorySortServiceImpl() {
    }
    
    @Override
    public void sortInventory(InventorySortConfig.SortMode sortMode) {
        sortInventory(sortMode, false);
    }
    
    @Override
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        sortInventory(sortMode, mergeFirst, SortComparatorFactory.createComparator(sortMode));
    }
    
    @Override
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        
        // 简化的游戏模式判断：只有创造模式+背包模式才用创造排序
        boolean isCreative = player.getAbilities().creativeMode;
        boolean isPlayerInventory = true; // 在背包界面调用
        
        if (isCreative && isPlayerInventory) {
            performCreativeSort(player, sortMode, mergeFirst, comparator);
        } else {
            performSurvivalSort(player, sortMode, mergeFirst, comparator);
        }
    }
    
    @Override
    public void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        try {
            LogUtil.info("Inventory", "开始整理容器，排序模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Inventory", "玩家不存在，无法整理容器");
                return;
            }

            // 收集容器当前状态
            List<ItemStack> current = new ArrayList<>(container.size());
            for (int i = 0; i < container.size(); i++) {
                current.add(container.getStack(i).copy());
            }
            int nonEmpty = (int) current.stream().filter(s -> !s.isEmpty()).count();
            LogUtil.info("Inventory", "收集容器非空物品: " + nonEmpty);

            if (nonEmpty == 0) {
                LogUtil.info("Inventory", "容器为空，无需整理");
                return;
            }

            List<ItemStack> desired;
            if (mergeFirst) {
                // 合并模式：先合并相同物品，再排序
                desired = mergeAndSortItems(current, SortComparatorFactory.createComparator(sortMode));
            } else {
                // 普通模式：直接排序，保持原堆叠
                desired = current.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).collect(Collectors.toList());
                sortItems(desired, SortComparatorFactory.createComparator(sortMode));
            }
            while (desired.size() < container.size()) desired.add(ItemStack.EMPTY);

            // 容器排序：使用PICKUP操作在容器内部进行排序，不使用QUICK_MOVE避免跨容器移动
            if (mergeFirst) {
                // 使用PICKUP操作进行合并和排序
                performContainerSortWithPickupInternal(player, container, current, desired);
            } else {
                // 使用PICKUP操作进行排序
                performContainerReorderWithPickupInternal(player, container, current, desired);
            }
            LogUtil.info("Inventory", "容器整理完成，模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "整理容器失败", e);
        }
    }
    
    @Override
    public void sortContainer(Inventory container) {
        sortContainer(container, InventorySortConfig.SortMode.NAME, false);
    }
    
    @Override
    public void smartSortByMousePosition(InventorySortConfig.SortMode sortMode) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                LogUtil.info("Inventory", "客户端不存在，跳过智能排序");
                return;
            }
            
            // 添加详细的界面类型调试信息
            LogUtil.info("Inventory", "智能排序开始 - 当前界面: " + 
                (client.currentScreen != null ? client.currentScreen.getClass().getSimpleName() : "null"));
            
            // 检查是否在背包界面
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                LogUtil.info("Inventory", "在背包界面，执行背包排序");
                // 启用合并模式：先用PICKUP堆叠再排序
                sortInventory(InventorySortConfig.SortMode.NAME, true);
                return;
            }
            
            // 检查是否在容器界面
            if (!(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
                LogUtil.info("Inventory", "R键按下，但没有打开库存界面，跳过智能排序");
                return;
            }
            
            // 获取缩放后的鼠标位置
            double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
            
            LogUtil.info("Inventory", "鼠标位置: ({}, {})", mouseX, mouseY);
            
            // 获取当前屏幕
            net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
            
            // 尝试获取槽位
            net.minecraft.screen.slot.Slot slot = MouseSlotUtil.getSlotAtPosition(handledScreen, mouseX, mouseY);
            
            if (slot == null) {
                LogUtil.warn("Inventory", "无法获取槽位，排序跳过");
                return;
            }
            
            // 根据当前界面类型决定排序策略
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // 背包界面：根据游戏模式选择排序策略
                LogUtil.info("Inventory", "背包界面：根据游戏模式选择排序策略");
                sortInventory(sortMode, true);
            } else {
                // 容器界面：统一使用PICKUP操作，不区分创造/生存模式
                LogUtil.info("Inventory", "容器界面：统一使用PICKUP操作排序");
                
                // 判断鼠标在哪个位置，确定整理哪个容器
                net.minecraft.inventory.Inventory inventory = slot.inventory;
                boolean isPlayerInventory = inventory == client.player.getInventory();
                LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 环境: " + (isPlayerInventory ? "玩家背包" : "容器"));
                
                if (isPlayerInventory) {
                    // 鼠标在背包槽位上，整理背包（容器界面中统一用PICKUP）
                    LogUtil.info("Inventory", "容器界面中鼠标在背包槽位上，整理背包（统一用PICKUP）");
                    List<Slot> playerSlots = getMainInventorySlots(client.player);
                    performUniversalSort(client.player, playerSlots, InventorySortConfig.SortMode.NAME, true);
                } else {
                    // 鼠标在容器槽位上，整理容器（统一用PICKUP）
                    LogUtil.info("Inventory", "容器界面中鼠标在容器槽位上，整理容器（统一用PICKUP）");
                    List<Slot> containerSlots = getContainerSlots(client.player, inventory);
                    LogUtil.info("Inventory", "获取到容器槽位数量: " + containerSlots.size());
                    performUniversalSort(client.player, containerSlots, InventorySortConfig.SortMode.NAME, true);
                }
            }
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "智能排序失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        simpleSelectionSort(sortMode, mergeFirst, SortComparatorFactory.createComparator(sortMode));
    }
    
    @Override
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 使用现有的简单选择排序逻辑，但传入自定义比较器
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        
        // 简化的游戏模式判断
        boolean isCreative = player.getAbilities().creativeMode;
        boolean isPlayerInventory = true;
        
        if (isCreative && isPlayerInventory) {
            performCreativeSimpleSort(player, sortMode, mergeFirst, comparator);
        } else {
            performSurvivalSimpleSort(player, sortMode, mergeFirst, comparator);
        }
    }
    
    @Override
    public void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 容器排序使用统一的逻辑，不区分创造/生存模式
        try {
            LogUtil.info("Inventory", "开始整理容器，排序模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Inventory", "玩家不存在，无法整理容器");
                return;
            }

            // 收集容器当前状态
            List<ItemStack> current = new ArrayList<>(container.size());
            for (int i = 0; i < container.size(); i++) {
                current.add(container.getStack(i).copy());
            }
            int nonEmpty = (int) current.stream().filter(s -> !s.isEmpty()).count();
            LogUtil.info("Inventory", "收集容器非空物品: " + nonEmpty);

            if (nonEmpty == 0) {
                LogUtil.info("Inventory", "容器为空，无需整理");
                return;
            }

            List<ItemStack> desired;
            if (mergeFirst) {
                // 合并模式：先合并相同物品，再排序
                desired = mergeAndSortItems(current, comparator);
            } else {
                // 普通模式：直接排序，保持原堆叠
                desired = current.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).collect(Collectors.toList());
                sortItems(desired, comparator);
            }
            while (desired.size() < container.size()) desired.add(ItemStack.EMPTY);

            // 容器排序：使用PICKUP操作在容器内部进行排序
            if (mergeFirst) {
                performContainerSortWithPickupInternal(player, container, current, desired);
                        } else {
                performContainerReorderWithPickupInternal(player, container, current, desired);
            }
            LogUtil.info("Inventory", "容器整理完成，模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "整理容器失败", e);
        }
    }
    
    // 新增的私有方法
    private void performCreativeSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 使用通用排序方法替代 CreativeModeHandler
        List<Slot> mainSlots = getMainInventorySlots(player);
        performUniversalSort(player, mainSlots, sortMode, mergeFirst);
    }
    
    private void performSurvivalSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 使用通用排序方法替代 SurvivalModeHandler
        List<Slot> mainSlots = getMainInventorySlots(player);
        performUniversalSort(player, mainSlots, sortMode, mergeFirst);
    }
    
    private void performCreativeSimpleSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 使用通用简单排序方法替代 CreativeModeHandler
        List<Slot> mainSlots = getMainInventorySlots(player);
        performUniversalSort(player, mainSlots, sortMode, mergeFirst);
    }
    
    private void performSurvivalSimpleSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        // 使用通用简单排序方法替代 SurvivalModeHandler
        List<Slot> mainSlots = getMainInventorySlots(player);
        performUniversalSort(player, mainSlots, sortMode, mergeFirst);
    }
    
    // 辅助方法：使用自定义比较器排序
    private void sortItems(List<ItemStack> items, Comparator<ItemStack> comparator) {
        items.sort(comparator);
    }
    

    

    

    
    /**
     * 简单的合并和排序逻辑
     * 使用插入排序算法，先合并相同物品，再按比较器排序
     */
    private List<ItemStack> mergeAndSortItems(List<ItemStack> items, Comparator<ItemStack> comparator) {
        LogUtil.info("Inventory", "开始合并和排序物品，原始物品数: " + items.stream().filter(s -> !s.isEmpty()).count());
        
        // 创建工作副本
        List<ItemStack> workingItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                workingItems.add(item.copy());
            }
        }
        
        // 第一步：合并相同物品
        mergeSameItems(workingItems);
        
        // 第二步：按比较器排序
        sortItems(workingItems, comparator);
        
        // 第三步：填充到原始大小
        while (workingItems.size() < items.size()) {
            workingItems.add(ItemStack.EMPTY);
        }
        
        LogUtil.info("Inventory", "合并和排序完成，最终物品数: " + workingItems.stream().filter(s -> !s.isEmpty()).count());
        return workingItems;
    }
    
    /**
     * 合并相同物品
     */
    private void mergeSameItems(List<ItemStack> items) {
        if (items.isEmpty()) return;
        
        // 使用Map来合并相同物品
        Map<String, ItemStack> mergedItems = new HashMap<>();
        
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            
            String key = getItemKey(item);
            ItemStack existing = mergedItems.get(key);
            
            if (existing == null) {
                // 新物品，直接添加
                mergedItems.put(key, item.copy());
            } else {
                // 相同物品，尝试合并
                int maxStack = item.getMaxCount();
                int currentCount = existing.getCount();
                int newCount = item.getCount();
                int totalCount = currentCount + newCount;
                
                if (totalCount <= maxStack) {
                    // 可以完全合并
                    existing.setCount(totalCount);
                } else {
                    // 部分合并，剩余部分保持原样
                    existing.setCount(maxStack);
                    // 这里可以处理溢出，但为了简单起见，我们暂时忽略
                    LogUtil.info("Inventory", "物品 " + item.getName().getString() + " 合并后超出最大堆叠，保持原样");
                }
            }
        }
        
        // 清空原列表并添加合并后的物品
        items.clear();
        items.addAll(mergedItems.values());
        
        LogUtil.info("Inventory", "合并完成，合并后物品数: " + items.size());
    }
    

    

    
    /**
     * 获取物品的唯一键，用于堆叠判断
     * 目前使用简化的键，仅包含物品ID
     * 后续可以扩展以包含NBT数据
     */
    private String getItemKey(ItemStack stack) {
        if (stack.isEmpty()) return "empty";
        
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        
        // 目前使用简化的键，仅包含物品ID
        // 后续可以扩展以包含NBT数据，确保相同物品的准确识别
        return itemId;
    }
    
    /**
     * 获取物品的显示名称（用于日志）
     */
    private String getItemDisplayName(ItemStack stack) {
        if (stack.isEmpty()) return "空";
        return stack.getName().getString();
    }
    

    
    /**
     * 比较两个物品是否相同类型（不考虑数量）
     */
    private boolean areStacksEqualType(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        
        // Only check if items are the same type
        return a.isOf(b.getItem());
    }

    /**
     * 严格比较两个物品（物品ID与数量）。二者都为空视为相等
     */
    private boolean areStacksEqualExact(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        
        // Check if items are the same type and count
        return a.isOf(b.getItem()) && a.getCount() == b.getCount();
    }
    
    /**
     * 使用PICKUP操作在容器内部进行合并和排序（内部实现）
     */
    private void performContainerSortWithPickupInternal(ClientPlayerEntity player, Inventory container, List<ItemStack> current, List<ItemStack> desired) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            throw new IllegalStateException("interactionManager 不可用");
        }
        
        ScreenHandler handler = player.currentScreenHandler;
        int syncId = handler.syncId;
        
        // 获取容器槽位ID
        List<Integer> slotIndices = getSlotIndicesForInventory(handler, container);
        
        if (slotIndices.size() != container.size()) {
            throw new IllegalStateException("意外的容器槽位数量: " + slotIndices.size() + " vs " + container.size());
        }
        
        LogUtil.info("Inventory", "开始使用 PICKUP 操作在容器内部进行合并和排序");
        
        // 使用PICKUP操作在容器内部进行排序，避免跨容器移动
        for (int i = 0; i < container.size(); i++) {
            ItemStack want = desired.get(i);
            ItemStack have = current.get(i);
            
            if (areStacksEqualExact(have, want)) {
                continue; // 已经正确
            }
            
            if (!want.isEmpty()) {
                // 需要在这个位置放置物品
                if (have.isEmpty() || !areStacksEqualType(have, want)) {
                    // 需要从容器内其他地方移动物品到这里
                    for (int j = i + 1; j < container.size(); j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            // 找到了相同类型的物品，使用PICKUP交换
                            int sourceSlot = slotIndices.get(j);
                            int targetSlot = slotIndices.get(i);
                            
                            // 使用PICKUP操作交换槽位
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            
                            LogUtil.info("Inventory", "容器 PICKUP 交换: 槽位 " + sourceSlot + " <-> " + targetSlot + " (移动物品到目标位置)");
                            
                            // 更新跟踪状态
                            current.set(i, candidate.copy());
                            current.set(j, have.copy());
                            break;
                        }
                    }
                } else if (have.getCount() < want.getCount()) {
                    // 需要从容器内其他地方补充物品到这里
                    int needed = want.getCount() - have.getCount();
                    int maxStack = want.getMaxCount();
                    int spaceInTarget = maxStack - have.getCount();
                    int canTransfer = Math.min(needed, spaceInTarget);
                    
                    for (int j = i + 1; j < container.size(); j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            int transferAmount = Math.min(canTransfer, candidate.getCount());
                            if (transferAmount > 0) {
                                int sourceSlot = slotIndices.get(j);
                                int targetSlot = slotIndices.get(i);
                                
                                // 使用PICKUP操作进行堆叠
                                client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                                client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                                
                                LogUtil.info("Inventory", "容器 PICKUP 堆叠: 槽位 " + sourceSlot + " -> " + targetSlot + " (补充 " + transferAmount + " 个物品)");
                                
                                // 更新跟踪状态
                                have.setCount(have.getCount() + transferAmount);
                                candidate.setCount(candidate.getCount() - transferAmount);
                                if (candidate.getCount() <= 0) {
                                    current.set(j, ItemStack.EMPTY);
                                }
                                canTransfer -= transferAmount;
                                if (canTransfer <= 0) break;
                            }
                        }
                    }
                }
            } else if (!have.isEmpty()) {
                // 这个位置应该是空的，但当前有物品，需要找到正确的目标位置
                // 根据排序规则，找到这个物品应该放在哪里
                int correctPosition = findCorrectPositionForItem(have, desired, i);
                if (correctPosition != i && correctPosition < container.size()) {
                    // 如果找到了正确的位置，移动到那里
                    int sourceSlot = slotIndices.get(i);
                    int targetSlot = slotIndices.get(correctPosition);
                    
                    // 使用PICKUP操作移动物品
                    client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                    client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                    
                    LogUtil.info("Inventory", "容器 PICKUP 移动: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动到正确位置)");
                    
                    // 更新跟踪状态
                    current.set(correctPosition, have.copy());
                    current.set(i, ItemStack.EMPTY);
                } else {
                    // 如果没找到正确位置，移动到容器末尾的空位
                    for (int j = container.size() - 1; j > i; j--) {
                        if (current.get(j).isEmpty()) {
                            int sourceSlot = slotIndices.get(i);
                            int targetSlot = slotIndices.get(j);
                            
                            // 使用PICKUP操作移动物品
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                            
                            LogUtil.info("Inventory", "容器 PICKUP 移动: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动到末尾空位)");
                            
                            // 更新跟踪状态
                            current.set(j, have.copy());
                            current.set(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
        
        LogUtil.info("Inventory", "容器 PICKUP 合并和排序完成");
    }
    
    /**
     * 使用PICKUP操作在容器内部进行排序（不合并）（内部实现）
     */
    private void performContainerReorderWithPickupInternal(ClientPlayerEntity player, Inventory container, List<ItemStack> current, List<ItemStack> desired) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            throw new IllegalStateException("interactionManager 不可用");
        }
        
        ScreenHandler handler = player.currentScreenHandler;
        int syncId = handler.syncId;
        
        // 获取容器槽位ID
        List<Integer> slotIndices = getSlotIndicesForInventory(handler, container);
        
        if (slotIndices.size() != container.size()) {
            throw new IllegalStateException("意外的容器槽位数量: " + slotIndices.size() + " vs " + container.size());
        }
        
        LogUtil.info("Inventory", "开始使用 PICKUP 操作在容器内部进行排序（不合并）");
        
        // 使用PICKUP操作在容器内部进行排序，避免跨容器移动
        for (int i = 0; i < container.size(); i++) {
            ItemStack want = desired.get(i);
            ItemStack have = current.get(i);
            
            if (areStacksEqualExact(have, want)) {
                continue; // 已经正确
            }
            
            if (!want.isEmpty()) {
                // 需要在这个位置放置物品
                if (have.isEmpty() || !areStacksEqualType(have, want)) {
                    // 需要从容器内其他地方移动物品到这里
                    for (int j = i + 1; j < container.size(); j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            // 找到了相同类型的物品，使用PICKUP交换
                            int sourceSlot = slotIndices.get(j);
                            int targetSlot = slotIndices.get(i);
                            
                            // 使用PICKUP操作交换槽位
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            
                            LogUtil.info("Inventory", "容器 PICKUP 交换: 槽位 " + sourceSlot + " <-> " + targetSlot + " (移动物品到目标位置)");
                            
                            // 更新跟踪状态
                            current.set(i, candidate.copy());
                            current.set(j, have.copy());
                            break;
                        }
                    }
                }
            } else if (!have.isEmpty()) {
                // 这个位置应该是空的，但当前有物品，需要找到正确的目标位置
                // 根据排序规则，找到这个物品应该放在哪里
                int correctPosition = findCorrectPositionForItem(have, desired, i);
                if (correctPosition != i && correctPosition < container.size()) {
                    // 如果找到了正确的位置，移动到那里
                    int sourceSlot = slotIndices.get(i);
                    int targetSlot = slotIndices.get(correctPosition);
                    
                    // 使用PICKUP操作移动物品
                    client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                    client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                    
                    LogUtil.info("Inventory", "容器 PICKUP 移动: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动到正确位置)");
                    
                    // 更新跟踪状态
                    current.set(correctPosition, have.copy());
                    current.set(i, ItemStack.EMPTY);
                } else {
                    // 如果没找到正确位置，移动到容器末尾的空位
                    for (int j = container.size() - 1; j > i; j--) {
                        if (current.get(j).isEmpty()) {
                            int sourceSlot = slotIndices.get(i);
                            int targetSlot = slotIndices.get(j);
                            
                            // 使用PICKUP操作移动物品
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player);
                            client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                            
                            LogUtil.info("Inventory", "容器 PICKUP 移动: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动到末尾空位)");
                            
                            // 更新跟踪状态
                            current.set(j, have.copy());
                            current.set(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
        
        LogUtil.info("Inventory", "容器 PICKUP 排序完成");
    }
    
    /**
     * 为物品找到正确的排序位置
     */
    private int findCorrectPositionForItem(ItemStack item, List<ItemStack> desired, int currentPosition) {
        if (item.isEmpty()) {
            return currentPosition;
        }
        
        // 在desired列表中找到这个物品应该出现的位置
        for (int i = 0; i < desired.size(); i++) {
            ItemStack desiredItem = desired.get(i);
            if (!desiredItem.isEmpty() && areStacksEqualType(item, desiredItem)) {
                return i;
            }
        }
        
        // 如果没找到，返回当前位置
        return currentPosition;
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
     * 获取主背包槽位列表
     */
    private List<Slot> getMainInventorySlots(ClientPlayerEntity player) {
        List<Slot> mainSlots = new ArrayList<>();
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.inventory == player.getInventory() && slot.getIndex() >= 9 && slot.getIndex() < 36) {
                mainSlots.add(slot);
            }
        }
        mainSlots.sort(Comparator.comparingInt(Slot::getIndex));
        return mainSlots;
    }

    /**
     * 获取容器槽位
     */
    private List<Slot> getContainerSlots(ClientPlayerEntity player, net.minecraft.inventory.Inventory container) {
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.inventory == container) {
                containerSlots.add(slot);
            }
        }
        containerSlots.sort(Comparator.comparingInt(Slot::getIndex));
        return containerSlots;
    }

    /**
     * 通用排序方法：可以指定排序范围，统一使用PICKUP操作
     */
    private void performUniversalSort(ClientPlayerEntity player, List<Slot> targetSlots, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        LogUtil.info("Inventory", "通用排序：使用PICKUP操作，排序范围: " + targetSlots.size() + " 个槽位");
        
        // 判断是否为玩家背包排序
        boolean isPlayerInventory = targetSlots.size() > 0 && targetSlots.get(0).inventory == player.getInventory();
        
        ItemMoveStrategy strategy;
        if (isPlayerInventory) {
            // 玩家背包排序：根据游戏模式选择策略
            strategy = ItemMoveStrategyFactory.createStrategy(player);
            LogUtil.info("Inventory", "玩家背包排序：使用" + (player.getAbilities().creativeMode ? "创造" : "生存") + "模式策略");
        } else {
            // 容器排序：强制使用生存策略（PICKUP），确保服务端同步
            strategy = ItemMoveStrategyFactory.createSurvivalStrategy();
            LogUtil.info("Inventory", "容器排序：已强制使用生存模式PICKUP策略");
        }
        
        if (mergeFirst) {
            // 合并：使用PICKUP的堆叠特性
            performUniversalMergeSort(player, strategy, targetSlots, sortMode);
        } else {
            // 排序：使用PICKUP的交换特性
            performUniversalSelectionSort(player, strategy, targetSlots, sortMode);
        }
    }
    
    /**
     * 通用合并排序
     */
    private void performUniversalMergeSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> targetSlots, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "通用合并排序：开始合并相同物品");
        
        // 第一步：合并相同物品
        for (int i = 0; i < targetSlots.size(); i++) {
            Slot slotI = targetSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            for (int j = i + 1; j < targetSlots.size(); j++) {
                Slot slotJ = targetSlots.get(j);
                ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 检查是否是相同物品
                String itemj_name = stackJ.getName().getString();
                String itemi_name = stackI.getName().getString();
                if (stackI.isOf(stackJ.getItem())&&itemj_name.equals(itemi_name)) {
                    // 尝试堆叠
                    if (strategy.canStackItems(stackI, stackJ)) {
                        strategy.stackItem(player, slotJ, slotI);
                        LogUtil.info("Inventory", "合并槽位 " + j + " 到槽位 " + i);
                    }
                }
            }
        }
        
        // 第二步：对合并后的物品进行排序
        performUniversalSelectionSort(player, strategy, targetSlots, sortMode);
        
        LogUtil.info("Inventory", "通用合并排序完成");
    }
    
    /**
     * 通用选择排序
     */
   private void performUniversalSelectionSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> targetSlots, InventorySortConfig.SortMode sortMode) {
    LogUtil.info("Inventory", "通用选择排序：开始排序");
    
    // 使用新的比较器工厂
    Comparator<ItemStack> comparator = SortComparatorFactory.createComparator(sortMode);
    LogUtil.info("Inventory", "使用排序模式: " + sortMode.getDisplayName());
    
    // 使用选择排序算法：找到整个范围内最应该靠前的物品
    for (int i = 0; i < targetSlots.size(); i++) {
        Slot slotI = targetSlots.get(i);
        ItemStack stackI = slotI.getStack();
        
        // 寻找从i位置开始的最佳物品（包括空物品）
        int bestIndex = i;
        ItemStack bestStack = stackI;
        
        for (int j = i + 1; j < targetSlots.size(); j++) {
            Slot slotJ = targetSlots.get(j);
            ItemStack stackJ = slotJ.getStack();
            
            // 空物品总是比非空物品"大"（应该排在后面）
            if (stackJ.isEmpty()) continue;
            
            if (bestStack.isEmpty() || comparator.compare(stackJ, bestStack) < 0) {
                bestStack = stackJ;
                bestIndex = j;
            }
        }
        
        // 如果找到了更好的物品，进行交换或移动
        if (bestIndex != i) {
            if (stackI.isEmpty()) {
                // 当前位置为空，直接移动最佳物品过来
                strategy.moveItem(player, targetSlots.get(bestIndex), slotI);
                LogUtil.info("Inventory", "移动槽位 " + bestIndex + " 到空槽位 " + i);
            } else {
                // 当前位置有物品，交换位置
                strategy.swapSlots(player, slotI, targetSlots.get(bestIndex));
                LogUtil.info("Inventory", "交换槽位 " + i + " 和槽位 " + bestIndex);
            }
        }
    }
    
    LogUtil.info("Inventory", "通用选择排序完成");
}
}
