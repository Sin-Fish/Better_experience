package com.aeolyn.better_experience.config.saver;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;
import com.aeolyn.better_experience.config.exception.ConfigSaveException;

/**
 * 配置保存器接口
 */
public interface ConfigSaver {
    
    /**
     * 保存主配置文件
     */
    void saveItemsConfig(ItemsConfig config) throws ConfigSaveException;
    
    /**
     * 保存单个物品配置
     */
    void saveItemConfig(String itemId, ItemConfig config) throws ConfigSaveException;
    
    /**
     * 删除物品配置文件
     */
    void deleteItemConfig(String itemId) throws ConfigSaveException;
    
    /**
     * 检查是否支持保存操作
     */
    boolean isWritable();
}
