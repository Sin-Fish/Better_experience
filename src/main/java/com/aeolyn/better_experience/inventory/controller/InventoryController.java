package com.aeolyn.better_experience.inventory.controller;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.service.InventorySortService;
import com.aeolyn.better_experience.inventory.service.InventoryTransferService;
import com.aeolyn.better_experience.inventory.service.InventorySortServiceImpl;
import com.aeolyn.better_experience.inventory.service.InventoryTransferServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一的背包控制器
 * 整合排序和转移功能，提供统一的接口
 */
public class InventoryController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Inventory");
    private static volatile InventoryController instance;
    
    private final InventorySortService sortService;
    private final InventoryTransferService transferService;
    private final ConfigManager configManager;
    
    private InventoryController() {
        this.configManager = ConfigManager.getInstance();
        this.sortService = new InventorySortServiceImpl();
        this.transferService = new InventoryTransferServiceImpl(configManager);
    }
    
    /**
     * 获取单例实例
     */
    public static InventoryController getInstance() {
        if (instance == null) {
            synchronized (InventoryController.class) {
                if (instance == null) {
                    instance = new InventoryController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化背包控制器
     */
    public static void initialize() {
        LogUtil.info("Inventory", "初始化背包控制器");
        getInstance();
    }
    
    /**
     * 获取排序服务
     */
    public InventorySortService getSortService() {
        return sortService;
    }
    
    /**
     * 获取转移服务
     */
    public InventoryTransferService getTransferService() {
        return transferService;
    }
}
