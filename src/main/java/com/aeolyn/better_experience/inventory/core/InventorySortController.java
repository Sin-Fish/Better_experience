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

				performReorderWithClicks(player, current, desired);
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

            // 执行容器重排
            performContainerReorderWithClicks(player, container, current, desired);
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
     * 合并相同物品并排序
     */
    private List<ItemStack> mergeAndSortItems(List<ItemStack> items, InventorySortConfig.SortMode sortMode) {
        LogUtil.info("Inventory", "开始合并相同物品");
        
        // 按物品类型分组
        Map<String, List<ItemStack>> itemGroups = new HashMap<>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            
            String key = getItemKey(stack);
            itemGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(stack.copy());
        }
        
        // 合并每组物品
        List<ItemStack> merged = new ArrayList<>();
        for (List<ItemStack> group : itemGroups.values()) {
            if (group.isEmpty()) continue;
            
            ItemStack base = group.get(0);
            int totalCount = group.stream().mapToInt(ItemStack::getCount).sum();
            int maxStack = base.getMaxCount();
            
            // 计算需要多少个完整堆叠
            int fullStacks = totalCount / maxStack;
            int remainder = totalCount % maxStack;
            
            // 添加完整堆叠
            for (int i = 0; i < fullStacks; i++) {
                ItemStack fullStack = base.copy();
                fullStack.setCount(maxStack);
                merged.add(fullStack);
            }
            
            // 添加剩余部分
            if (remainder > 0) {
                ItemStack partialStack = base.copy();
                partialStack.setCount(remainder);
                merged.add(partialStack);
            }
        }
        
        LogUtil.info("Inventory", "合并完成，原物品数: " + items.stream().filter(s -> !s.isEmpty()).count() + "，合并后: " + merged.size());
        
        // 排序合并后的物品
        sortItems(merged, sortMode);
        return merged;
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
         String itemId = Registries.ITEM.getId(stack.getItem()).toString();
         // 简化版本，只使用物品ID，不考虑NBT
         return itemId;
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
         
         for (int i = 0; i < 27; i++) {
             ItemStack want = desired.get(i);
             ItemStack have = current.get(i);
             if (areStacksEqualExact(have, want)) {
                 continue;
             }
             
             int j = -1;
             for (int k = i + 1; k < 27; k++) {
                 if (areStacksEqualExact(current.get(k), want)) {
                     j = k;
                     break;
                 }
             }
             if (j == -1) {
                 if (want.isEmpty() && !have.isEmpty()) {
                     for (int k = 26; k > i; k--) {
                         if (current.get(k).isEmpty()) {
                             j = k;
                             break;
                         }
                     }
                 }
             }
             
             if (j != -1 && j != i) {
                 int slotA = mainSlotIds.get(i);
                 int slotB = mainSlotIds.get(j);
                 clickSwap(client, syncId, slotA, slotB, player);
                 ItemStack tmp = current.get(i);
                 current.set(i, current.get(j));
                 current.set(j, tmp);
             }
         }
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
         
         for (int i = 0; i < container.size(); i++) {
             ItemStack want = desired.get(i);
             ItemStack have = current.get(i);
             if (areStacksEqualExact(have, want)) {
                 continue;
             }
             
             int j = -1;
             for (int k = i + 1; k < container.size(); k++) {
                 if (areStacksEqualExact(current.get(k), want)) {
                     j = k;
                     break;
                 }
             }
             if (j == -1) {
                 if (want.isEmpty() && !have.isEmpty()) {
                     for (int k = container.size() - 1; k > i; k--) {
                         if (current.get(k).isEmpty()) {
                             j = k;
                             break;
                         }
                     }
                 }
             }
             
             if (j != -1 && j != i) {
                 int slotA = slotIndices.get(i);
                 int slotB = slotIndices.get(j);
                 clickSwap(client, syncId, slotA, slotB, player);
                 ItemStack tmp = current.get(i);
                 current.set(i, current.get(j));
                 current.set(j, tmp);
             }
         }
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
      * 严格比较两个物品（物品ID与数量）。二者都为空视为相等
      */
     private boolean areStacksEqualExact(ItemStack a, ItemStack b) {
         if (a.isEmpty() && b.isEmpty()) return true;
         if (a.isEmpty() || b.isEmpty()) return false;
         return a.isOf(b.getItem()) && a.getCount() == b.getCount();
     }

    /**
     * 根据鼠标位置智能排序（精确版，使用getSlotAt检测槽位）
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
            
            // 输出所有方法名以调试
            java.lang.reflect.Method[] methods = handledScreen.getClass().getDeclaredMethods();
            StringBuilder methodList = new StringBuilder("handledScreen 方法列表: ");
            for (java.lang.reflect.Method m : methods) {
                methodList.append(m.getName()).append(", ");
            }
            LogUtil.info("Inventory", methodList.toString());

            // 输出当前类方法列表（已有）
            // 获取超类
            Class<?> superClass = handledScreen.getClass().getSuperclass();
            LogUtil.info("Inventory", "超类名: " + superClass.getName());

            // 输出超类方法列表 (已有)

            // 添加过滤: 找出签名匹配 (double, double) 的方法
            java.lang.reflect.Method[] superMethods = superClass.getDeclaredMethods(); // 添加声明
            StringBuilder matchingMethods = new StringBuilder("匹配签名 (double, double) 的方法: ");
            String autoMethodName = null;
            for (java.lang.reflect.Method m : superMethods) {
                Class<?>[] params = m.getParameterTypes();
                String signature = m.getName() + " params: " + Arrays.toString(params) + " return: " + m.getReturnType().getSimpleName();
                LogUtil.info("Inventory", "方法签名: " + signature);
                if (params.length == 2 && params[0] == double.class && params[1] == double.class && m.getReturnType() == net.minecraft.screen.slot.Slot.class) {
                    matchingMethods.append(m.getName()).append(", ");
                    if (autoMethodName == null) autoMethodName = m.getName(); // 使用第一个匹配
                }
            }
            LogUtil.info("Inventory", matchingMethods.toString());

            // 声明 slot
            net.minecraft.screen.slot.Slot slot = null;

            // 在Creative if 中加强调试和动态查找
            String className = handledScreen.getClass().getName(); // 添加声明
            if (className.contains("CreativeInventoryScreen") || className.equals("net.minecraft.class_10260")) {
                LogUtil.info("Inventory", "检测到CreativeInventoryScreen，开始调试输出");
                // 输出当前类方法列表 (已有)
                java.lang.reflect.Method[] creativeMethods = handledScreen.getClass().getDeclaredMethods();
                StringBuilder creativeMethodList = new StringBuilder("Creative类方法列表: ");
                for (java.lang.reflect.Method m : creativeMethods) {
                    creativeMethodList.append(m.getName()).append(", ");
                }
                LogUtil.info("Inventory", creativeMethodList.toString());
                // 签名匹配并动态选择方法
                StringBuilder creativeMatching = new StringBuilder("Creative匹配 (double, double) 方法: ");
                String creativeMethodName = null;
                for (java.lang.reflect.Method m : creativeMethods) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length == 2 && params[0] == double.class && params[1] == double.class) { // 移除 return 检查
                        creativeMatching.append(m.getName() + " return: " + m.getReturnType().getSimpleName()).append(", ");
                        if (creativeMethodName == null) creativeMethodName = m.getName(); // 第一个匹配
                    }
                }
                LogUtil.info("Inventory", creativeMatching.toString());
                if (creativeMethodName != null) {
                    try {
                        LogUtil.info("Inventory", "动态尝试调用 " + creativeMethodName + ", 输入: (" + mouseX + ", " + mouseY + ")"); 
                        java.lang.reflect.Method getSlotAtMethod = handledScreen.getClass().getDeclaredMethod(creativeMethodName, double.class, double.class);
                        getSlotAtMethod.setAccessible(true);
                        slot = (net.minecraft.screen.slot.Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                        LogUtil.info("Inventory", "Creative动态调用 " + creativeMethodName + " 成功, slot " + (slot == null ? "null" : "not null"));
                        if (slot != null) {
                            LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 库存类型: " + slot.inventory.getClass().getSimpleName());
                        }
                    } catch (Exception e) {
                        LogUtil.warn("Inventory", "Creative动态调用 " + creativeMethodName + " 失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.warn("Inventory", "未找到匹配签名的方法，尝试备用: method_2385, method_2383");
                    String[] backupNames = {"method_2385", "method_2383"};
                    for (String backup : backupNames) {
                        try {
                            java.lang.reflect.Method getSlotAtMethod = handledScreen.getClass().getDeclaredMethod(backup, double.class, double.class);
                            getSlotAtMethod.setAccessible(true);
                            slot = (net.minecraft.screen.slot.Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                            LogUtil.info("Inventory", "Creative备用 " + backup + " 成功调用");
                            if (slot != null) {
                                LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 库存类型: " + slot.inventory.getClass().getSimpleName());
                            }
                            break;
                        } catch (Exception e) {
                            LogUtil.warn("Inventory", "备用 " + backup + " 失败: " + e.getMessage());
                        }
                    }
                }
            }
            // 如果 slot 仍为 null，继续原循环
            if (slot == null) {
                Class<?> currentClass = handledScreen.getClass();
                LogUtil.info("Inventory", "当前类名: " + currentClass.getName());

                while (currentClass != null && slot == null) {
                    LogUtil.info("Inventory", "在类 " + currentClass.getName() + " 中查找 getSlotAt");

                    java.lang.reflect.Method[] classMethods = currentClass.getDeclaredMethods();
                    StringBuilder classMethodList = new StringBuilder("当前类方法列表: ");
                    for (java.lang.reflect.Method m : classMethods) {
                        classMethodList.append(m.getName()).append(", ");
                    }
                    LogUtil.info("Inventory", classMethodList.toString());

                    String[] possibleMethodNames = {"getSlotAt", "method_5452", "method_2385", "method_1542", "method_64240", "method_2383", "method_64241", "method_2381", "method_2378"}; // 添加日志中方法
                    LogUtil.info("Inventory", "开始尝试调用 getSlotAt，方法列表: " + Arrays.toString(possibleMethodNames));
                    for (String methodName : possibleMethodNames) {
                        try {
                            java.lang.reflect.Method getSlotAtMethod = currentClass.getDeclaredMethod(methodName, double.class, double.class);
                            getSlotAtMethod.setAccessible(true);
                            slot = (net.minecraft.screen.slot.Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                            LogUtil.info("Inventory", "成功调用 getSlotAt，使用方法名: " + methodName + " 在类 " + currentClass.getName());
                            if (slot != null) {
                                LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 库存类型: " + slot.inventory.getClass().getSimpleName());
                                LogUtil.info("Inventory", "槽位坐标: (" + slot.x + ", " + slot.y + ")"); // 添加坐标输出
                            } else {
                                LogUtil.warn("Inventory", "调用成功但 slot 为 null");
                            }
                            break;
                        } catch (NoSuchMethodException e) {
                            LogUtil.warn("Inventory", "方法 " + methodName + " 不存在在 " + currentClass.getName() + ": " + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            LogUtil.error("Inventory", "调用 " + methodName + " 失败在 " + currentClass.getName() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if (slot == null) {
                        currentClass = currentClass.getSuperclass();
                    }
                }
            }
            // 强制进入手动查找如果 slot == null
            if (slot == null) {
                LogUtil.info("Inventory", "反射返回 null，强制手动查找");
                // 获取 handler 字段
                net.minecraft.screen.ScreenHandler handler = null;
                String[] handlerFieldNames = {"handler", "field_22718"}; // Yarn 1.21.6 确切名称
                Class<?> screenClass = handledScreen.getClass();
                while (screenClass != null && handler == null) {
                    for (String fieldName : handlerFieldNames) {
                        try {
                            java.lang.reflect.Field handlerField = screenClass.getDeclaredField(fieldName);
                            handlerField.setAccessible(true);
                            handler = (net.minecraft.screen.ScreenHandler) handlerField.get(handledScreen);
                            LogUtil.info("Inventory", "成功获取 handler 使用字段: " + fieldName);
                            break;
                        } catch (NoSuchFieldException e) {
                            // ignore
                        }
                    }
                    if (handler == null) {
                        screenClass = screenClass.getSuperclass();
                    }
                }
                if (handler == null) {
                    LogUtil.warn("Inventory", "无法获取 handler 字段");
                    // 输出所有字段
                    StringBuilder fieldsList = new StringBuilder("screenClass 所有字段: ");
                    for (java.lang.reflect.Field f : screenClass.getDeclaredFields()) {
                        fieldsList.append(f.getName()).append(", ");
                    }
                    LogUtil.info("Inventory", fieldsList.toString());
                }
                // 获取 slots 列表
                java.lang.reflect.Field slotsField = null;
                String[] slotsFieldNames = {"slots", "field_5468"}; // Yarn 1.21.6 确切名称
                Class<?> handlerClass = handler.getClass();
                while (handlerClass != null && slotsField == null) {
                    for (String fieldName : slotsFieldNames) {
                        try {
                            slotsField = handlerClass.getDeclaredField(fieldName);
                            slotsField.setAccessible(true);
                            LogUtil.info("Inventory", "成功获取 slots 使用字段: " + fieldName);
                            break;
                        } catch (NoSuchFieldException e) {
                            // ignore
                        }
                    }
                    if (slotsField == null) {
                        handlerClass = handlerClass.getSuperclass();
                    }
                }
                if (slotsField != null) {
                    @SuppressWarnings("unchecked")
                    java.util.List<net.minecraft.screen.slot.Slot> slots = (java.util.List<net.minecraft.screen.slot.Slot>) slotsField.get(handler);
                    for (net.minecraft.screen.slot.Slot s : slots) {
                        if (mouseX >= s.x && mouseX <= s.x + 16 && mouseY >= s.y && mouseY <= s.y + 16) { // 改为 <= 以包含边界
                            slot = s;
                            LogUtil.info("Inventory", "手动找到槽位: " + slot.id + ", 库存类型: " + slot.inventory.getClass().getSimpleName());
                            break;
                        }
                    }
                    if (slot == null) {
                        LogUtil.warn("Inventory", "手动查找未找到槽位");
                    }
                } else {
                    LogUtil.warn("Inventory", "无法获取 slots 字段");
                }
            }
            
            if (slot == null) {
                LogUtil.warn("Inventory", "所有方法尝试失败，无法获取槽位");
            } else {
                net.minecraft.inventory.Inventory inventory = slot.inventory;
                boolean isPlayerInventory = inventory == client.player.getInventory();
                LogUtil.info("Inventory", "找到槽位: " + slot.id + ", 环境: " + (isPlayerInventory ? "玩家背包" : "容器"));
                if (isPlayerInventory) {
                    LogUtil.info("Inventory", "鼠标在背包槽位上，执行背包排序");
                    sortInventory(InventorySortConfig.SortMode.NAME, true); // 合并模式
                } else {
                    LogUtil.info("Inventory", "鼠标在容器槽位上，执行容器排序");
                    sortContainer(inventory, InventorySortConfig.SortMode.NAME, true); // 合并模式
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
}
