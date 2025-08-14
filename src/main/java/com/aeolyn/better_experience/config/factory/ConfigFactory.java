package com.aeolyn.better_experience.config.factory;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;

/**
 * 配置工厂接口
 */
public interface ConfigFactory {
    
    /**
     * 创建默认主配置
     */
    ItemsConfig createDefaultItemsConfig();
    
    /**
     * 创建默认物品配置
     */
    ItemConfig createDefaultItemConfig(String itemId);
    
    /**
     * 创建默认日志配置
     */
    com.aeolyn.better_experience.config.LogConfig createDefaultLogConfig();
    
    /**
     * 创建默认渲染设置
     */
    ItemConfig.RenderSettings createDefaultRenderSettings();
}
