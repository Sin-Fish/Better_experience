package com.aeolyn.better_experience.common.config.loader;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;

/**
 * 配置加载器接口
 */
public interface ConfigLoader {
    
    /**
     * 加载主配置文件
     */
    ItemsConfig loadItemsConfig() throws ConfigLoadException;
    
    /**
     * 加载单个物品配置
     */
    ItemConfig loadItemConfig(String itemId) throws ConfigLoadException;
    
    /**
     * 检查配置文件是否存在
     */
    boolean configExists(String itemId);
    
    /**
     * 获取所有可用的物品配置ID
     */
    java.util.Set<String> getAvailableItemConfigs();
    
    /**
     * 加载副手限制配置
     */
    OffHandRestrictionConfig loadOffHandRestrictionConfig() throws ConfigLoadException;
}
