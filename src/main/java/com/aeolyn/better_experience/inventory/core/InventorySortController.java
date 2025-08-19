package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.screen.ScreenHandler;

/**
 * 背包排序控制器
 * 专门负责背包和容器的排序功能
 */
public class InventorySortController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Inventory");
    private static volatile InventorySortController instance;
    
    private InventorySortController() {}
    
    /**
     * 获取单例实例
     */
    public static InventorySortController getInstance() {
        if (instance == null) {
            synchronized (InventorySortController.class) {
                if (instance == null) {
                    instance = new InventorySortController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化背包整理控制器
     */
    public static void initialize() {
        LogUtil.info("Inventory", "初始化背包整理控制器");
        getInstance();
    }
    
    /**
     * 整理背包
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode) {
        sortInventory(sortMode, false); // 默认不合并
    }
    
    /**
     * 整理背包（支持合并模式）
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        // 使用简单的选择排序算法
        simpleSelectionSort(sortMode, mergeFirst);
    }
    
    /**
     * 整理容器（支持合并模式）
     */
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
    
    /**
     * 整理容器（默认不合并）
     */
    public void sortContainer(Inventory container) {
        sortContainer(container, InventorySortConfig.SortMode.NAME, false);
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
     * 检查两个物品是否可以合并堆叠
     */
    private boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.isOf(stack2.getItem())) return false;
        
        // 检查是否可以堆叠在一起
        int maxStack = Math.min(stack1.getMaxCount(), stack2.getMaxCount());
        return stack1.getCount() + stack2.getCount() <= maxStack;
    }
    
    /**
     * 计算合并后的堆叠数量
     */
    private int calculateMergedStackCount(ItemStack stack1, ItemStack stack2) {
        if (!canMergeStacks(stack1, stack2)) return -1;
        return stack1.getCount() + stack2.getCount();
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
      * 使用 clickSlot 将主背包栏(9-35)重排为目标排列，保证服务端/客户端一致
      */
     private void performReorderWithClicks(ClientPlayerEntity player, List<ItemStack> current, List<ItemStack> desired) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client == null || client.interactionManager == null) {
             throw new IllegalStateException("interactionManager 不可用");
         }
         
         ScreenHandler handler = player.currentScreenHandler;
         int syncId = handler.syncId;
         
         List<Integer> mainSlotIds = new ArrayList<>();
         for (Slot s : handler.slots) {
             if (s.inventory == player.getInventory() && s.getIndex() >= 9 && s.getIndex() < 36) {
                 mainSlotIds.add(s.id);
             }
         }
         // Sort by internal index to ensure order
         mainSlotIds.sort(Comparator.comparingInt(id -> {
             for (Slot s : handler.slots) {
                 if (s.id == id) return s.getIndex();
             }
             return -1;
         }));
         if (mainSlotIds.size() != 27) {
             throw new IllegalStateException("意外的玩家主背包槽位数量: " + mainSlotIds.size());
         }
         
         LogUtil.info("Inventory", "开始背包重排，当前物品数: " + current.stream().filter(s -> !s.isEmpty()).count());
         
         // 使用更简单、更稳定的重排算法
         // 采用冒泡排序的思想，逐步将物品移动到正确位置
         
         boolean changed;
         int maxIterations = 27; // 防止无限循环
         int iteration = 0;
         
         do {
             changed = false;
             iteration++;
             
             // 重新读取当前背包状态，确保状态同步
             List<ItemStack> currentState = new ArrayList<>();
             for (int i = 9; i < 36; i++) {
                 currentState.add(player.getInventory().getStack(i).copy());
             }
             
         for (int i = 0; i < 27; i++) {
             ItemStack want = desired.get(i);
                 ItemStack have = currentState.get(i);
                 
                 // 如果当前位置已经正确，跳过
                 if (areStacksEqualType(have, want)) {
                     continue;
                 }
                 
                 // 在整个背包中查找目标物品
                 int targetSlotIndex = -1;
                 for (int j = 0; j < 27; j++) {
                     if (j != i && areStacksEqualType(currentState.get(j), want)) {
                         targetSlotIndex = j;
                     break;
                 }
             }
             
                 // 如果找到了目标物品，进行交换
                 if (targetSlotIndex != -1) {
                     int slotA = mainSlotIds.get(i);
                     int slotB = mainSlotIds.get(targetSlotIndex);
                     
                     if (slotA != slotB) {
                         swapSlots(player, slotA, slotB);
                         changed = true;
                         
                         LogUtil.info("Inventory", "第" + iteration + "轮交换: " + slotA + " <-> " + slotB + " (物品: " + 
                             (want.isEmpty() ? "空" : want.getName().getString()) + " <-> " +
                             (have.isEmpty() ? "空" : have.getName().getString()) + ")");
                         
                         // 等待一下让服务端同步
                         try {
                             Thread.sleep(50);
                         } catch (InterruptedException e) {
                             Thread.currentThread().interrupt();
                         }
                     }
                 }
             }
             
         } while (changed && iteration < maxIterations);
         
         if (iteration >= maxIterations) {
             LogUtil.warn("Inventory", "重排达到最大迭代次数，可能存在循环依赖");
         }
         
                 LogUtil.info("Inventory", "背包重排完成，共进行了 " + iteration + " 轮交换");
    }
    
    /** 获取严格匹配键：物品类型 + 数量。用于一次性交换算法的精确定位 */
    private String getExactKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "empty#0";
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        return itemId + "#" + stack.getCount();
    }
    
    /**
     * 一次性交换重排：
     * - 基于精确键(类型+数量)构建当前位置索引队列
     * - 单次扫描按需交换到位，避免多轮迭代与等待
     */
    private void performReorderBySwaps(ClientPlayerEntity player, List<ItemStack> current, List<ItemStack> desired) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            throw new IllegalStateException("interactionManager 不可用");
        }
        
        ScreenHandler handler = player.currentScreenHandler;
        List<Integer> mainSlotIds = new ArrayList<>();
        for (Slot s : handler.slots) {
            if (s.inventory == player.getInventory() && s.getIndex() >= 9 && s.getIndex() < 36) {
                mainSlotIds.add(s.id);
            }
        }
        // 按 index 顺序排序，确保与 current/desired 对齐
        mainSlotIds.sort(Comparator.comparingInt(id -> {
            for (Slot s : handler.slots) {
                if (s.id == id) return s.getIndex();
            }
            return -1;
        }));
        if (mainSlotIds.size() != 27) {
            throw new IllegalStateException("意外的玩家主背包槽位数量: " + mainSlotIds.size());
        }
        
        // 精确键 -> 队列(当前这些键所在的位置索引)
        Map<String, Deque<Integer>> keyToPositions = new HashMap<>();
        for (int i = 0; i < 27; i++) {
            String k = getExactKey(current.get(i));
            keyToPositions.computeIfAbsent(k, kk -> new ArrayDeque<>()).addLast(i);
        }
        
        for (int i = 0; i < 27; i++) {
            ItemStack want = i < desired.size() ? desired.get(i) : ItemStack.EMPTY;
            ItemStack have = current.get(i);
            
            // 已在正确位置则消费该索引
            if (areStacksEqualExact(have, want)) {
                Deque<Integer> dq = keyToPositions.get(getExactKey(have));
                if (dq != null) {
                    // 移除一个等于 i 的元素
                    dq.removeFirstOccurrence(i);
                }
                continue;
            }
            
            // 查找满足 want 的位置
            Deque<Integer> wantQueue = keyToPositions.get(getExactKey(want));
            int j = -1;
            if (wantQueue != null) {
                // 取一个不等于 i 的位置
                Iterator<Integer> it = wantQueue.iterator();
                while (it.hasNext()) {
                    int idx = it.next();
                    if (idx != i) { j = idx; it.remove(); break; }
                }
            }
            
            if (j == -1) {
                // 没有可用位置，跳过（可能是空栈或数量不匹配）
                continue;
            }
            
            int slotA = mainSlotIds.get(i);
            int slotB = mainSlotIds.get(j);
            if (slotA == slotB) continue;
            
            // 执行交换
            swapSlots(player, slotA, slotB);
            
            // 同步本地 current
            ItemStack tmp = current.get(i);
            current.set(i, current.get(j));
            current.set(j, tmp);
            
            // 更新映射：
            // 1) 从 haveKey 的队列移除 i，并把 j 放回去
            String haveKey = getExactKey(tmp);
            Deque<Integer> haveQueue = keyToPositions.computeIfAbsent(haveKey, kk -> new ArrayDeque<>());
            haveQueue.removeFirstOccurrence(i);
            haveQueue.addLast(j);
            
            // 2) i 位置现在放入了 wantKey，i 已经在 wantQueue 中被移除，不需要再添加
        }
        
        LogUtil.info("Inventory", "背包重排完成（一次性交换）");
     }
     
     /**
      * 执行容器重排（使用clickSlot）
      */
     private void performContainerReorderWithClicks(ClientPlayerEntity player, Inventory container, List<ItemStack> current, List<ItemStack> desired) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client == null || client.interactionManager == null) {
             throw new IllegalStateException("interactionManager 不可用");
         }
         
         ScreenHandler handler = player.currentScreenHandler;
         int syncId = handler.syncId;
         
         List<Integer> slotIndices = getSlotIndicesForInventory(handler, container);
         
         if (slotIndices.size() != container.size()) {
             throw new IllegalStateException("意外的容器槽位数量: " + slotIndices.size() + " vs " + container.size());
         }
         
         LogUtil.info("Inventory", "开始容器重排，当前物品数: " + current.stream().filter(s -> !s.isEmpty()).count());
         
         for (int i = 0; i < container.size(); i++) {
             ItemStack want = desired.get(i);
             ItemStack have = current.get(i);
             
             // Skip if already correct
             if (areStacksEqualExact(have, want)) {
                 continue;
             }
             
             // Find the item we want at this position
             int j = -1;
             
             // First, try to find exact match
             for (int k = i + 1; k < container.size(); k++) {
                 if (areStacksEqualExact(current.get(k), want)) {
                     j = k;
                     break;
                 }
             }
             
             // If no exact match and we want an empty slot, find an empty slot
             if (j == -1 && want.isEmpty() && !have.isEmpty()) {
                 for (int k = container.size() - 1; k > i; k--) {
                     if (current.get(k).isEmpty()) {
                         j = k;
                         break;
                     }
                 }
             }
             
             // If we want a non-empty item but don't have exact match, try to find items that can satisfy the desired stack
             if (j == -1 && !want.isEmpty()) {
                 // Look for items that can satisfy what we want
                 for (int k = i + 1; k < container.size(); k++) {
                     ItemStack candidate = current.get(k);
                     if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                         // Check if this candidate can satisfy our desired stack
                         if (candidate.getCount() >= want.getCount()) {
                             j = k;
                             break;
                         }
                     }
                 }
                 
                 // If still not found, look for multiple stacks that can be combined
                 if (j == -1) {
                     int neededCount = want.getCount();
                     int availableCount = 0;
                     List<Integer> candidates = new ArrayList<>();
                     
                     for (int k = i + 1; k < container.size(); k++) {
                         ItemStack candidate = current.get(k);
                         if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                             availableCount += candidate.getCount();
                             candidates.add(k);
                             if (availableCount >= neededCount) {
                                 // Use the first candidate that gives us enough
                                 j = candidates.get(0);
                                 break;
                             }
                         }
                     }
                 }
             }
             
             // Perform the swap if we found a suitable item
             if (j != -1 && j != i) {
                 int slotA = slotIndices.get(i);
                 int slotB = slotIndices.get(j);
                 swapSlots(player, slotA, slotB);
                 
                 // Update our tracking list
                 ItemStack tmp = current.get(i);
                 current.set(i, current.get(j));
                 current.set(j, tmp);
                 
                 LogUtil.info("Inventory", "交换容器槽位: " + slotA + " <-> " + slotB + " (物品: " + 
                     (current.get(i).isEmpty() ? "空" : current.get(i).getName().getString()) + " <-> " +
                     (current.get(j).isEmpty() ? "空" : current.get(j).getName().getString()) + ")");
             }
         }
         
         LogUtil.info("Inventory", "容器重排完成");
     }
     
         /**
     * 使用策略模式交换两个槽位的物品
     */
    private void swapSlots(ClientPlayerEntity player, int slotA, int slotB) {
        // 获取合适的移动策略
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        
        // 找到对应的Slot对象
        Slot slotAObj = null;
        Slot slotBObj = null;
        
        for (Slot slot : player.currentScreenHandler.slots) {
            if (slot.id == slotA) slotAObj = slot;
            if (slot.id == slotB) slotBObj = slot;
        }
        
        if (slotAObj != null && slotBObj != null) {
            strategy.swapSlots(player, slotAObj, slotBObj);
        } else {
            LogUtil.warn("Inventory", "无法找到槽位对象，回退到传统方法");
            // 回退到传统方法
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.interactionManager != null) {
                int syncId = player.currentScreenHandler.syncId;
                client.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, player);
                client.interactionManager.clickSlot(syncId, slotB, 0, SlotActionType.PICKUP, player);
                client.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, player);
            }
        }
    }
     
         /**
     * 智能排序：根据鼠标位置决定排序背包还是容器
     * 注意：背包界面不使用 QUICK_MOVE，避免跨容器问题
     */
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
            
            // 添加调试输出
            LogUtil.info("Inventory", "R键按下，当前屏幕类: " + client.currentScreen.getClass().getName());
            
            // 特别检查潜影盒界面
            if (client.currentScreen.getClass().getSimpleName().contains("ShulkerBox")) {
                LogUtil.info("Inventory", "检测到潜影盒界面！");
            }
            
            // 获取缩放后的鼠标位置
            double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
            
            LogUtil.info("Inventory", "鼠标位置: ({}, {})", mouseX, mouseY);
            
            // 获取当前屏幕
            net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
            
            // 声明 slot
            net.minecraft.screen.slot.Slot slot = null;
            
            // 尝试获取槽位
            Class<?> currentClass = handledScreen.getClass();
            while (currentClass != null && slot == null) {
                String[] possibleMethodNames = {"getSlotAt", "method_5452", "method_2385", "method_1542", "method_64240", "method_2383", "method_64241", "method_2381", "method_2378"};
                for (String methodName : possibleMethodNames) {
                    try {
                        java.lang.reflect.Method getSlotAtMethod = currentClass.getDeclaredMethod(methodName, double.class, double.class);
                        getSlotAtMethod.setAccessible(true);
                        slot = (net.minecraft.screen.slot.Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                        if (slot != null) {
                            LogUtil.info("Inventory", "成功调用 " + methodName + "，找到槽位: " + slot.id);
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个方法
                    }
                }
                if (slot == null) {
                    currentClass = currentClass.getSuperclass();
                }
            }
            
            if (slot == null) {
                LogUtil.warn("Inventory", "无法获取槽位，排序跳过");
                return;
            }
            
            // 根据当前界面类型决定排序策略
            if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                // 背包界面：根据游戏模式选择排序策略（创造用setStack，生存用PICKUP）
                LogUtil.info("Inventory", "背包界面：根据游戏模式选择排序策略");
                simpleSelectionSort(InventorySortConfig.SortMode.NAME, true);
            } else {
                // 容器界面：统一使用PICKUP操作，不区分创造/生存模式
                LogUtil.info("Inventory", "容器界面：统一使用PICKUP操作排序");
                
                // 判断鼠标在哪个位置，确定整理哪个容器
                net.minecraft.inventory.Inventory inventory = slot.inventory;
                boolean isPlayerInventory = inventory == client.player.getInventory();
                LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 环境: " + (isPlayerInventory ? "玩家背包" : "容器"));
                
                // 添加容器类型调试信息
                if (!isPlayerInventory) {
                    LogUtil.info("Inventory", "容器类型: " + inventory.getClass().getSimpleName() + 
                        ", 容器大小: " + inventory.size());
                }
                
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
     * 简单的选择排序算法
     * 根据游戏模式选择不同的排序策略
     */
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        try {
            LogUtil.info("Inventory", "开始执行简单选择排序，排序模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Inventory", "玩家不存在，无法整理背包");
                return;
            }

            // 根据游戏模式选择不同的排序策略
            if (player.getAbilities().creativeMode) {
                // 创造模式：使用直接操作 + 数据包同步
                performCreativeModeSort(player, sortMode, mergeFirst);
            } else {
                // 生存模式：使用PICKUP操作优化
                performSurvivalModeSort(player, sortMode, mergeFirst);
            }
            
            LogUtil.info("Inventory", "简单选择排序完成");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "简单选择排序失败", e);
        }
    }
    
    /**
     * 创造模式排序：直接操作库存 + 数据包同步
     */
    private void performCreativeModeSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        Inventory inventory = player.getInventory();
        
        // 收集当前主背包状态（9-35槽位）
        List<ItemStack> currentItems = new ArrayList<>();
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
            performSimpleMergeSort(player, currentItems, sortMode);
        } else {
            // 普通模式：直接排序
            performSimpleSort(player, currentItems, sortMode);
        }
    }
    
    /**
     * 生存模式排序：使用PICKUP操作优化
     */
    private void performSurvivalModeSort(ClientPlayerEntity player, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        LogUtil.info("Inventory", "生存模式：使用PICKUP优化排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        if (mergeFirst) {
            // 生存模式合并：使用PICKUP的堆叠特性
            performSurvivalMergeSort(player, strategy, mainSlots, sortMode);
        } else {
            // 生存模式排序：使用PICKUP的交换特性
            performSurvivalSelectionSort(player, strategy, mainSlots, sortMode);
        }
    }
    
    /**
     * 简单的合并排序
     */
    private void performSimpleMergeSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始执行简单合并排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 第一步：合并相同物品
        mergeSameItems(player, strategy, mainSlots);
        
        // 第二步：重新收集物品状态
        List<ItemStack> mergedItems = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            mergedItems.add(player.getInventory().getStack(i).copy());
        }
        
        // 第三步：对合并后的物品进行排序
        performSimpleSort(player, mergedItems, sortMode);
        
        LogUtil.info("Inventory", "简单合并排序完成");
    }
    
    /**
     * 合并相同物品
     */
    private void mergeSameItems(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots) {
        LogUtil.info("Inventory", "开始合并相同物品");
        
        // 使用简单的双指针方法合并相同物品
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
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
        
        LogUtil.info("Inventory", "相同物品合并完成");
    }
    
    /**
     * 简单的排序算法
     */
    private void performSimpleSort(ClientPlayerEntity player, List<ItemStack> currentItems, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始执行简单排序");
        
        ItemMoveStrategy strategy = ItemMoveStrategyFactory.createStrategy(player);
        List<Slot> mainSlots = getMainInventorySlots(player);
        
        // 获取排序比较器
        SortComparator comparator = SortComparatorFactory.getComparator(sortMode);
        LogUtil.info("Inventory", "使用排序策略: " + comparator.getName());
        
        // 使用选择排序算法：找到整个背包中最应该靠前的物品
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            // 如果当前位置为空，找到后面最应该靠前的非空物品
            if (stackI.isEmpty()) {
                int bestEmptyIndex = -1;
                ItemStack bestEmptyStack = null;
                
                // 找到后面最应该靠前的物品
                for (int j = i + 1; j < mainSlots.size(); j++) {
                    Slot slotJ = mainSlots.get(j);
                    ItemStack stackJ = slotJ.getStack();
                    
                    if (!stackJ.isEmpty()) {
                        if (bestEmptyStack == null || comparator.compare(stackJ, bestEmptyStack) < 0) {
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
            ItemStack bestStack = stackI;
            
            // 从当前位置开始，找到最应该靠前的物品
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 使用策略比较物品，找到最应该靠前的
                if (comparator.compare(stackJ, bestStack) < 0) {
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
        
        LogUtil.info("Inventory", "简单排序完成");
    }
    




    /**
     * 排序比较策略接口
     * 用于实现不同的排序比较逻辑
     */
    public interface SortComparator {
        /**
         * 比较两个物品
         * @param stack1 物品1
         * @param stack2 物品2
         * @return 负数表示stack1应该排在stack2前面，正数表示stack2应该排在stack1前面，0表示相等
         */
        int compare(ItemStack stack1, ItemStack stack2);
        
        /**
         * 获取策略名称
         * @return 策略名称
         */
        String getName();
    }
    
    /**
     * 按名称排序策略（升序）
     */
    public static class NameAscendingComparator implements SortComparator {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            String name1 = stack1.getName().getString();
            String name2 = stack2.getName().getString();
            int nameCompare = name1.compareTo(name2);
            
            // 如果名称相同，按数量降序排序（数量多的在前）
            if (nameCompare == 0) {
                int count1 = stack1.getCount();
                int count2 = stack2.getCount();
                return Integer.compare(count2, count1); // 降序
            }
            return nameCompare;
        }
        
        @Override
        public String getName() {
            return "按名称升序";
        }
    }
    
    /**
     * 按名称排序策略（降序）
     */
    public static class NameDescendingComparator implements SortComparator {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            String name1 = stack1.getName().getString();
            String name2 = stack2.getName().getString();
            int nameCompare = name2.compareTo(name1); // 降序
            
            // 如果名称相同，按数量降序排序（数量多的在前）
            if (nameCompare == 0) {
                int count1 = stack1.getCount();
                int count2 = stack2.getCount();
                return Integer.compare(count2, count1); // 降序
            }
            return nameCompare;
        }
        
        @Override
        public String getName() {
            return "按名称降序";
        }
    }
    
    /**
     * 按数量排序策略（降序）
     */
    public static class QuantityDescendingComparator implements SortComparator {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            int count1 = stack1.getCount();
            int count2 = stack2.getCount();
            
            if (count1 != count2) {
                return Integer.compare(count2, count1); // 降序
            }
            
            // 数量相同时按名称排序
            String name1 = stack1.getName().getString();
            String name2 = stack2.getName().getString();
            return name1.compareTo(name2);
        }
        
        @Override
        public String getName() {
            return "按数量降序";
        }
    }
    
    /**
     * 按数量排序策略（升序）
     */
    public static class QuantityAscendingComparator implements SortComparator {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            int count1 = stack1.getCount();
            int count2 = stack2.getCount();
            
            if (count1 != count2) {
                return Integer.compare(count1, count2); // 升序
            }
            
            // 数量相同时按名称排序
            String name1 = stack1.getName().getString();
            String name2 = stack2.getName().getString();
            return name1.compareTo(name2);
        }
        
        @Override
        public String getName() {
            return "按数量升序";
        }
    }
    
    /**
     * 按类型排序策略
     */
    public static class TypeComparator implements SortComparator {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            // 按物品类型分组排序
            String type1 = getItemType(stack1);
            String type2 = getItemType(stack2);
            
            int typeCompare = type1.compareTo(type2);
            if (typeCompare != 0) {
                return typeCompare;
            }
            
            // 类型相同时按名称排序
            String name1 = stack1.getName().getString();
            String name2 = stack2.getName().getString();
            return name1.compareTo(name2);
        }
        
        private String getItemType(ItemStack stack) {
            // 简单的类型判断，可以根据需要扩展
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("pickaxe")) {
                return "工具";
            } else if (itemId.contains("armor")) {
                return "装备";
            } else if (itemId.contains("food")) {
                return "食物";
            } else if (itemId.contains("block")) {
                return "方块";
            } else {
                return "其他";
            }
        }
        
        @Override
        public String getName() {
            return "按类型排序";
        }
    }
    
    /**
     * 排序策略工厂
     */
    public static class SortComparatorFactory {
        private static final Map<InventorySortConfig.SortMode, SortComparator> comparators = new HashMap<>();
        
        static {
            // 注册默认的比较器
            comparators.put(InventorySortConfig.SortMode.NAME, new NameAscendingComparator());
            comparators.put(InventorySortConfig.SortMode.QUANTITY, new QuantityDescendingComparator());
        }
        
        /**
         * 获取排序比较器
         * @param sortMode 排序模式
         * @return 排序比较器
         */
        public static SortComparator getComparator(InventorySortConfig.SortMode sortMode) {
            return comparators.getOrDefault(sortMode, new NameAscendingComparator());
        }
        
        /**
         * 注册自定义比较器
         * @param sortMode 排序模式
         * @param comparator 比较器
         */
        public static void registerComparator(InventorySortConfig.SortMode sortMode, SortComparator comparator) {
            comparators.put(sortMode, comparator);
        }
        
        /**
         * 获取所有可用的比较器
         * @return 比较器映射
         */
        public static Map<InventorySortConfig.SortMode, SortComparator> getAllComparators() {
            return new HashMap<>(comparators);
        }
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
     * 生存模式合并排序：利用PICKUP的堆叠特性
     */
    private void performSurvivalMergeSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始合并排序");
        
        // 第一步：使用PICKUP的堆叠特性合并相同物品
        // 从前往后遍历，将相同物品堆叠到前面的槽位
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            if (stackI.isEmpty()) continue;
            
            // 从当前位置开始，向后查找相同物品进行堆叠
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 检查是否是相同物品且可以堆叠
                if (stackI.isOf(stackJ.getItem()) && strategy.canStackItems(stackI, stackJ)) {
                    strategy.stackItem(player, slotJ, slotI);
                    LogUtil.info("Inventory", "生存模式合并: 槽位 " + j + " -> " + i);
                }
            }
        }
        
        // 第二步：对合并后的物品进行排序
        performSurvivalSelectionSort(player, strategy, mainSlots, sortMode);
        
        LogUtil.info("Inventory", "生存模式合并排序完成");
    }
    
    /**
     * 生存模式选择排序：利用PICKUP的交换特性
     */
    private void performSurvivalSelectionSort(ClientPlayerEntity player, ItemMoveStrategy strategy, List<Slot> mainSlots, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "生存模式：开始选择排序");
        
        // 获取排序比较器
        SortComparator comparator = SortComparatorFactory.getComparator(sortMode);
        LogUtil.info("Inventory", "生存模式使用排序策略: " + comparator.getName());
        
        // 使用选择排序算法，但优化PICKUP操作
        for (int i = 0; i < mainSlots.size(); i++) {
            Slot slotI = mainSlots.get(i);
            ItemStack stackI = slotI.getStack();
            
            // 如果当前位置为空，找到后面最应该靠前的非空物品
            if (stackI.isEmpty()) {
                int bestEmptyIndex = -1;
                ItemStack bestEmptyStack = null;
                
                // 找到后面最应该靠前的物品
                for (int j = i + 1; j < mainSlots.size(); j++) {
                    Slot slotJ = mainSlots.get(j);
                    ItemStack stackJ = slotJ.getStack();
                    
                    if (!stackJ.isEmpty()) {
                        if (bestEmptyStack == null || comparator.compare(stackJ, bestEmptyStack) < 0) {
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
            ItemStack bestStack = stackI;
            
            // 从当前位置开始，找到最应该靠前的物品
            for (int j = i + 1; j < mainSlots.size(); j++) {
                Slot slotJ = mainSlots.get(j);
                ItemStack stackJ = slotJ.getStack();
                
                if (stackJ.isEmpty()) continue;
                
                // 使用策略比较物品，找到最应该靠前的
                if (comparator.compare(stackJ, bestStack) < 0) {
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
     * 容器界面统一排序：使用PICKUP操作，不区分创造/生存模式
     */
    private void performContainerSortWithPickup(ClientPlayerEntity player, Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        try {
            LogUtil.info("Inventory", "容器界面统一排序：开始整理容器，排序模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
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

            // 容器界面统一使用PICKUP操作进行排序
            if (mergeFirst) {
                // 使用PICKUP操作进行合并和排序
                performContainerSortWithPickupInternal(player, container, current, desired);
            } else {
                // 使用PICKUP操作进行排序
                performContainerReorderWithPickupInternal(player, container, current, desired);
            }
            LogUtil.info("Inventory", "容器界面统一排序完成，模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "容器界面统一排序失败", e);
        }
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
        SortComparator comparator = SortComparatorFactory.getComparator(sortMode);
        LogUtil.info("Inventory", "使用排序策略: " + comparator.getName());
        
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
                        if (bestEmptyStack == null || comparator.compare(stackJ, bestEmptyStack) < 0) {
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
                    
                    if (!stackJ.isEmpty() && comparator.compare(stackJ, bestStack) < 0) {
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
     * 测试潜影盒支持
     * 用于诊断潜影盒排序问题
     */
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
}
