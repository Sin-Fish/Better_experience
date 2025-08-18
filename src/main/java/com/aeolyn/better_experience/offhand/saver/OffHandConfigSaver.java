package com.aeolyn.better_experience.offhand.saver;

import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
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
 * 副手配置保存器实现
 */
public class OffHandConfigSaver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-OffHand-Saver");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final String configDir;
    private final String offHandConfigFile;
    
    public OffHandConfigSaver(String configDir) {
        this.configDir = configDir;
        this.offHandConfigFile = "offhand_restrictions.json";
    }
    
    public OffHandConfigSaver() {
        this("config/better_experience");
    }
    
    public void saveOffHandRestrictionConfig(OffHandRestrictionConfig config) throws ConfigSaveException {
        if (config == null) {
            throw new ConfigSaveException("OffHandRestrictionConfig cannot be null");
        }
        
        Path configPath = getConfigPath(offHandConfigFile);
        
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
    
    public boolean isWritable() {
        Path configPath = getConfigPath(offHandConfigFile);
        Path parentDir = configPath.getParent();
        
        try {
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            return Files.isWritable(parentDir);
        } catch (Exception e) {
            LOGGER.error("检查副手配置写入权限失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath(String... parts) {
        return Paths.get(configDir, parts);
    }
}
