package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.service.InventorySortService;
import com.aeolyn.better_experience.inventory.service.InventorySortServiceImpl;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * 背包排序控制器
 * 简化架构，直接使用服务层，支持自定义比较器
 */
public class InventorySortController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Inventory");
    private static volatile InventorySortController instance;
    
    private final InventorySortService sortService;
    
    private InventorySortController() {
        this.sortService = new InventorySortServiceImpl();
    }
    
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
     * 整理背包（使用默认比较器）
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode) {
        sortInventory(sortMode, false);
    }
    
    /**
     * 整理背包（使用默认比较器，支持合并模式）
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        sortInventory(sortMode, mergeFirst, SortComparatorFactory.createComparator(sortMode));
    }
    
    /**
     * 整理背包（支持自定义比较器）
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        sortService.sortInventory(sortMode, mergeFirst, comparator);
    }
    
    /**
     * 整理容器（使用默认比较器）
     */
    public void sortContainer(Inventory container) {
        sortContainer(container, InventorySortConfig.SortMode.NAME, false);
    }
    
    /**
     * 整理容器（使用默认比较器，支持合并模式）
     */
    public void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        sortContainer(container, sortMode, mergeFirst, SortComparatorFactory.createComparator(sortMode));
    }
    
    /**
     * 整理容器（支持自定义比较器）
     */
    public void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        sortService.sortContainer(container, sortMode, mergeFirst, comparator);
    }
    
    /**
     * 智能排序：根据鼠标位置决定排序背包还是容器
     */
    public void smartSortByMousePosition() {
        sortService.smartSortByMousePosition();
    }
    
    /**
     * 简单的选择排序算法（使用默认比较器）
     */
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        simpleSelectionSort(sortMode, mergeFirst, SortComparatorFactory.createComparator(sortMode));
    }
    
    /**
     * 简单的选择排序算法（支持自定义比较器）
     */
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst, Comparator<ItemStack> comparator) {
        sortService.simpleSelectionSort(sortMode, mergeFirst, comparator);
    }
    
    /**
     * 测试潜影盒支持
     */
    public void testShulkerBoxSupport() {
        sortService.testShulkerBoxSupport();
    }
}
