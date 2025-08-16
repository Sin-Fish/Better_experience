package com.aeolyn.better_experience.common.config.saver;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigSaveException;

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
    
    /**
     * 保存副手限制配置
     */
    void saveOffHandRestrictionConfig(OffHandRestrictionConfig config) throws ConfigSaveException;
}
