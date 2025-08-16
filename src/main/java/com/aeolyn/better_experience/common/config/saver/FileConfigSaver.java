package com.aeolyn.better_experience.common.config.saver;

import com.aeolyn.better_experience.common.config.ItemsConfig;
import com.aeolyn.better_experience.common.config.ItemConfig;
import com.aeolyn.better_experience.common.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigSaveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件配置保存器实�?
 */
public class FileConfigSaver implements ConfigSaver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Saver");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final String configDir;
    private final String itemsConfigFile;
    private final String itemConfigsDir;
    
    public FileConfigSaver(String configDir) {
        this.configDir = configDir;
        this.itemsConfigFile = "items.json";
        this.itemConfigsDir = "item_configs";
    }
    
    public FileConfigSaver() {
        this("config/better_experience");
    }
    
    @Override
    public void saveItemsConfig(ItemsConfig config) throws ConfigSaveException {
        if (config == null) {
            throw new ConfigSaveException("ItemsConfig cannot be null");
        }
        
        Path configPath = getConfigPath(itemsConfigFile);
        
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 保存配置文件
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
            
            LOGGER.info("主配置文件保存成�? {}", configPath);
        } catch (Exception e) {
            LOGGER.error("保存主配置文件失�? " + e.getMessage(), e);
            throw new ConfigSaveException("Failed to save items config to " + configPath, e);
        }
    }
    
    @Override
    public void saveItemConfig(String itemId, ItemConfig config) throws ConfigSaveException {
        if (itemId == null || itemId.isEmpty()) {
            throw new ConfigSaveException("Item ID cannot be null or empty");
        }
        
        if (config == null) {
            throw new ConfigSaveException("ItemConfig cannot be null");
        }
        
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(itemConfigsDir, fileName);
        
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 保存配置文件
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
            
            LOGGER.info("物品配置文件保存成功: {}", configPath);
        } catch (Exception e) {
            LOGGER.error("保存物品配置失败 " + itemId + ": " + e.getMessage(), e);
            throw new ConfigSaveException("Failed to save item config for " + itemId, e);
        }
    }
    
    @Override
    public void deleteItemConfig(String itemId) throws ConfigSaveException {
        if (itemId == null || itemId.isEmpty()) {
            throw new ConfigSaveException("Item ID cannot be null or empty");
        }
        
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(itemConfigsDir, fileName);
        
        try {
            if (Files.exists(configPath)) {
                Files.delete(configPath);
                LOGGER.info("物品配置文件删除成功: {}", configPath);
            } else {
                LOGGER.warn("物品配置文件不存在，无法删除: {}", configPath);
            }
        } catch (Exception e) {
            LOGGER.error("删除物品配置失败 " + itemId + ": " + e.getMessage(), e);
            throw new ConfigSaveException("Failed to delete item config for " + itemId, e);
        }
    }
    
    @Override
    public boolean isWritable() {
        Path configPath = getConfigPath(itemsConfigFile);
        Path parentDir = configPath.getParent();
        
        try {
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            return Files.isWritable(parentDir);
        } catch (Exception e) {
            LOGGER.error("检查写入权限失�? " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath(String... parts) {
        return Paths.get(configDir, parts);
    }
    
    @Override
    public void saveOffHandRestrictionConfig(OffHandRestrictionConfig config) throws ConfigSaveException {
        if (config == null) {
            throw new ConfigSaveException("OffHandRestrictionConfig cannot be null");
        }
        
        Path configPath = getConfigPath("offhand_restrictions.json");
        
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 保存配置文件
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
            
            LOGGER.info("副手限制配置文件保存成功: {}", configPath);
        } catch (Exception e) {
            LOGGER.error("保存副手限制配置文件失败: " + e.getMessage(), e);
            throw new ConfigSaveException("Failed to save offhand restriction config to " + configPath, e);
        }
    }
}
