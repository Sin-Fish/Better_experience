package com.aeolyn.better_experience.common.config.manager;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.cache.ConfigCache;
import com.aeolyn.better_experience.common.config.cache.MemoryConfigCache;
import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;
import com.aeolyn.better_experience.common.config.exception.ConfigSaveException;
import com.aeolyn.better_experience.common.config.factory.ConfigFactory;
import com.aeolyn.better_experience.common.config.factory.DefaultConfigFactory;
import com.aeolyn.better_experience.common.config.loader.ConfigLoader;
import com.aeolyn.better_experience.common.config.loader.FileConfigLoader;
import com.aeolyn.better_experience.common.config.saver.ConfigSaver;
import com.aeolyn.better_experience.common.config.saver.FileConfigSaver;
import com.aeolyn.better_experience.common.config.validator.ConfigValidator;
import com.aeolyn.better_experience.common.config.validator.ItemConfigValidator;
import com.aeolyn.better_experience.common.config.validator.ValidationResult;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.common.util.ConfigValidationUtil;

import java.util.Set;

/**
 * 配置管理器实现
 * 作为各个组件的协调者，提供统一的配置管理接口
 */
public class ConfigManagerImpl {
    
    // 使用统一日志工具
    
    private final ConfigLoader loader;
    private final ConfigSaver saver;
    private final ConfigValidator validator;
    private final ConfigFactory factory;
    private final ConfigCache cache;
    
    private volatile boolean initialized = false;
    
    public ConfigManagerImpl() {
        this.factory = new DefaultConfigFactory();
        this.loader = new FileConfigLoader(factory);
        this.saver = new FileConfigSaver();
        this.validator = new ItemConfigValidator();
        this.cache = new MemoryConfigCache();
    }
    
    public ConfigManagerImpl(ConfigLoader loader, ConfigSaver saver, 
                           ConfigValidator validator, ConfigFactory factory,
                           ConfigCache cache) {
        this.loader = loader;
        this.saver = saver;
        this.validator = validator;
        this.factory = factory;
        this.cache = cache;
    }
    
    /**
     * 初始化配置管理器
     */
    public void initialize() {
        if (initialized) {
            LogUtil.warn(LogUtil.MODULE_CONFIG, "配置管理器已经初始化");
            return;
        }
        
        try {
            LogUtil.logInitialization(LogUtil.MODULE_CONFIG, "配置管理器");
            
            // 加载主配置
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            
            // 验证主配置
            ValidationResult mainValidation = ConfigValidationUtil.validate(itemsConfig);
            ConfigValidationUtil.logValidationResult(LogUtil.MODULE_CONFIG, mainValidation);
            if (!mainValidation.isValid()) {
                throw new ConfigLoadException("Main config validation failed: " + ConfigValidationUtil.formatValidationErrors(mainValidation));
            }
            
            // 初始化缓存
            initializeCache(itemsConfig);
            
            initialized = true;
            LogUtil.logCompletion(LogUtil.MODULE_CONFIG, "配置管理器");
            
        } catch (Exception e) {
            LogUtil.logFailure(LogUtil.MODULE_CONFIG, "配置管理器初始化", e);
            throw new RuntimeException("Failed to initialize config manager", e);
        }
    }
    
    /**
     * 初始化缓存
     */
    private void initializeCache(ItemsConfig itemsConfig) {
        cache.invalidate();
        
        if (itemsConfig.getEnabledItems() != null) {
            // 批量添加启用的物品到缓存
            ((MemoryConfigCache) cache).addAllEnabledItems(new java.util.HashSet<>(itemsConfig.getEnabledItems()));
            
            // 加载所有启用的物品配置
            for (String itemId : itemsConfig.getEnabledItems()) {
                try {
                    ItemConfig itemConfig = loader.loadItemConfig(itemId);
                    
                    // 验证物品配置
                    ValidationResult itemValidation = ConfigValidationUtil.validate(itemConfig);
                    ConfigValidationUtil.logValidationResult(itemId, itemValidation);
                    if (!itemValidation.isValid()) {
                        continue;
                    }
                    
                    cache.put(itemId, itemConfig);
                    
                                 } catch (Exception e) {
                     LogUtil.error(LogUtil.MODULE_CONFIG, "加载物品配置失败 {}: {}", itemId, e.getMessage());
                 }
            }
        }
        
                 ((MemoryConfigCache) cache).setValid(true);
         LogUtil.info(LogUtil.MODULE_CONFIG, "缓存初始化完成: {}", cache.getStats());
    }
    
    /**
     * 检查物品是否启用
     */
    public boolean isItemEnabled(String itemId) {
        ensureInitialized();
        return cache.isItemEnabled(itemId);
    }
    
    /**
     * 获取物品配置
     */
    public ItemConfig getItemConfig(String itemId) {
        ensureInitialized();
        return cache.getItemConfig(itemId);
    }
    
    /**
     * 获取所有启用的物品
     */
    public Set<String> getEnabledItems() {
        ensureInitialized();
        return cache.getEnabledItems();
    }
    
    /**
     * 获取主配置
     */
    public ItemsConfig getItemsConfig() {
        ensureInitialized();
        try {
            return loader.loadItemsConfig();
                 } catch (ConfigLoadException e) {
             LogUtil.error(LogUtil.MODULE_CONFIG, "获取主配置失败: {}", e.getMessage(), e);
             return factory.createDefaultItemsConfig();
         }
    }
    
    /**
     * 更新主配置
     */
    public void updateItemsConfig(ItemsConfig itemsConfig) {
        ensureInitialized();
        
        try {
                         // 验证主配置
             ValidationResult validation = ConfigValidationUtil.validate(itemsConfig);
             ConfigValidationUtil.logValidationResult(LogUtil.MODULE_CONFIG, validation);
             if (!validation.isValid()) {
                 throw new IllegalArgumentException("Invalid main config: " + ConfigValidationUtil.formatValidationErrors(validation));
             }
             
             // 保存主配置
             saver.saveItemsConfig(itemsConfig);
             
             // 重新初始化缓存
             initializeCache(itemsConfig);
             
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "主配置更新成功");
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "更新主配置", e);
             throw new RuntimeException("Failed to update main config", e);
         }
    }
    
    /**
     * 更新物品配置
     */
    public void updateItemConfig(String itemId, ItemConfig config) {
        ensureInitialized();
        
        try {
                         // 验证配置
             ValidationResult validation = ConfigValidationUtil.validate(config);
             ConfigValidationUtil.logValidationResult(itemId, validation);
             if (!validation.isValid()) {
                 throw new IllegalArgumentException("Invalid config: " + ConfigValidationUtil.formatValidationErrors(validation));
             }
             
             // 保存配置
             saver.saveItemConfig(itemId, config);
             
             // 更新缓存
             cache.put(itemId, config);
             cache.putEnabled(itemId, config.isEnabled());
             
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "物品配置更新成功");
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "更新物品配置: " + itemId, e);
             throw new RuntimeException("Failed to update item config", e);
         }
    }
    
    /**
     * 添加物品配置
     */
    public boolean addItemConfig(String itemId, ItemConfig config) {
        ensureInitialized();
        
        try {
                         // 验证配置
             ValidationResult validation = ConfigValidationUtil.validate(config);
             ConfigValidationUtil.logValidationResult(itemId, validation);
             if (!validation.isValid()) {
                 return false;
             }
             
             // 保存配置
             saver.saveItemConfig(itemId, config);
             
             // 更新主配置
             ItemsConfig itemsConfig = loader.loadItemsConfig();
             if (!itemsConfig.getEnabledItems().contains(itemId)) {
                 itemsConfig.getEnabledItems().add(itemId);
                 saver.saveItemsConfig(itemsConfig);
             }
             
             // 更新缓存
             cache.put(itemId, config);
             cache.putEnabled(itemId, config.isEnabled());
             
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "物品配置添加成功");
             return true;
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "添加物品配置: " + itemId, e);
             return false;
         }
    }
    
    /**
     * 导入物品配置（不更新主配置）
     */
    public boolean importItemConfig(String itemId, ItemConfig config) {
        ensureInitialized();
        
        try {
                         // 验证配置
             ValidationResult validation = ConfigValidationUtil.validate(config);
             ConfigValidationUtil.logValidationResult(itemId, validation);
             if (!validation.isValid()) {
                 return false;
             }
             
             // 保存配置（不更新主配置）
             saver.saveItemConfig(itemId, config);
             
             // 更新缓存
             cache.put(itemId, config);
             cache.putEnabled(itemId, config.isEnabled());
             
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "物品配置导入成功");
             return true;
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "导入物品配置: " + itemId, e);
             return false;
         }
    }
    
    /**
     * 删除物品配置
     */
    public void removeItemConfig(String itemId) {
        ensureInitialized();
        
        try {
                         // 删除配置文件
             saver.deleteItemConfig(itemId);
             
             // 更新主配置
             ItemsConfig itemsConfig = loader.loadItemsConfig();
             itemsConfig.getEnabledItems().remove(itemId);
             saver.saveItemsConfig(itemsConfig);
             
             // 更新缓存
             cache.remove(itemId);
             
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "物品配置删除成功");
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "删除物品配置: " + itemId, e);
             throw new RuntimeException("Failed to remove item config", e);
         }
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
                 try {
             LogUtil.logInitialization(LogUtil.MODULE_CONFIG, "配置重新加载");
             
             // 重新加载主配置
             ItemsConfig itemsConfig = loader.loadItemsConfig();
             
             // 重新初始化缓存
             initializeCache(itemsConfig);
             
             LogUtil.logCompletion(LogUtil.MODULE_CONFIG, "配置重新加载");
             
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "重新加载配置", e);
             throw new RuntimeException("Failed to reload config", e);
         }
    }
    
    /**
     * 获取缓存统计
     */
    public com.aeolyn.better_experience.common.config.cache.CacheStats getCacheStats() {
        return cache.getStats();
    }
    
    /**
     * 检查调试模式是否启用
     */
    public boolean isDebugEnabled() {
        ensureInitialized();
        try {
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            return itemsConfig.getSettings() != null && itemsConfig.getSettings().isEnableDebugLogs();
                 } catch (Exception e) {
             LogUtil.error(LogUtil.MODULE_CONFIG, "检查调试模式失败: {}", e.getMessage(), e);
             return false;
         }
    }
    
    /**
     * 检查渲染日志是否启用
     */
    public boolean isRenderLogsEnabled() {
        ensureInitialized();
        try {
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            return itemsConfig.getLogConfig() != null && itemsConfig.getLogConfig().isEnableRenderLogs();
                 } catch (Exception e) {
             LogUtil.error(LogUtil.MODULE_CONFIG, "检查渲染日志失败: {}", e.getMessage(), e);
             return false;
         }
    }
    
    /**
     * 检查配置日志是否启用
     */
    public boolean isConfigLogsEnabled() {
        ensureInitialized();
        try {
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            return itemsConfig.getLogConfig() != null && itemsConfig.getLogConfig().isEnableConfigLogs();
                 } catch (Exception e) {
             LogUtil.error(LogUtil.MODULE_CONFIG, "检查配置日志失败: {}", e.getMessage(), e);
             return false;
         }
    }
    
    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("ConfigManager is not initialized. Call initialize() first.");
        }
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 获取副手限制配置
     */
    public OffHandRestrictionConfig getOffHandRestrictionConfig() {
        ensureInitialized();
        try {
            return loader.loadOffHandRestrictionConfig();
                 } catch (Exception e) {
             LogUtil.error(LogUtil.MODULE_CONFIG, "加载副手限制配置失败: {}", e.getMessage(), e);
             return new OffHandRestrictionConfig(); // 返回默认配置
         }
    }
    
    /**
     * 保存副手限制配置
     */
    public void saveOffHandRestrictionConfig() {
        ensureInitialized();
        try {
            OffHandRestrictionConfig config = getOffHandRestrictionConfig();
                         saver.saveOffHandRestrictionConfig(config);
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "副手限制配置保存");
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "保存副手限制配置", e);
             throw new RuntimeException("Failed to save offhand restriction config", e);
         }
    }
    
    /**
     * 更新副手限制配置
     */
    public void updateOffHandRestrictionConfig(OffHandRestrictionConfig config) {
        ensureInitialized();
        try {
                         saver.saveOffHandRestrictionConfig(config);
             LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "副手限制配置更新");
         } catch (Exception e) {
             LogUtil.logFailure(LogUtil.MODULE_CONFIG, "更新副手限制配置", e);
             throw new RuntimeException("Failed to update offhand restriction config", e);
         }
    }
}
