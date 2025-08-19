package com.aeolyn.better_experience.inventory.service;

import net.minecraft.inventory.Inventory;

/**
 * 背包转移服务接口
 * 定义物品转移的核心方法
 */
public interface InventoryTransferService {
    
    /**
     * 智能转移物品
     */
    void smartTransferItems();
    
    /**
     * 存入容器
     * @param container 目标容器
     */
    void depositToContainer(Inventory container);
    
    /**
     * 从容器取出
     * @param container 源容器
     */
    void withdrawFromContainer(Inventory container);
}
