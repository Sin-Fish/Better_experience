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
 * 背包整理控制器
 * 负责背包排序、容器操作等核心功能
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
        try {
            LogUtil.info("Inventory", "开始执行背包整理，排序模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst);
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Inventory", "玩家不存在，无法整理背包");
                return;
            }

            Inventory inventory = player.getInventory();
            LogUtil.info("Inventory", "获取到玩家背包，开始收集物品信息");

			// 新实现：使用服务端一致的 clickSlot 交换，避免客户端/服务端不同步
			try {
				List<ItemStack> current = new ArrayList<>(27);
				for (int i = 9; i < 36; i++) {
					current.add(inventory.getStack(i).copy());
				}
				int nonEmpty = (int) current.stream().filter(s -> !s.isEmpty()).count();
				LogUtil.info("Inventory", "收集当前主背包栏非空物品: " + nonEmpty);

				List<ItemStack> desired;
				if (mergeFirst) {
					// 合并模式：先合并相同物品，再排序
					desired = mergeAndSortItems(current, sortMode);
				} else {
					// 普通模式：直接排序，保持原堆叠
					desired = current.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).collect(Collectors.toList());
					sortItems(desired, sortMode);
				}
				while (desired.size() < 27) desired.add(ItemStack.EMPTY);

				// Update sortInventory method to use QUICK_MOVE merging
				if (mergeFirst) {
					// Use QUICK_MOVE for merging
					performMergeWithQuickMove(player, current, desired);
				} else {
					// Use regular reordering
					performReorderWithClicks(player, current, desired);
				}
				LogUtil.info("Inventory", "背包整理完成（服务端一致），模式: " + sortMode.getDisplayName() + "，合并模式: " + mergeFirst + "，目标非空物品: " + desired.stream().filter(s -> !s.isEmpty()).count());
				return;
			} catch (Exception re) {
				LogUtil.warn("Inventory", "服务端一致重排失败，回退到本地重排: " + re.getMessage());
			}
            
                        // 1. 将背包信息复制到数组中（只处理背包栏，不包括装备栏）
            ItemStack[] inventoryItems = new ItemStack[27]; // 背包栏有27个槽位 (9-35)
            int itemCount = 0;

            for (int i = 9; i < 36; i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty()) {
                    inventoryItems[itemCount++] = stack.copy(); // 深拷贝避免引用问题
                }
            }
            
            LogUtil.info("Inventory", "收集到 " + itemCount + " 个物品，开始创建物品列表");
            
            // 2. 创建实际物品列表（去掉空位）并处理堆叠
            List<ItemStack> items = new ArrayList<>();
            Map<String, ItemStack> itemMap = new HashMap<>(); // 用于合并相同物品
            
            for (int i = 0; i < itemCount; i++) {
                ItemStack stack = inventoryItems[i];
                String itemKey = getItemKey(stack);
                
                if (itemMap.containsKey(itemKey)) {
                    // 相同物品，尝试堆叠
                    ItemStack existingStack = itemMap.get(itemKey);
                    int maxStack = stack.getMaxCount();
                    int currentCount = existingStack.getCount();
                    int newCount = stack.getCount();
                    
                    if (currentCount + newCount <= maxStack) {
                        // 可以完全堆叠
                        existingStack.setCount(currentCount + newCount);
                    } else {
                        // 部分堆叠，创建新的堆叠
                        int space = maxStack - currentCount;
                        existingStack.setCount(maxStack);
                        stack.setCount(newCount - space);
                        items.add(stack.copy());
                    }
                } else {
                    // 新物品，添加到映射中
                    itemMap.put(itemKey, stack.copy());
                }
            }
            
            // 将合并后的物品添加到列表中
            items.addAll(itemMap.values());
            
            if (items.isEmpty()) {
                LogUtil.info("Inventory", "背包为空，无需整理");
                return;
            }
            
            // 3. 对物品列表进行排序
            LogUtil.info("Inventory", "开始对 " + items.size() + " 个物品进行排序");
            sortItems(items, sortMode);
            LogUtil.info("Inventory", "物品排序完成");
            
            // 4. 清空背包栏
            LogUtil.info("Inventory", "清空背包栏，准备重新放置物品");
            for (int i = 9; i < 36; i++) {
                inventory.setStack(i, ItemStack.EMPTY);
            }
            
                           // 5. 将排序后的物品重新放入背包
               LogUtil.info("Inventory", "开始将排序后的物品重新放入背包");
               int slotIndex = 9;
               for (ItemStack item : items) {
                   if (slotIndex >= 36) break;
                   inventory.setStack(slotIndex++, item);
               }
               LogUtil.info("Inventory", "物品重新放置完成，共放置 " + items.size() + " 个物品");
               
                               // 6. 强制刷新客户端UI - 使用更简单有效的方法
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.player != null) {
                    // 通知客户端背包已更新
                    client.player.getInventory().markDirty();
                    
                    // 强制刷新当前界面
                    if (client.currentScreen != null) {
                        try {
                            // 方法1: 重新初始化界面
                            client.currentScreen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
                            LogUtil.info("Inventory", "通过init方法刷新UI");
                        } catch (Exception e) {
                            LogUtil.warn("Inventory", "init方法失败: " + e.getMessage());
                            
                            // 方法2: 尝试resize方法
                            try {
                                client.currentScreen.getClass()
                                    .getMethod("resize", MinecraftClient.class, int.class, int.class)
                                    .invoke(client.currentScreen, client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
                                LogUtil.info("Inventory", "通过resize方法刷新UI");
                            } catch (Exception ex) {
                                LogUtil.warn("Inventory", "resize方法也失败: " + ex.getMessage());
                            }
                        }
                    }
                    
                    // 方法3: 强制重新同步背包数据
                    try {
                        // 再次通知客户端背包数据已更改
                        client.player.getInventory().markDirty();
                        
                        // 如果是背包界面，尝试特殊处理
                        if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                            // 对于背包界面，尝试强制刷新
                            client.currentScreen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
                            LogUtil.info("Inventory", "背包界面特殊刷新");
                        }
                        
                        LogUtil.info("Inventory", "强制重新同步背包数据");
                    } catch (Exception e) {
                        LogUtil.warn("Inventory", "强制同步失败: " + e.getMessage());
                    }
                    
                    LogUtil.info("Inventory", "已通知客户端UI刷新");
                }
            
            LogUtil.info("Inventory", "背包整理完成，模式: " + sortMode.getDisplayName() + "，共整理 " + items.size() + " 个物品");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "整理背包失败", e);
        }
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

            // Update sortContainer method to use QUICK_MOVE merging
            if (mergeFirst) {
                // Use QUICK_MOVE for merging
                performContainerMergeWithQuickMove(player, container, current, desired);
            } else {
                // Use regular reordering
                performContainerReorderWithClicks(player, container, current, desired);
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
    
    // Implement scheme 2: queue-based merging approach
    private List<ItemStack> mergeAndSortItems(List<ItemStack> items, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始合并相同物品，原始物品数: " + items.stream().filter(s -> !s.isEmpty()).count());
        
        // Create a working copy of items
        List<ItemStack> workingItems = new ArrayList<>();
        for (ItemStack item : items) {
            workingItems.add(item.copy());
        }
        
        // Track which slots have been processed
        boolean[] processed = new boolean[workingItems.size()];
        
        // Process each slot from front to back
        for (int i = 0; i < workingItems.size(); i++) {
            if (processed[i] || workingItems.get(i).isEmpty()) {
                continue;
            }
            
            ItemStack currentItem = workingItems.get(i);
            String itemKey = getItemKey(currentItem);
            int maxStack = currentItem.getMaxCount();
            
            LogUtil.info("Inventory", "处理槽位 " + i + " 的物品: " + currentItem.getName().getString() + " x" + currentItem.getCount());
            
            // Queue to store all items of the same type: [position, count]
            Queue<int[]> itemQueue = new LinkedList<>();
            
            // Collect all items of the same type from this position onwards
            for (int j = i; j < workingItems.size(); j++) {
                if (!processed[j] && !workingItems.get(j).isEmpty() && getItemKey(workingItems.get(j)).equals(itemKey)) {
                    itemQueue.offer(new int[]{j, workingItems.get(j).getCount()});
                    processed[j] = true;
                    LogUtil.info("Inventory", "  找到相同物品在槽位 " + j + ": " + workingItems.get(j).getName().getString() + " x" + workingItems.get(j).getCount());
                }
            }
            
            // Calculate total count
            int totalCount = itemQueue.stream().mapToInt(arr -> arr[1]).sum();
            LogUtil.info("Inventory", "  总数量: " + totalCount + "，最大堆叠: " + maxStack);
            
            // Fill slots from front to back
            int currentSlot = i;
            int remainingToFill = totalCount;
            
            while (remainingToFill > 0 && currentSlot < workingItems.size()) {
                int toFill = Math.min(remainingToFill, maxStack);
                
                // Create the stack for this slot
                workingItems.set(currentSlot, currentItem.copy());
                workingItems.get(currentSlot).setCount(toFill);
                
                LogUtil.info("Inventory", "    槽位 " + currentSlot + " 放置: " + toFill + " 个");
                
                remainingToFill -= toFill;
                currentSlot++;
            }
            
            // Clear any remaining slots that were part of this merge
            while (!itemQueue.isEmpty()) {
                int[] item = itemQueue.poll();
                int position = item[0];
                if (position >= currentSlot) { // Only clear slots that are after our filled slots
                    workingItems.set(position, ItemStack.EMPTY);
                    LogUtil.info("Inventory", "    清空槽位 " + position);
                }
            }
        }
        
        LogUtil.info("Inventory", "合并完成，合并后物品数: " + workingItems.stream().filter(s -> !s.isEmpty()).count());
        
        // Sort the merged items
        sortItems(workingItems, sortMode);
        return workingItems;
    }
    
    /**
     * 一键存入箱子
     */
    public void depositToContainer(Inventory container) {
        try {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            
            Inventory playerInventory = player.getInventory();
            int depositedCount = 0;
            
            // 从背包中查找可以存入的物品
            for (int i = 9; i < 36; i++) {
                ItemStack playerStack = playerInventory.getStack(i);
                if (playerStack.isEmpty()) continue;
                
                // 查找容器中的空位或相同物品的堆叠
                for (int j = 0; j < container.size(); j++) {
                    ItemStack containerStack = container.getStack(j);
                    
                    if (containerStack.isEmpty()) {
                        // 空位，直接放入
                        container.setStack(j, playerStack.copy());
                        playerInventory.setStack(i, ItemStack.EMPTY);
                        depositedCount++;
                        break;
                    } else if (canStack(playerStack, containerStack)) {
                        // 相同物品，尝试堆叠
                        int maxStack = Math.min(playerStack.getMaxCount(), containerStack.getMaxCount());
                        int space = maxStack - containerStack.getCount();
                        
                        if (space > 0) {
                            int transfer = Math.min(space, playerStack.getCount());
                            containerStack.increment(transfer);
                            playerStack.decrement(transfer);
                            
                            if (playerStack.isEmpty()) {
                                playerInventory.setStack(i, ItemStack.EMPTY);
                                depositedCount++;
                                break;
                            }
                        }
                    }
                }
            }
            
            LogUtil.info("Inventory", "存入物品完成，共存入 " + depositedCount + " 个物品");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "存入容器失败", e);
        }
    }
    
    /**
     * 一键从箱子拿取
     */
    public void withdrawFromContainer(Inventory container) {
        try {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            
            Inventory playerInventory = player.getInventory();
            int withdrawnCount = 0;
            
            // 从容器中查找可以拿取的物品
            for (int i = 0; i < container.size(); i++) {
                ItemStack containerStack = container.getStack(i);
                if (containerStack.isEmpty()) continue;
                
                // 查找背包中的空位或相同物品的堆叠
                for (int j = 9; j < 36; j++) {
                    ItemStack playerStack = playerInventory.getStack(j);
                    
                    if (playerStack.isEmpty()) {
                        // 空位，直接放入
                        playerInventory.setStack(j, containerStack.copy());
                        container.setStack(i, ItemStack.EMPTY);
                        withdrawnCount++;
                        break;
                    } else if (canStack(containerStack, playerStack)) {
                        // 相同物品，尝试堆叠
                        int maxStack = Math.min(containerStack.getMaxCount(), playerStack.getMaxCount());
                        int space = maxStack - playerStack.getCount();
                        
                        if (space > 0) {
                            int transfer = Math.min(space, containerStack.getCount());
                            playerStack.increment(transfer);
                            containerStack.decrement(transfer);
                            
                            if (containerStack.isEmpty()) {
                                container.setStack(i, ItemStack.EMPTY);
                                withdrawnCount++;
                                break;
                            }
                        }
                    }
                }
            }
            
            LogUtil.info("Inventory", "拿取物品完成，共拿取 " + withdrawnCount + " 个物品");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "从容器拿取失败", e);
        }
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
            case CATEGORY:
                LogUtil.info("Inventory", "执行按分类排序");
                sortByCategory(items, config.getSortSettings().getCategoryOrder());
                break;
            case TYPE:
                LogUtil.info("Inventory", "执行按类型排序");
                sortByType(items, config.getSortSettings().getTypeOrder());
                break;
        }
        
        LogUtil.info("Inventory", "排序完成");
    }
    
    /**
     * 按名称排序
     */
    private void sortByName(List<ItemStack> items, boolean ascending) {
        LogUtil.info("Inventory", "开始按名称排序，升序: " + ascending + "，物品数量: " + items.size());
        
        // 记录排序前的物品名称
        List<String> beforeNames = items.stream()
            .map(item -> item.getName().getString())
            .collect(Collectors.toList());
        LogUtil.info("Inventory", "排序前物品名称: " + beforeNames);
        
        items.sort((a, b) -> {
            String nameA = a.getName().getString();
            String nameB = b.getName().getString();
            return ascending ? nameA.compareTo(nameB) : nameB.compareTo(nameA);
        });
        
        // 记录排序后的物品名称
        List<String> afterNames = items.stream()
            .map(item -> item.getName().getString())
            .collect(Collectors.toList());
        LogUtil.info("Inventory", "排序后物品名称: " + afterNames);
    }
    
    /**
     * 按数量排序
     */
    private void sortByQuantity(List<ItemStack> items, boolean descending) {
        items.sort((a, b) -> {
            int countA = a.getCount();
            int countB = b.getCount();
            return descending ? Integer.compare(countB, countA) : Integer.compare(countA, countB);
        });
    }
    
    /**
     * 按分类排序
     */
    private void sortByCategory(List<ItemStack> items, String[] categoryOrder) {
        Map<String, Integer> categoryPriority = new HashMap<>();
        for (int i = 0; i < categoryOrder.length; i++) {
            categoryPriority.put(categoryOrder[i], i);
        }
        
        items.sort((a, b) -> {
            String categoryA = getItemCategory(a);
            String categoryB = getItemCategory(b);
            
            int priorityA = categoryPriority.getOrDefault(categoryA, Integer.MAX_VALUE);
            int priorityB = categoryPriority.getOrDefault(categoryB, Integer.MAX_VALUE);
            
            return Integer.compare(priorityA, priorityB);
        });
    }
    
    /**
     * 按类型排序
     */
    private void sortByType(List<ItemStack> items, String[] typeOrder) {
        Map<String, Integer> typePriority = new HashMap<>();
        for (int i = 0; i < typeOrder.length; i++) {
            typePriority.put(typeOrder[i], i);
        }
        
        items.sort((a, b) -> {
            String typeA = getItemType(a);
            String typeB = getItemType(b);
            
            int priorityA = typePriority.getOrDefault(typeA, Integer.MAX_VALUE);
            int priorityB = typePriority.getOrDefault(typeB, Integer.MAX_VALUE);
            
            return Integer.compare(priorityA, priorityB);
        });
    }
    
    /**
     * 获取物品分类
     */
    private String getItemCategory(ItemStack stack) {
        Item item = stack.getItem();
        String itemId = Registries.ITEM.getId(item).toString();
        
        if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("pickaxe") || 
            itemId.contains("shovel") || itemId.contains("hoe") || itemId.contains("bow") || 
            itemId.contains("crossbow") || itemId.contains("trident")) {
            return "武器";
        } else if (itemId.contains("helmet") || itemId.contains("chestplate") || 
                   itemId.contains("leggings") || itemId.contains("boots")) {
            return "防具";
        } else if (itemId.contains("apple") || itemId.contains("bread") || itemId.contains("meat") || 
                   itemId.contains("fish") || itemId.contains("potion")) {
            return "食物";
        } else if (itemId.contains("diamond") || itemId.contains("iron") || itemId.contains("gold") || 
                   itemId.contains("emerald") || itemId.contains("coal") || itemId.contains("stone")) {
            return "材料";
        } else if (itemId.contains("flower") || itemId.contains("sapling") || itemId.contains("torch") || 
                   itemId.contains("lantern")) {
            return "装饰";
        } else {
            return "其他";
        }
    }
    
    /**
     * 获取物品类型
     */
    private String getItemType(ItemStack stack) {
        Item item = stack.getItem();
        String itemId = Registries.ITEM.getId(item).toString();
        
        if (itemId.contains("block") || itemId.contains("stone") || itemId.contains("dirt") || 
            itemId.contains("wood") || itemId.contains("log")) {
            return "方块";
        } else if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("pickaxe") || 
                   itemId.contains("shovel") || itemId.contains("hoe")) {
            return "工具";
        } else if (itemId.contains("sword") || itemId.contains("bow") || itemId.contains("crossbow")) {
            return "武器";
        } else if (itemId.contains("helmet") || itemId.contains("chestplate") || 
                   itemId.contains("leggings") || itemId.contains("boots")) {
            return "防具";
        } else if (itemId.contains("apple") || itemId.contains("bread") || itemId.contains("meat")) {
            return "食物";
        } else if (itemId.contains("potion")) {
            return "药水";
        } else {
            return "物品";
        }
    }
    
         /**
      * 检查两个物品是否可以堆叠
      */
     private boolean canStack(ItemStack stack1, ItemStack stack2) {
         return stack1.isOf(stack2.getItem());
     }
     
     /**
      * 获取物品的唯一键，用于堆叠判断
      */
     private String getItemKey(ItemStack stack) {
         if (stack.isEmpty()) return "empty";
         
         String itemId = Registries.ITEM.getId(stack.getItem()).toString();
         
                 // For now, use simplified key without NBT to avoid compatibility issues
    // TODO: Add proper NBT handling when method availability is confirmed
    return itemId;
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
         
         for (int i = 0; i < 27; i++) {
             ItemStack want = desired.get(i);
             ItemStack have = current.get(i);
             
             // Skip if already correct
             if (areStacksEqualExact(have, want)) {
                 continue;
             }
             
             // Find the item we want at this position
             int j = -1;
             
             // First, try to find exact match
             for (int k = i + 1; k < 27; k++) {
                 if (areStacksEqualExact(current.get(k), want)) {
                     j = k;
                     break;
                 }
             }
             
             // If no exact match and we want an empty slot, find an empty slot
             if (j == -1 && want.isEmpty() && !have.isEmpty()) {
                 for (int k = 26; k > i; k--) {
                     if (current.get(k).isEmpty()) {
                         j = k;
                         break;
                     }
                 }
             }
             
             // If we want a non-empty item but don't have exact match, try to find items that can satisfy the desired stack
             if (j == -1 && !want.isEmpty()) {
                 // Look for items that can satisfy what we want
                 for (int k = i + 1; k < 27; k++) {
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
                     
                     for (int k = i + 1; k < 27; k++) {
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
                 int slotA = mainSlotIds.get(i);
                 int slotB = mainSlotIds.get(j);
                 clickSwap(client, syncId, slotA, slotB, player);
                 
                 // Update our tracking list
                 ItemStack tmp = current.get(i);
                 current.set(i, current.get(j));
                 current.set(j, tmp);
                 
                 LogUtil.info("Inventory", "交换槽位: " + slotA + " <-> " + slotB + " (物品: " + 
                     (current.get(i).isEmpty() ? "空" : current.get(i).getName().getString()) + " <-> " +
                     (current.get(j).isEmpty() ? "空" : current.get(j).getName().getString()) + ")");
             }
         }
         
         LogUtil.info("Inventory", "背包重排完成");
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
                 clickSwap(client, syncId, slotA, slotB, player);
                 
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
      * 用三次 PICKUP 点击完成两个槽位的交换
      */
     private void clickSwap(MinecraftClient client, int syncId, int slotA, int slotB, ClientPlayerEntity player) {
         client.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, player);
         client.interactionManager.clickSlot(syncId, slotB, 0, SlotActionType.PICKUP, player);
         client.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, player);
         LogUtil.info("Inventory", "交换槽位: " + slotA + " <-> " + slotB);
     }
     
     /**
      * 智能排序：根据鼠标位置决定排序背包还是容器
      */
     public void smartSortByMousePosition() {
         try {
             MinecraftClient client = MinecraftClient.getInstance();
             if (client == null || !(client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen)) {
                 LogUtil.info("Inventory", "R键按下，但没有打开库存界面，跳过智能排序");
                 return;
             }
             // 添加调试输出
             LogUtil.info("Inventory", "R键按下，当前屏幕类: " + client.currentScreen.getClass().getName());
             
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
             
             net.minecraft.inventory.Inventory inventory = slot.inventory;
             boolean isPlayerInventory = inventory == client.player.getInventory();
             LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 环境: " + (isPlayerInventory ? "玩家背包" : "容器"));
             
             if (isPlayerInventory) {
                 // 检查是否是主背包槽位（9-35），排除工具栏（0-8）
                 if (slot.getIndex() >= 9 && slot.getIndex() < 36) {
                     LogUtil.info("Inventory", "鼠标在主背包槽位上，执行背包排序");
                     sortInventory(InventorySortConfig.SortMode.NAME, true); // 合并模式
                 } else {
                     LogUtil.info("Inventory", "鼠标在工具栏槽位上，跳过排序");
                 }
             } else {
                 LogUtil.info("Inventory", "鼠标在容器槽位上，执行容器排序");
                 // 获取整个容器库存，而不是单个槽位的库存
                 net.minecraft.inventory.Inventory containerInventory = getContainerInventory(handledScreen);
                 if (containerInventory != null) {
                     sortContainer(containerInventory, InventorySortConfig.SortMode.NAME, true); // 合并模式
                 } else {
                     LogUtil.warn("Inventory", "无法获取容器库存，跳过排序");
                 }
             }
             
         } catch (Exception e) {
             LogUtil.error("Inventory", "智能排序失败: " + e.getMessage());
         }
     }
     
     /**
      * 检查鼠标是否在背包区域
      */
     private boolean isMouseInInventoryArea(double mouseX, double mouseY) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) return false;
         
         int screenWidth = client.getWindow().getScaledWidth();
         int screenHeight = client.getWindow().getScaledHeight();
         
         int centerX = screenWidth / 2;
         int centerY = screenHeight / 2;
         
         // 扩大背包区域范围
         return mouseX > centerX - 176 && mouseX < centerX && 
                mouseY > centerY - 83 && mouseY < centerY + 75;
     }
     
     /**
      * 检查鼠标是否在容器区域
      */
     private boolean isMouseInContainerArea(double mouseX, double mouseY) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) return false;
         
         int screenWidth = client.getWindow().getScaledWidth();
         int screenHeight = client.getWindow().getScaledHeight();
         
         int centerX = screenWidth / 2;
         int centerY = screenHeight / 2;
         
         // 扩大容器区域范围
         return mouseX > centerX && mouseX < centerX + 176 && 
                mouseY > centerY - 83 && mouseY < centerY + 75;
     }
     
     /**
      * 根据鼠标位置排序容器
      */
     private void sortContainerByMousePosition(double mouseX, double mouseY, boolean mergeFirst) {
         try {
             MinecraftClient client = MinecraftClient.getInstance();
             if (client.currentScreen == null) return;
             
             // 尝试获取当前容器的Inventory
             net.minecraft.inventory.Inventory container = getCurrentContainer();
             if (container != null) {
                 sortContainer(container, InventorySortConfig.SortMode.NAME, mergeFirst);
             } else {
                 LogUtil.warn("Inventory", "无法获取当前容器");
             }
         } catch (Exception e) {
             LogUtil.error("Inventory", "容器排序失败", e);
         }
     }
     
     /**
      * 获取当前容器的Inventory
      */
     private net.minecraft.inventory.Inventory getCurrentContainer() {
         try {
             MinecraftClient client = MinecraftClient.getInstance();
             if (client.currentScreen == null) return null;
             
             if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen) {
                 net.minecraft.client.gui.screen.ingame.HandledScreen<?> handledScreen = 
                     (net.minecraft.client.gui.screen.ingame.HandledScreen<?>) client.currentScreen;
                 
                 // 方法1: 通过handler获取
                 try {
                     // 尝试不同的 handler 字段名
                     String[] handlerFieldNames = {"handler", "field_22789", "screenHandler"}; // 可能的混淆名
                     Object handler = null;
                     for (String fieldName : handlerFieldNames) {
                         try {
                             java.lang.reflect.Field handlerField = handledScreen.getClass().getDeclaredField(fieldName);
                             handlerField.setAccessible(true);
                             handler = handlerField.get(handledScreen);
                             if (handler != null) {
                                 LogUtil.info("Inventory", "成功获取 handler，使用字段: " + fieldName);
                                 break;
                             }
                         } catch (NoSuchFieldException e) {
                             // 继续尝试下一个
                         }
                     }
                     
                     if (handler != null) {
                         // 尝试不同的 inventory 字段名
                         String[] inventoryFieldNames = {"inventory", "field_2388", "container"};
                         for (String fieldName : inventoryFieldNames) {
                             try {
                                 java.lang.reflect.Field inventoryField = handler.getClass().getDeclaredField(fieldName);
                                 inventoryField.setAccessible(true);
                                 Object inventory = inventoryField.get(handler);
                                 if (inventory instanceof net.minecraft.inventory.Inventory) {
                                     LogUtil.info("Inventory", "成功获取容器实例，使用字段: " + fieldName);
                                     return (net.minecraft.inventory.Inventory) inventory;
                                 }
                             } catch (NoSuchFieldException e) {
                                 // 继续尝试下一个
                             }
                         }
                     }
                 } catch (Exception e) {
                     LogUtil.warn("Inventory", "通过handler获取容器失败: " + e.getMessage());
                 }
                 
                 // 方法2: 通过slots获取
                 try {
                     // 尝试不同的 slots 字段名
                     String[] slotsFieldNames = {"slots", "field_22790"};
                     Object slots = null;
                     for (String fieldName : slotsFieldNames) {
                         try {
                             java.lang.reflect.Field slotsField = handledScreen.getClass().getDeclaredField(fieldName);
                             slotsField.setAccessible(true);
                             slots = slotsField.get(handledScreen);
                             if (slots instanceof java.util.List) {
                                 LogUtil.info("Inventory", "成功获取 slots，使用字段: " + fieldName);
                                 break;
                             }
                         } catch (NoSuchFieldException e) {
                             // 继续尝试下一个
                         }
                     }
                     
                     if (slots instanceof java.util.List) {
                         java.util.List<?> slotList = (java.util.List<?>) slots;
                         if (!slotList.isEmpty()) {
                             // 获取第一个槽位，然后获取其inventory
                             Object firstSlot = slotList.get(0);
                             if (firstSlot != null) {
                                 // 尝试不同的 inventory 字段名
                                 String[] slotInventoryNames = {"inventory", "field_1731"};
                                 for (String fieldName : slotInventoryNames) {
                                     try {
                                         java.lang.reflect.Field inventoryField = firstSlot.getClass().getDeclaredField(fieldName);
                                         inventoryField.setAccessible(true);
                                         Object inventory = inventoryField.get(firstSlot);
                                         if (inventory instanceof net.minecraft.inventory.Inventory) {
                                             LogUtil.info("Inventory", "成功通过slots获取容器实例，使用字段: " + fieldName);
                                             return (net.minecraft.inventory.Inventory) inventory;
                                         }
                                     } catch (NoSuchFieldException e) {
                                         // 继续尝试下一个
                                     }
                                 }
                             }
                         }
                     }
                 } catch (Exception e) {
                     LogUtil.warn("Inventory", "通过slots获取容器失败: " + e.getMessage());
                 }
             }
             
             LogUtil.warn("Inventory", "无法获取容器实例，所有方法都失败了");
             return null;
         } catch (Exception e) {
             LogUtil.warn("Inventory", "获取容器失败: " + e.getMessage());
             return null;
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
     * Add new method to check if items can be merged (partial stack)
     */
    private boolean canMergeStacks(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if (!a.isOf(b.getItem())) return false;
        
        // Check if they can be stacked together
        int maxStack = a.getMaxCount();
        return a.getCount() + b.getCount() <= maxStack;
    }

    /**
     * Add new method to perform merging using QUICK_MOVE operations
     */
    private void performMergeWithQuickMove(ClientPlayerEntity player, List<ItemStack> current, List<ItemStack> desired) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            throw new IllegalStateException("interactionManager 不可用");
        }
        
        ScreenHandler handler = player.currentScreenHandler;
        int syncId = handler.syncId;
        
        // 只处理主背包槽位（9-35），排除工具栏（0-8）
        List<Integer> mainSlotIds = new ArrayList<>();
        for (Slot s : handler.slots) {
            if (s.inventory == player.getInventory() && s.getIndex() >= 9 && s.getIndex() < 36) {
                mainSlotIds.add(s.id);
            }
        }
        mainSlotIds.sort(Comparator.comparingInt(id -> {
            for (Slot s : handler.slots) {
                if (s.id == id) return s.getIndex();
            }
            return -1;
        }));
        
        if (mainSlotIds.size() != 27) {
            throw new IllegalStateException("意外的玩家主背包槽位数量: " + mainSlotIds.size());
        }
        
        LogUtil.info("Inventory", "开始使用 QUICK_MOVE 实现目标状态（合并+排序）");
        
        // 使用 QUICK_MOVE 来实现目标状态
        // 首先处理合并，然后处理排序
        for (int i = 0; i < 27; i++) {
            ItemStack want = desired.get(i);
            ItemStack have = current.get(i);
            
            if (areStacksEqualExact(have, want)) {
                continue; // 已经正确
            }
            
            if (!want.isEmpty()) {
                // 需要在这个位置放置物品
                if (have.isEmpty() || !areStacksEqualType(have, want)) {
                    // 需要从其他地方移动物品到这里
                    for (int j = i + 1; j < 27; j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            // 找到了相同类型的物品，使用 QUICK_MOVE 移动
                            int sourceSlot = mainSlotIds.get(j);
                            int targetSlot = mainSlotIds.get(i);
                            
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                            LogUtil.info("Inventory", "QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动物品到目标位置)");
                            
                            // 更新跟踪状态
                            current.set(i, candidate.copy());
                            current.set(j, ItemStack.EMPTY);
                            break;
                        }
                    }
                } else if (have.getCount() < want.getCount()) {
                    // 需要从其他地方补充物品到这里
                    int needed = want.getCount() - have.getCount();
                    int maxStack = want.getMaxCount(); // 使用物品的实际堆叠上限
                    int spaceInTarget = maxStack - have.getCount();
                    int canTransfer = Math.min(needed, spaceInTarget);
                    
                    for (int j = i + 1; j < 27; j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            int transferAmount = Math.min(canTransfer, candidate.getCount());
                            if (transferAmount > 0) {
                                int sourceSlot = mainSlotIds.get(j);
                                int targetSlot = mainSlotIds.get(i);
                                
                                client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                                LogUtil.info("Inventory", "QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (补充 " + transferAmount + " 个物品，堆叠上限: " + maxStack + ")");
                                
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
                // 这个位置应该是空的，但当前有物品，需要移动到后面
                for (int j = 27 - 1; j > i; j--) {
                    if (current.get(j).isEmpty()) {
                        int sourceSlot = mainSlotIds.get(i);
                        int targetSlot = mainSlotIds.get(j);
                        
                        client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                        LogUtil.info("Inventory", "QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动物品到空位)");
                        
                        // 更新跟踪状态
                        current.set(j, have.copy());
                        current.set(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
        
        LogUtil.info("Inventory", "QUICK_MOVE 目标状态实现完成");
    }

    /**
     * Add similar method for containers
     */
    private void performContainerMergeWithQuickMove(ClientPlayerEntity player, Inventory container, List<ItemStack> current, List<ItemStack> desired) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) {
            throw new IllegalStateException("interactionManager 不可用");
        }
        
        ScreenHandler handler = player.currentScreenHandler;
        int syncId = handler.syncId;
        
        // 获取容器槽位ID，确保正确映射
        List<Integer> slotIndices = getSlotIndicesForInventory(handler, container);
        
        if (slotIndices.size() != container.size()) {
            throw new IllegalStateException("意外的容器槽位数量: " + slotIndices.size() + " vs " + container.size());
        }
        
        LogUtil.info("Inventory", "开始使用 QUICK_MOVE 实现容器目标状态（合并+排序）");
        
        // 使用 QUICK_MOVE 来实现目标状态
        // 首先处理合并，然后处理排序
        for (int i = 0; i < container.size(); i++) {
            ItemStack want = desired.get(i);
            ItemStack have = current.get(i);
            
            if (areStacksEqualExact(have, want)) {
                continue; // 已经正确
            }
            
            if (!want.isEmpty()) {
                // 需要在这个位置放置物品
                if (have.isEmpty() || !areStacksEqualType(have, want)) {
                    // 需要从其他地方移动物品到这里
                    for (int j = i + 1; j < container.size(); j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            // 找到了相同类型的物品，使用 QUICK_MOVE 移动
                            int sourceSlot = slotIndices.get(j);
                            int targetSlot = slotIndices.get(i);
                            
                            client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                            LogUtil.info("Inventory", "容器 QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动物品到目标位置)");
                            
                            // 更新跟踪状态
                            current.set(i, candidate.copy());
                            current.set(j, ItemStack.EMPTY);
                            break;
                        }
                    }
                } else if (have.getCount() < want.getCount()) {
                    // 需要从其他地方补充物品到这里
                    int needed = want.getCount() - have.getCount();
                    int maxStack = want.getMaxCount(); // 使用物品的实际堆叠上限
                    int spaceInTarget = maxStack - have.getCount();
                    int canTransfer = Math.min(needed, spaceInTarget);
                    
                    for (int j = i + 1; j < container.size(); j++) {
                        ItemStack candidate = current.get(j);
                        if (!candidate.isEmpty() && areStacksEqualType(candidate, want)) {
                            int transferAmount = Math.min(canTransfer, candidate.getCount());
                            if (transferAmount > 0) {
                                int sourceSlot = slotIndices.get(j);
                                int targetSlot = slotIndices.get(i);
                                
                                client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                                LogUtil.info("Inventory", "容器 QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (补充 " + transferAmount + " 个物品，堆叠上限: " + maxStack + ")");
                                
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
                // 这个位置应该是空的，但当前有物品，需要移动到后面
                for (int j = container.size() - 1; j > i; j--) {
                    if (current.get(j).isEmpty()) {
                        int sourceSlot = slotIndices.get(i);
                        int targetSlot = slotIndices.get(j);
                        
                        client.interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.QUICK_MOVE, player);
                        LogUtil.info("Inventory", "容器 QUICK_MOVE: 槽位 " + sourceSlot + " -> " + targetSlot + " (移动物品到空位)");
                        
                        // 更新跟踪状态
                        current.set(j, have.copy());
                        current.set(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
        
        LogUtil.info("Inventory", "容器 QUICK_MOVE 目标状态实现完成");
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
                        LogUtil.info("Inventory", "从 ScreenHandler 找到容器库存");
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
                            LogUtil.info("Inventory", "找到容器库存字段: " + fieldName);
                            return inventory;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个字段
                }
            }
            
        } catch (Exception e) {
            LogUtil.warn("Inventory", "获取容器库存失败: " + e.getMessage());
        }
        
        LogUtil.warn("Inventory", "无法获取容器库存");
        return null;
    }
}
