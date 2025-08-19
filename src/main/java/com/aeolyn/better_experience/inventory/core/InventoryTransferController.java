package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.service.InventoryTransferService;
import com.aeolyn.better_experience.inventory.service.InventoryTransferServiceImpl;
import net.minecraft.inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 智能转移控制器
 * 使用新的服务结构，提供统一的转移接口
 */
public class InventoryTransferController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Transfer");
    private static volatile InventoryTransferController instance;
    
    private final InventoryTransferService transferService;
    
    private InventoryTransferController() {
        this.transferService = new InventoryTransferServiceImpl();
    }
    
    /**
     * 获取单例实例
     */
    public static InventoryTransferController getInstance() {
        if (instance == null) {
            synchronized (InventoryTransferController.class) {
                if (instance == null) {
                    instance = new InventoryTransferController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化智能转移控制器
     */
    public static void initialize() {
        LogUtil.info("Transfer", "初始化智能转移控制器");
        getInstance();
    }
    
    /**
     * 智能一键存入/取出功能（Shift+R）
     * 根据配置选择判断逻辑
     */
    public void smartTransferItems() {
        transferService.smartTransferItems();
    }
    
    /**
     * 一键存入容器（兼容旧版本调用）
     */
    public void depositToContainer(Inventory container) {
        transferService.depositToContainer(container);
    }
    
    /**
     * 一键从容器取出（兼容旧版本调用）
     */
    public void withdrawFromContainer(Inventory container) {
        transferService.withdrawFromContainer(container);
    }
}
