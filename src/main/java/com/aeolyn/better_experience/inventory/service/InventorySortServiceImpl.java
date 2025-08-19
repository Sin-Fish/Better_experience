package com.aeolyn.better_experience.inventory.service;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.handler.CreativeModeHandler;
import com.aeolyn.better_experience.inventory.handler.GameModeHandler;
import com.aeolyn.better_experience.inventory.handler.SurvivalModeHandler;
import com.aeolyn.better_experience.inventory.strategy.SortStrategy;
import com.aeolyn.better_experience.inventory.strategy.SortStrategyFactory;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategy;
import com.aeolyn.better_experience.inventory.core.ItemMoveStrategyFactory;
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
    
    private final CreativeModeHandler creativeHandler;
    private final SurvivalModeHandler survivalHandler;
    
    public InventorySortServiceImpl() {
        this.creativeHandler = new CreativeModeHandler();
        this.survivalHandler = new SurvivalModeHandler();
    }
    
    @Override
    public void sortInventory(InventorySortConfig.SortMode sortMode) {
        sortInventory(sortMode, false);
    }
    
    @Override
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        
        GameModeHandler handler = getGameModeHandler(player);
        handler.performSort(player, sortMode, mergeFirst);
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
                desired = mergeAndSortItems(current, sortMode);
            } else {
                // 普通模式：直接排序，保持原堆叠
                desired = current.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).collect(Collectors.toList());
                sortItems(desired, sortMode);
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
    public void smartSortByMousePosition() {
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
            net.minecraft.screen.slot.Slot slot = getSlotAtPosition(handledScreen, mouseX, mouseY);
            
            if (slot == null) {
                LogUtil.warn("Inventory", "无法获取槽位，排序跳过");
                return;
            }
            
            // 根据当前界面类型决定排序策略
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // 背包界面：根据游戏模式选择排序策略
                LogUtil.info("Inventory", "背包界面：根据游戏模式选择排序策略");
                sortInventory(InventorySortConfig.SortMode.NAME, true);
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
        sortInventory(sortMode, mergeFirst);
    }
    
    @Override
    public void testShulkerBoxSupport() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.currentScreen == null) {
                LogUtil.info("Inventory", "测试潜影盒支持：客户端或屏幕为空");
                return;
            }
            
            LogUtil.info("Inventory", "=== 潜影盒支持测试开始 ===");
            LogUtil.info("Inventory", "当前界面类型: " + client.currentScreen.getClass().getSimpleName());
            LogUtil.info("Inventory", "当前界面完整类名: " + client.currentScreen.getClass().getName());
            
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen) {
                net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                    (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
                
                LogUtil.info("Inventory", "ScreenHandler类型: " + handledScreen.getScreenHandler().getClass().getSimpleName());
                LogUtil.info("Inventory", "ScreenHandler完整类名: " + handledScreen.getScreenHandler().getClass().getName());
                
                // 检查所有槽位
                LogUtil.info("Inventory", "=== 槽位信息 ===");
                for (int i = 0; i < handledScreen.getScreenHandler().slots.size(); i++) {
                    Slot slot = handledScreen.getScreenHandler().slots.get(i);
                    LogUtil.info("Inventory", "槽位 " + i + ": ID=" + slot.id + 
                        ", Index=" + slot.getIndex() + 
                        ", Inventory=" + slot.inventory.getClass().getSimpleName() +
                        ", 物品=" + (slot.getStack().isEmpty() ? "空" : slot.getStack().getName().getString()));
                }
                
                // 检查是否是潜影盒
                boolean isShulkerBox = client.currentScreen.getClass().getSimpleName().contains("ShulkerBox") ||
                                     handledScreen.getScreenHandler().getClass().getSimpleName().contains("ShulkerBox");
                
                LogUtil.info("Inventory", "是否是潜影盒界面: " + isShulkerBox);
                
                if (isShulkerBox) {
                    LogUtil.info("Inventory", "=== 潜影盒特殊检测 ===");
                    // 尝试直接对潜影盒进行排序
                    try {
                        // 获取潜影盒的容器
                        net.minecraft.inventory.Inventory shulkerInventory = null;
                        for (Slot slot : handledScreen.getScreenHandler().slots) {
                            if (slot.inventory != client.player.getInventory()) {
                                shulkerInventory = slot.inventory;
                                break;
                            }
                        }
                        
                        if (shulkerInventory != null) {
                            LogUtil.info("Inventory", "找到潜影盒容器: " + shulkerInventory.getClass().getSimpleName());
                            LogUtil.info("Inventory", "潜影盒容器大小: " + shulkerInventory.size());
                            
                            // 尝试直接排序
                            sortContainer(shulkerInventory, InventorySortConfig.SortMode.NAME, true);
                            LogUtil.info("Inventory", "潜影盒排序完成");
                        } else {
                            LogUtil.warn("Inventory", "未找到潜影盒容器");
                        }
                    } catch (Exception e) {
                        LogUtil.error("Inventory", "潜影盒排序失败", e);
                    }
                }
            }
            
            LogUtil.info("Inventory", "=== 潜影盒支持测试结束 ===");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "潜影盒支持测试失败", e);
        }
    }
    
    private GameModeHandler getGameModeHandler(ClientPlayerEntity player) {
        if (player.getAbilities().creativeMode) {
            return creativeHandler;
        } else {
            return survivalHandler;
        }
    }
    
    /**
     * 完善的合并排序逻辑
     * 算法：
     * 1. 遍历每个槽位：从前往后处理每个槽位
     * 2. 找到相同物品：当遇到一个物品时，向后查找所有相同类型的物品
     * 3. 统计总数量：计算所有相同物品的总数量
     * 4. 队列处理：将找到的相同物品记录到队列中（位置和数量）
     * 5. 填充策略：使用PICKUP从队头到队尾依次填充，填满64个就移动到下一个位置
     * 6. 清空原位置：将原来分散的物品位置清空
     * 
     * 性能优化：
     * - 预排序相同物品以提高查找效率
     * - 使用HashMap缓存物品键值
     * - 批量处理相同类型的物品
     */
    private List<ItemStack> mergeAndSortItems(List<ItemStack> items, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始合并相同物品，原始物品数: " + items.stream().filter(s -> !s.isEmpty()).count());
        
        // 创建工作副本
        List<ItemStack> workingItems = new ArrayList<>();
        for (ItemStack item : items) {
            workingItems.add(item.copy());
        }
        
        // 性能优化：预排序相同物品以提高查找效率
        Map<String, List<Integer>> itemGroups = new HashMap<>();
        for (int i = 0; i < workingItems.size(); i++) {
            ItemStack stack = workingItems.get(i);
            if (!stack.isEmpty()) {
                String key = getItemKey(stack);
                itemGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        
        LogUtil.info("Inventory", "预分组完成，共 " + itemGroups.size() + " 种不同物品");
        
        // 记录已处理的槽位
        boolean[] processed = new boolean[workingItems.size()];
        int mergeCount = 0; // 记录合并次数
        
        // 从前往后遍历每个槽位
        for (int i = 0; i < workingItems.size(); i++) {
            if (processed[i] || workingItems.get(i).isEmpty()) {
                continue;
            }
            
            ItemStack currentItem = workingItems.get(i);
            String itemKey = getItemKey(currentItem);
            int maxStack = currentItem.getMaxCount();
            
            LogUtil.info("Inventory", "=== 开始处理第 " + (++mergeCount) + " 组物品 ===");
            LogUtil.info("Inventory", "处理槽位 " + i + " 的物品: " + currentItem.getName().getString() + " x" + currentItem.getCount());
            
            // 队列存储所有相同类型的物品：[位置, 数量]
            Queue<int[]> itemQueue = new LinkedList<>();
            List<Integer> originalPositions = new ArrayList<>(); // 记录原始位置用于清空
            List<String> mergeDetails = new ArrayList<>(); // 记录合并详情
            
            // 使用预分组优化查找相同类型的物品
            List<Integer> sameTypeSlots = itemGroups.get(itemKey);
            if (sameTypeSlots != null) {
                for (int slotIndex : sameTypeSlots) {
                    if (!processed[slotIndex] && slotIndex >= i) { // 只处理当前位置及之后的槽位
                        int count = workingItems.get(slotIndex).getCount();
                        itemQueue.offer(new int[]{slotIndex, count});
                        originalPositions.add(slotIndex);
                        processed[slotIndex] = true;
                        mergeDetails.add("槽位" + slotIndex + ":" + count + "个");
                        LogUtil.info("Inventory", "  找到相同物品在槽位 " + slotIndex + ": " + getItemDisplayName(workingItems.get(slotIndex)) + " x" + count);
                    }
                }
            }
            
            // 统计总数量
            int totalCount = itemQueue.stream().mapToInt(arr -> arr[1]).sum();
            LogUtil.info("Inventory", "  合并详情: " + String.join(", ", mergeDetails));
            LogUtil.info("Inventory", "  总数量: " + totalCount + "，最大堆叠: " + maxStack);
            
            // 填充策略：从队头到队尾依次填充，填满64个就移动到下一个位置
            int currentSlot = i;
            int remainingToFill = totalCount;
            int filledSlots = 0;
            
            while (remainingToFill > 0 && currentSlot < workingItems.size()) {
                int toFill = Math.min(remainingToFill, maxStack);
                
                // 创建该槽位的堆叠
                workingItems.set(currentSlot, currentItem.copy());
                workingItems.get(currentSlot).setCount(toFill);
                
                filledSlots++;
                LogUtil.info("Inventory", "    槽位 " + currentSlot + " 放置: " + toFill + " 个 (剩余: " + (remainingToFill - toFill) + ", 已填充槽位: " + filledSlots + ")");
                
                remainingToFill -= toFill;
                currentSlot++;
            }
            
            // 清空原位置：将原来分散的物品位置清空
            int clearedSlots = 0;
            for (int position : originalPositions) {
                if (position >= currentSlot) { // 只清空在我们填充槽位之后的位置
                    workingItems.set(position, ItemStack.EMPTY);
                    clearedSlots++;
                    LogUtil.info("Inventory", "    清空原槽位 " + position);
                }
            }
            
            // 输出合并结果统计
            LogUtil.info("Inventory", "  合并结果: 填充了 " + filledSlots + " 个槽位，清空了 " + clearedSlots + " 个原槽位");
            
            // 如果还有剩余物品，需要处理溢出
            if (remainingToFill > 0) {
                LogUtil.warn("Inventory", "  警告：物品 " + currentItem.getName().getString() + " 还有 " + remainingToFill + " 个无法放置，背包已满");
            }
            
            LogUtil.info("Inventory", "=== 第 " + mergeCount + " 组物品处理完成 ===");
        }
        
        int finalItemCount = (int) workingItems.stream().filter(s -> !s.isEmpty()).count();
        int originalItemCount = (int) items.stream().filter(s -> !s.isEmpty()).count();
        int totalSlotsSaved = originalItemCount - finalItemCount;
        
        LogUtil.info("Inventory", "合并完成统计:");
        LogUtil.info("Inventory", "  原始物品数: " + originalItemCount);
        LogUtil.info("Inventory", "  合并后物品数: " + finalItemCount);
        LogUtil.info("Inventory", "  节省槽位数: " + totalSlotsSaved);
        LogUtil.info("Inventory", "  共处理了 " + mergeCount + " 组物品");
        LogUtil.info("Inventory", "  合并效率: " + String.format("%.1f", (double) totalSlotsSaved / originalItemCount * 100) + "%");
        
        // 对合并后的物品进行排序
        LogUtil.info("Inventory", "开始对合并后的物品进行排序，排序模式: " + sortMode.getDisplayName());
        sortItems(workingItems, sortMode);
        
        // 验证合并结果
        if (!validateMergeResult(items, workingItems)) {
            LogUtil.error("Inventory", "合并结果验证失败，可能存在数据丢失");
        }
        
        return workingItems;
    }
    
    /**
     * 排序物品列表
     */
    private void sortItems(List<ItemStack> items, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始排序物品，排序模式: " + sortMode.getDisplayName() + "，物品数量: " + items.size());
        
        // 暂时使用默认配置，后续会从ConfigManager获取
        InventorySortConfig config = new InventorySortConfig();
        
        if (config == null) {
            LogUtil.warn("Inventory", "配置为空，无法进行排序");
            return;
        }
        
        switch (sortMode) {
            case NAME:
                LogUtil.info("Inventory", "执行按名称排序，升序: " + config.getSortSettings().isNameAscending());
                sortByName(items, config.getSortSettings().isNameAscending());
                break;
            case QUANTITY:
                LogUtil.info("Inventory", "执行按数量排序，降序: " + config.getSortSettings().isQuantityDescending());
                sortByQuantity(items, config.getSortSettings().isQuantityDescending());
                break;
        }
        
        LogUtil.info("Inventory", "排序完成");
    }
    
    /**
     * 按名称排序（复合排序：第一级按名称，第二级按数量）
     */
    private void sortByName(List<ItemStack> items, boolean ascending) {
        LogUtil.info("Inventory", "开始复合排序（名称+数量），名称升序: " + ascending + "，物品数量: " + items.size());
        
        items.sort((a, b) -> {
            String nameA = a.getName().getString();
            String nameB = b.getName().getString();
            
            // 第一级：按名称排序
            int nameCompare = ascending ? nameA.compareTo(nameB) : nameB.compareTo(nameA);
            
            // 第二级：如果名称相同，按数量降序排序（数量多的在前）
            if (nameCompare == 0) {
                return Integer.compare(b.getCount(), a.getCount());
            }
            
            return nameCompare;
        });
        
        LogUtil.info("Inventory", "复合排序完成");
    }
    
    /**
     * 按数量排序（复合排序：第一级按数量，第二级按名称）
     */
    private void sortByQuantity(List<ItemStack> items, boolean descending) {
        LogUtil.info("Inventory", "开始复合排序（数量+名称），数量降序: " + descending + "，物品数量: " + items.size());
        
        items.sort((a, b) -> {
            int countA = a.getCount();
            int countB = b.getCount();
            
            // 第一级：按数量排序
            int countCompare = descending ? Integer.compare(countB, countA) : Integer.compare(countA, countB);
            
            // 第二级：如果数量相同，按名称排序（保持一致性）
            if (countCompare == 0) {
                String nameA = a.getName().getString();
                String nameB = b.getName().getString();
                return nameA.compareTo(nameB);
            }
            
            return countCompare;
        });
        
        LogUtil.info("Inventory", "复合排序完成");
    }
    
    /**
     * 获取物品的唯一键，用于堆叠判断
     */
    private String getItemKey(ItemStack stack) {
        if (stack.isEmpty()) return "empty";
        
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        
        // 对于现在，使用简化的键而不包含NBT以避免兼容性问题
        // TODO: 当方法可用性确认时添加适当的NBT处理
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
     * 验证合并结果的正确性
     */
    private boolean validateMergeResult(List<ItemStack> original, List<ItemStack> merged) {
        // 统计原始物品
        Map<String, Integer> originalCounts = new HashMap<>();
        for (ItemStack stack : original) {
            if (!stack.isEmpty()) {
                String key = getItemKey(stack);
                originalCounts.put(key, originalCounts.getOrDefault(key, 0) + stack.getCount());
            }
        }
        
        // 统计合并后物品
        Map<String, Integer> mergedCounts = new HashMap<>();
        for (ItemStack stack : merged) {
            if (!stack.isEmpty()) {
                String key = getItemKey(stack);
                mergedCounts.put(key, mergedCounts.getOrDefault(key, 0) + stack.getCount());
            }
        }
        
        // 比较数量
        for (String key : originalCounts.keySet()) {
            int originalCount = originalCounts.get(key);
            int mergedCount = mergedCounts.getOrDefault(key, 0);
            if (originalCount != mergedCount) {
                LogUtil.warn("Inventory", "验证失败：物品 " + key + " 数量不匹配，原始: " + originalCount + "，合并后: " + mergedCount);
                return false;
            }
        }
        
        // 检查是否有额外的物品
        for (String key : mergedCounts.keySet()) {
            if (!originalCounts.containsKey(key)) {
                LogUtil.warn("Inventory", "验证失败：发现额外物品 " + key + "，数量: " + mergedCounts.get(key));
                return false;
            }
        }
        
        LogUtil.info("Inventory", "合并结果验证通过");
        return true;
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
     * 获取指定位置的槽位
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
                            LogUtil.info("Inventory", "成功调用 " + methodName + "，找到槽位: " + slot.id);
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
            LogUtil.warn("Inventory", "获取槽位失败: " + e.getMessage());
        }
        return null;
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
                if (stackI.isOf(stackJ.getItem())) {
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
        
        // 获取排序比较器
        SortStrategy sortStrategy = SortStrategyFactory.getStrategy(sortMode);
        LogUtil.info("Inventory", "使用排序策略: " + sortStrategy.getName());
        
        // 使用选择排序算法：找到整个范围内最应该靠前的物品
        for (int i = 0; i < targetSlots.size(); i++) {
            Slot slotI = targetSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            // 如果当前位置为空，找到后面最应该靠前的非空物品
            if (stackI.isEmpty()) {
                int bestEmptyIndex = -1;
                ItemStack bestEmptyStack = null;
                
                // 找到后面最应该靠前的物品
                for (int j = i + 1; j < targetSlots.size(); j++) {
                    Slot slotJ = targetSlots.get(j);
                    ItemStack stackJ = slotJ.getStack();
                    
                    if (!stackJ.isEmpty()) {
                        if (bestEmptyStack == null || sortStrategy.compare(stackJ, bestEmptyStack) < 0) {
                            bestEmptyStack = stackJ;
                            bestEmptyIndex = j;
                        }
                    }
                }
                
                // 如果找到了更好的物品，移动到当前位置
                if (bestEmptyIndex != -1) {
                    strategy.moveItem(player, targetSlots.get(bestEmptyIndex), slotI);
                    LogUtil.info("Inventory", "移动槽位 " + bestEmptyIndex + " 到空槽位 " + i);
                }
            } else {
                // 当前位置有物品，找到后面最应该靠前的物品
                int bestIndex = i;
                ItemStack bestStack = stackI;
                
                for (int j = i + 1; j < targetSlots.size(); j++) {
                    Slot slotJ = targetSlots.get(j);
                    ItemStack stackJ = slotJ.getStack();
                    
                    if (!stackJ.isEmpty() && sortStrategy.compare(stackJ, bestStack) < 0) {
                        bestStack = stackJ;
                        bestIndex = j;
                    }
                }
                
                // 如果找到了更好的物品，交换位置
                if (bestIndex != i) {
                    strategy.swapSlots(player, slotI, targetSlots.get(bestIndex));
                    LogUtil.info("Inventory", "移动槽位 " + i + " 和槽位 " + bestIndex);
                }
            }
        }
        
        LogUtil.info("Inventory", "通用选择排序完成");
    }
}
