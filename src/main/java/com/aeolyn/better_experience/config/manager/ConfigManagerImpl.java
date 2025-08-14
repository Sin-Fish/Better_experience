package com.aeolyn.better_experience.config.manager;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;
import com.aeolyn.better_experience.config.cache.ConfigCache;
import com.aeolyn.better_experience.config.cache.MemoryConfigCache;
import com.aeolyn.better_experience.config.exception.ConfigLoadException;
import com.aeolyn.better_experience.config.exception.ConfigSaveException;
import com.aeolyn.better_experience.config.factory.ConfigFactory;
import com.aeolyn.better_experience.config.factory.DefaultConfigFactory;
import com.aeolyn.better_experience.config.loader.ConfigLoader;
import com.aeolyn.better_experience.config.loader.FileConfigLoader;
import com.aeolyn.better_experience.config.saver.ConfigSaver;
import com.aeolyn.better_experience.config.saver.FileConfigSaver;
import com.aeolyn.better_experience.config.validator.ConfigValidator;
import com.aeolyn.better_experience.config.validator.ItemConfigValidator;
import com.aeolyn.better_experience.config.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 配置管理器实现
 * 作为各个组件的协调者，提供统一的配置管理接口
 */
public class ConfigManagerImpl {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Manager");
    
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
            LOGGER.warn("配置管理器已经初始化");
            return;
        }
        
        try {
            LOGGER.info("开始初始化配置管理器...");
            
            // 加载主配置
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            
            // 验证主配置
            ValidationResult mainValidation = validator.validate(itemsConfig);
            if (!mainValidation.isValid()) {
                LOGGER.error("主配置验证失败: {}", mainValidation.getErrors());
                throw new ConfigLoadException("Main config validation failed: " + mainValidation.getErrors());
            }
            
            if (mainValidation.hasWarnings()) {
                LOGGER.warn("主配置验证警告: {}", mainValidation.getWarnings());
            }
            
            // 初始化缓存
            initializeCache(itemsConfig);
            
            initialized = true;
            LOGGER.info("配置管理器初始化完成");
            
        } catch (Exception e) {
            LOGGER.error("配置管理器初始化失败: " + e.getMessage(), e);
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
                    ValidationResult itemValidation = validator.validate(itemConfig);
                    if (!itemValidation.isValid()) {
                        LOGGER.error("物品配置验证失败 {}: {}", itemId, itemValidation.getErrors());
                        continue;
                    }
                    
                    if (itemValidation.hasWarnings()) {
                        LOGGER.warn("物品配置验证警告 {}: {}", itemId, itemValidation.getWarnings());
                    }
                    
                    cache.put(itemId, itemConfig);
                    
                } catch (Exception e) {
                    LOGGER.error("加载物品配置失败 {}: {}", itemId, e.getMessage());
                }
            }
        }
        
        ((MemoryConfigCache) cache).setValid(true);
        LOGGER.info("缓存初始化完成: {}", cache.getStats());
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
            LOGGER.error("获取主配置失败: " + e.getMessage(), e);
            return factory.createDefaultItemsConfig();
        }
    }
    
    /**
     * 更新物品配置
     */
    public void updateItemConfig(String itemId, ItemConfig config) {
        ensureInitialized();
        
        try {
            // 验证配置
            ValidationResult validation = validator.validate(config);
            if (!validation.isValid()) {
                LOGGER.error("配置验证失败: {}", validation.getErrors());
                throw new IllegalArgumentException("Invalid config: " + validation.getErrors());
            }
            
            // 保存配置
            saver.saveItemConfig(itemId, config);
            
            // 更新缓存
            cache.put(itemId, config);
            cache.putEnabled(itemId, config.isEnabled());
            
            LOGGER.info("物品配置更新成功: {}", itemId);
            
        } catch (Exception e) {
            LOGGER.error("更新物品配置失败 {}: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("Failed to update item config", e);
        }
    }
    
    /**
     * 添加物品配置
     */
    public void addItemConfig(String itemId, ItemConfig config) {
        ensureInitialized();
        
        try {
            // 验证配置
            ValidationResult validation = validator.validate(config);
            if (!validation.isValid()) {
                LOGGER.error("配置验证失败: {}", validation.getErrors());
                throw new IllegalArgumentException("Invalid config: " + validation.getErrors());
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
            
            LOGGER.info("物品配置添加成功: {}", itemId);
            
        } catch (Exception e) {
            LOGGER.error("添加物品配置失败 {}: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("Failed to add item config", e);
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
            
            LOGGER.info("物品配置删除成功: {}", itemId);
            
        } catch (Exception e) {
            LOGGER.error("删除物品配置失败 {}: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove item config", e);
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        try {
            LOGGER.info("开始重新加载配置...");
            
            // 重新加载主配置
            ItemsConfig itemsConfig = loader.loadItemsConfig();
            
            // 重新初始化缓存
            initializeCache(itemsConfig);
            
            LOGGER.info("配置重新加载完成");
            
        } catch (Exception e) {
            LOGGER.error("重新加载配置失败: " + e.getMessage(), e);
            throw new RuntimeException("Failed to reload config", e);
        }
    }
    
    /**
     * 获取缓存统计
     */
    public com.aeolyn.better_experience.config.cache.CacheStats getCacheStats() {
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
            LOGGER.error("检查调试模式失败: " + e.getMessage(), e);
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
            LOGGER.error("检查渲染日志失败: " + e.getMessage(), e);
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
            LOGGER.error("检查配置日志失败: " + e.getMessage(), e);
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
}
