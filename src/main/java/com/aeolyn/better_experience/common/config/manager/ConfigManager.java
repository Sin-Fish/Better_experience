package com.aeolyn.better_experience.common.config.manager;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.common.config.cache.CacheStats;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.common.config.ModConfig;

import java.util.Set;

/**
 * 配置管理器门面类
 * 提供统一的配置管理接口，内部委托给ConfigManagerImpl
 */
public class ConfigManager {
    
    // 使用统一日志工具
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
            LogUtil.warn(LogUtil.MODULE_CONFIG, "配置管理器已经初始化");
            return;
        }
        
        synchronized (ConfigManager.class) {
            if (initialized) {
                LogUtil.warn(LogUtil.MODULE_CONFIG, "配置管理器已经初始化");
                return;
            }
            
            try {
                ConfigManager manager = getInstance();
                manager.impl.initialize();
                initialized = true;
                LogUtil.logCompletion(LogUtil.MODULE_CONFIG, "配置管理器");
            } catch (Exception e) {
                LogUtil.logFailure(LogUtil.MODULE_CONFIG, "配置管理器初始化", e);
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
     * 导入物品配置（不更新主配置）
     */
    public boolean importItemConfig(String itemId, ItemConfig config) {
        return impl.importItemConfig(itemId, config);
    }
    
    /**
     * 更新主配置
     */
    public void updateItemsConfig(ItemsConfig itemsConfig) {
        impl.updateItemsConfig(itemsConfig);
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
            LogUtil.error(LogUtil.MODULE_CONFIG, "检查GUI日志失败: {}", e.getMessage(), e);
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
            LogUtil.error(LogUtil.MODULE_CONFIG, "检查Mixin日志失败: {}", e.getMessage(), e);
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
            LogUtil.error(LogUtil.MODULE_CONFIG, "检查性能日志失败: {}", e.getMessage(), e);
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
            LogUtil.error(LogUtil.MODULE_CONFIG, "获取默认设置失败: {}", e.getMessage(), e);
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
                LogUtil.logConfigSave(itemId);
            } else {
                LogUtil.warn(LogUtil.MODULE_CONFIG, "物品配置不存在，无法保存: {}", itemId);
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "保存物品配置失败 {}: {}", itemId, e.getMessage(), e);
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
            LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "所有配置保存");
        } catch (Exception e) {
            LogUtil.logFailure(LogUtil.MODULE_CONFIG, "保存所有配置", e);
        }
    }
    
    /**
     * 测试配置持久化功能
     */
    public void testConfigPersistence() {
        LogUtil.info(LogUtil.MODULE_CONFIG, "开始测试配置持久化功能...");
        
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
                LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "配置持久化测试");
            } else {
                LogUtil.error(LogUtil.MODULE_CONFIG, "配置持久化测试失败！");
            }
            
            // 清理测试配置
            impl.removeItemConfig("minecraft:test_item");
            
        } catch (Exception e) {
            LogUtil.logFailure(LogUtil.MODULE_CONFIG, "配置持久化测试", e);
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return impl.isInitialized();
    }
    
    /**
     * 获取副手限制配置
     */
    public OffHandRestrictionConfig getOffHandRestrictionConfig() {
        return impl.getOffHandRestrictionConfig();
    }
    
    /**
     * 保存副手限制配置
     */
    public void saveOffHandRestrictionConfig() {
        impl.saveOffHandRestrictionConfig();
    }
    
    /**
     * 更新副手限制配置
     */
    public void updateOffHandRestrictionConfig(OffHandRestrictionConfig config) {
        impl.updateOffHandRestrictionConfig(config);
    }
    
    /**
     * 通用配置获取方法
     */
    public <T> T getConfig(Class<T> configClass) {
        if (configClass == InventorySortConfig.class) {
            return (T) getInventorySortConfig();
        } else if (configClass == OffHandRestrictionConfig.class) {
            return (T) getOffHandRestrictionConfig();
        } else if (configClass == ItemsConfig.class) {
            return (T) getItemsConfig();
        }
        throw new IllegalArgumentException("不支持的配置类型: " + configClass.getName());
    }
    
    /**
     * 通用配置保存方法
     */
    public <T> void saveConfig(T config) {
        if (config instanceof InventorySortConfig) {
            updateInventorySortConfig((InventorySortConfig) config);
        } else if (config instanceof OffHandRestrictionConfig) {
            updateOffHandRestrictionConfig((OffHandRestrictionConfig) config);
        } else if (config instanceof ItemsConfig) {
            updateItemsConfig((ItemsConfig) config);
        } else {
            throw new IllegalArgumentException("不支持的配置类型: " + config.getClass().getName());
        }
    }
    
    /**
     * 获取背包排序配置
     */
    public InventorySortConfig getInventorySortConfig() {
        return impl.getInventorySortConfig();
    }
    
    /**
     * 更新背包排序配置
     */
    public void updateInventorySortConfig(InventorySortConfig config) {
        impl.updateInventorySortConfig(config);
    }
    
    // ==================== 通用配置管理 ====================
    
    /**
     * 获取通用配置
     */
    public ModConfig getModConfig() {
        return impl.getModConfig();
    }
    
    /**
     * 更新通用配置
     */
    public void updateModConfig(ModConfig config) {
        impl.updateModConfig(config);
    }
    
    /**
     * 检查模块是否启用
     */
    public boolean isModuleEnabled(String moduleName) {
        return impl.isModuleEnabled(moduleName);
    }
    
    /**
     * 启用/禁用模块
     */
    public void setModuleEnabled(String moduleName, boolean enabled) {
        impl.setModuleEnabled(moduleName, enabled);
    }
    
    /**
     * 检查调试模式是否启用
     */
    public boolean isDebugModeEnabled() {
        return impl.isDebugModeEnabled();
    }
    
    /**
     * 设置调试模式
     */
    public void setDebugMode(boolean enabled) {
        impl.setDebugMode(enabled);
    }
    
    /**
     * 获取自动保存间隔
     */
    public int getAutoSaveInterval() {
        return impl.getAutoSaveInterval();
    }
    
    /**
     * 设置自动保存间隔
     */
    public void setAutoSaveInterval(int interval) {
        impl.setAutoSaveInterval(interval);
    }
    
    /**
     * 检查3D渲染模块是否启用
     */
    public boolean isRender3dEnabled() {
        return impl.isRender3dEnabled();
    }
    
    /**
     * 检查副手限制模块是否启用
     */
    public boolean isOffhandRestrictionEnabled() {
        return impl.isOffhandRestrictionEnabled();
    }
    
    /**
     * 检查背包排序模块是否启用
     */
    public boolean isInventorySortEnabled() {
        return impl.isInventorySortEnabled();
    }
}
