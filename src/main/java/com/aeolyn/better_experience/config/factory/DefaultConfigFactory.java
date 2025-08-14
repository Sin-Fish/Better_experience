package com.aeolyn.better_experience.config.factory;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;
import com.aeolyn.better_experience.config.LogConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认配置工厂实现
 */
public class DefaultConfigFactory implements ConfigFactory {
    
    @Override
    public ItemsConfig createDefaultItemsConfig() {
        ItemsConfig config = new ItemsConfig();
        config.setEnabledItems(new ArrayList<>());
        config.setSettings(createDefaultSettings());
        config.setLogConfig(createDefaultLogConfig());
        return config;
    }
    
    @Override
    public ItemConfig createDefaultItemConfig(String itemId) {
        ItemConfig config = new ItemConfig();
        config.setItemId(itemId);
        config.setEnabled(true);
        config.setRenderAsBlock(true);
        config.setBlockId(itemId);
        config.setRenderAsEntity(false);
        config.setEntityType("");
        
        // 设置默认渲染参数
        config.setFirstPerson(createDefaultRenderSettings());
        config.setThirdPerson(createDefaultRenderSettings());
        
        return config;
    }
    
    @Override
    public LogConfig createDefaultLogConfig() {
        LogConfig config = new LogConfig();
        config.setEnableRenderLogs(false);
        config.setEnableConfigLogs(true);
        config.setEnableGuiLogs(false);
        config.setEnableMixinLogs(false);
        config.setEnablePerformanceLogs(false);
        return config;
    }
    
    @Override
    public ItemConfig.RenderSettings createDefaultRenderSettings() {
        ItemConfig.RenderSettings settings = new ItemConfig.RenderSettings();
        settings.setScale(1.0f);
        settings.setRotationX(0.0f);
        settings.setRotationY(0.0f);
        settings.setRotationZ(0.0f);
        settings.setTranslateX(0.0f);
        settings.setTranslateY(0.0f);
        settings.setTranslateZ(0.0f);
        return settings;
    }
    
    private ItemsConfig.Settings createDefaultSettings() {
        ItemsConfig.Settings settings = new ItemsConfig.Settings();
        // 使用现有的字段，Settings类没有setter方法，使用默认值
        return settings;
    }
}
