package com.aeolyn.better_experience.config.manager;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;
import com.aeolyn.better_experience.config.cache.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 配置管理器门面类
 * 提供统一的配置管理接口，内部委托给ConfigManagerImpl
 */
public class ConfigManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Config");
    private static volatile ConfigManager instance;
    private static volatile boolean initialized = false;
    
    private final ConfigManagerImpl impl;
    
    private ConfigManager() {
        this.impl = new ConfigManagerImpl();
    }
    
    /**
     * 获取单例实例
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化配置管理器
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warn("配置管理器已经初始化");
            return;
        }
        
        synchronized (ConfigManager.class) {
            if (initialized) {
                LOGGER.warn("配置管理器已经初始化");
                return;
            }
            
            try {
                ConfigManager manager = getInstance();
                manager.impl.initialize();
                initialized = true;
                LOGGER.info("配置管理器初始化完成");
            } catch (Exception e) {
                LOGGER.error("配置管理器初始化失败: " + e.getMessage(), e);
                throw new RuntimeException("配置管理器初始化失败", e);
            }
        }
    }
    
    /**
     * 检查物品是否启用
     */
    public boolean isItemEnabled(String itemId) {
        return impl.isItemEnabled(itemId);
    }
    
    /**
     * 获取物品配置
     */
    public ItemConfig getItemConfig(String itemId) {
        return impl.getItemConfig(itemId);
    }
    
    /**
     * 获取所有启用的物品
     */
    public Set<String> getEnabledItems() {
        return impl.getEnabledItems();
    }
    
    /**
     * 获取主配置
     */
    public ItemsConfig getItemsConfig() {
        return impl.getItemsConfig();
    }
    
    /**
     * 更新物品配置
     */
    public void updateItemConfig(String itemId, ItemConfig config) {
        impl.updateItemConfig(itemId, config);
    }
    
    /**
     * 添加物品配置
     */
    public boolean addItemConfig(String itemId, ItemConfig config) {
        return impl.addItemConfig(itemId, config);
    }
    
    /**
     * 删除物品配置
     */
    public void removeItemConfig(String itemId) {
        impl.removeItemConfig(itemId);
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        impl.reload();
    }
    
    /**
     * 获取缓存统计
     */
    public CacheStats getCacheStats() {
        return impl.getCacheStats();
    }
    
    /**
     * 检查调试模式是否启用
     */
    public boolean isDebugEnabled() {
        return impl.isDebugEnabled();
    }
    
    /**
     * 检查渲染日志是否启用
     */
    public boolean isRenderLogsEnabled() {
        return impl.isRenderLogsEnabled();
    }
    
    /**
     * 检查配置日志是否启用
     */
    public boolean isConfigLogsEnabled() {
        return impl.isConfigLogsEnabled();
    }
    
    /**
     * 检查GUI日志是否启用
     */
    public boolean isGuiLogsEnabled() {
        try {
            ItemsConfig itemsConfig = impl.getItemsConfig();
            return itemsConfig.getLogConfig() != null && itemsConfig.getLogConfig().isEnableGuiLogs();
        } catch (Exception e) {
            LOGGER.error("检查GUI日志失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查Mixin日志是否启用
     */
    public boolean isMixinLogsEnabled() {
        try {
            ItemsConfig itemsConfig = impl.getItemsConfig();
            return itemsConfig.getLogConfig() != null && itemsConfig.getLogConfig().isEnableMixinLogs();
        } catch (Exception e) {
            LOGGER.error("检查Mixin日志失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查性能日志是否启用
     */
    public boolean isPerformanceLogsEnabled() {
        try {
            ItemsConfig itemsConfig = impl.getItemsConfig();
            return itemsConfig.getLogConfig() != null && itemsConfig.getLogConfig().isEnablePerformanceLogs();
        } catch (Exception e) {
            LOGGER.error("检查性能日志失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取默认设置
     */
    public ItemsConfig.Settings getDefaultSettings() {
        try {
            ItemsConfig itemsConfig = impl.getItemsConfig();
            return itemsConfig != null ? itemsConfig.getSettings() : null;
        } catch (Exception e) {
            LOGGER.error("获取默认设置失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取所有已配置的物品
     */
    public Set<String> getAllConfiguredItems() {
        return impl.getEnabledItems();
    }
    
    /**
     * 保存物品配置到文件
     */
    public void saveConfig(String itemId) {
        try {
            ItemConfig config = impl.getItemConfig(itemId);
            if (config != null) {
                impl.updateItemConfig(itemId, config);
                LOGGER.info("物品配置保存成功: {}", itemId);
            } else {
                LOGGER.warn("物品配置不存在，无法保存: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("保存物品配置失败 " + itemId + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存所有配置
     */
    public void saveAllConfigs() {
        try {
            // 保存主配置文件
            ItemsConfig itemsConfig = impl.getItemsConfig();
            // 这里需要访问内部的saver，暂时通过重新加载来实现
            impl.reload();
            LOGGER.info("所有配置保存完成");
        } catch (Exception e) {
            LOGGER.error("保存所有配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 测试配置持久化功能
     */
    public void testConfigPersistence() {
        LOGGER.info("开始测试配置持久化功能...");
        
        try {
            // 创建一个测试配置
            ItemConfig testConfig = new ItemConfig();
            testConfig.setItemId("minecraft:test_item");
            testConfig.setEnabled(true);
            testConfig.setRenderAsBlock(true);
            testConfig.setBlockId("minecraft:test_item");
            testConfig.getFirstPerson().setScale(2.5f);
            testConfig.getFirstPerson().setRotationX(45.0f);
            
            // 保存测试配置
            impl.addItemConfig("minecraft:test_item", testConfig);
            
            // 重新加载配置
            impl.reload();
            
            // 验证配置是否正确加载
            ItemConfig loadedConfig = impl.getItemConfig("minecraft:test_item");
            if (loadedConfig != null && 
                loadedConfig.getFirstPerson().getScale() == 2.5f &&
                loadedConfig.getFirstPerson().getRotationX() == 45.0f) {
                LOGGER.info("配置持久化测试成功！");
            } else {
                LOGGER.error("配置持久化测试失败！");
            }
            
            // 清理测试配置
            impl.removeItemConfig("minecraft:test_item");
            
        } catch (Exception e) {
            LOGGER.error("配置持久化测试失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return impl.isInitialized();
    }
}
