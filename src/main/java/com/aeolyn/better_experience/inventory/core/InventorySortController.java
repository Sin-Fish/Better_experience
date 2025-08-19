package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.controller.InventoryController;
import net.minecraft.inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 背包排序控制器
 * 使用新的服务架构，提供统一的排序接口
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
        InventoryController.getInstance().getSortService().sortInventory(sortMode);
    }
    
    /**
     * 整理背包（支持合并模式）
     */
    public void sortInventory(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        InventoryController.getInstance().getSortService().sortInventory(sortMode, mergeFirst);
    }
    
    /**
     * 整理容器（支持合并模式）
     */
    public void sortContainer(Inventory container, InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        InventoryController.getInstance().getSortService().sortContainer(container, sortMode, mergeFirst);
    }
    
    /**
     * 整理容器（默认不合并）
     */
    public void sortContainer(Inventory container) {
        InventoryController.getInstance().getSortService().sortContainer(container);
    }
    
    /**
     * 智能排序：根据鼠标位置决定排序背包还是容器
     */
    public void smartSortByMousePosition() {
        InventoryController.getInstance().getSortService().smartSortByMousePosition();
    }
    
    /**
     * 简单的选择排序算法
     */
    public void simpleSelectionSort(InventorySortConfig.SortMode sortMode, boolean mergeFirst) {
        InventoryController.getInstance().getSortService().simpleSelectionSort(sortMode, mergeFirst);
    }
    
    /**
     * 测试潜影盒支持
     */
    public void testShulkerBoxSupport() {
        InventoryController.getInstance().getSortService().testShulkerBoxSupport();
    }
}
