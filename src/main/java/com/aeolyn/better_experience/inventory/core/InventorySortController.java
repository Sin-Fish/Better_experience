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
        try {
            LogUtil.info("Inventory", "开始执行背包整理，排序模式: " + sortMode.getDisplayName());
            
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                LogUtil.warn("Inventory", "玩家不存在，无法整理背包");
                return;
            }

            Inventory inventory = player.getInventory();
            LogUtil.info("Inventory", "获取到玩家背包，开始收集物品信息");
            
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
            
            // 2. 创建实际物品列表（去掉空位）
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                items.add(inventoryItems[i]);
            }
            
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
            
            LogUtil.info("Inventory", "背包整理完成，模式: " + sortMode.getDisplayName() + "，共整理 " + items.size() + " 个物品");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "整理背包失败", e);
        }
    }
    
    /**
     * 整理容器
     */
    public void sortContainer(Inventory container) {
        try {
            // 1. 将容器信息复制到数组中
            ItemStack[] containerItems = new ItemStack[container.size()];
            int itemCount = 0;
            
            for (int i = 0; i < container.size(); i++) {
                ItemStack stack = container.getStack(i);
                if (!stack.isEmpty()) {
                    containerItems[itemCount++] = stack.copy(); // 深拷贝避免引用问题
                }
            }
            
            // 2. 创建实际物品列表（去掉空位）
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < itemCount; i++) {
                items.add(containerItems[i]);
            }
            
            if (items.isEmpty()) {
                LogUtil.info("Inventory", "容器为空，无需整理");
                return;
            }
            
            // 3. 对物品列表进行排序（默认按名称排序）
            sortItems(items, InventorySortConfig.SortMode.NAME);
            
            // 4. 清空容器
            for (int i = 0; i < container.size(); i++) {
                container.setStack(i, ItemStack.EMPTY);
            }
            
            // 5. 将排序后的物品重新放入容器
            for (int i = 0; i < items.size() && i < container.size(); i++) {
                container.setStack(i, items.get(i));
            }
            
            LogUtil.info("Inventory", "容器整理完成，共整理 " + items.size() + " 个物品");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "整理容器失败", e);
        }
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
}
