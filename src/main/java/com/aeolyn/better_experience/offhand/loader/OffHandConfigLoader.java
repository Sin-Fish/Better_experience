package com.aeolyn.better_experience.offhand.loader;

import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 副手配置加载器实现
 */
public class OffHandConfigLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-OffHand-Loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final String configDir;
    private final String offHandConfigFile;
    
    public OffHandConfigLoader(String configDir) {
        this.configDir = configDir;
        this.offHandConfigFile = "offhand_restrictions.json";
    }
    
    public OffHandConfigLoader() {
        this("config/better_experience");
    }
    
    public OffHandRestrictionConfig loadOffHandRestrictionConfig() throws ConfigLoadException {
        Path configPath = getConfigPath(offHandConfigFile);
        
        try {
            // 如果配置文件不存在，创建默认配置
            if (!Files.exists(configPath)) {
                createDefaultOffHandRestrictionConfig(configPath);
            }
            
            // 读取配置文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                OffHandRestrictionConfig config = GSON.fromJson(reader, OffHandRestrictionConfig.class);
                if (config == null) {
                    config = new OffHandRestrictionConfig();
                }
                
                // 执行配置迁移（从旧格式到新格式）
                config.migrateFromLegacyFormat();
                
                LOGGER.info("副手限制配置文件加载成功: {}", configPath);
                return config;
            }
        } catch (Exception e) {
            LOGGER.error("加载副手限制配置文件失败: " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to load offhand restriction config from " + configPath, e);
        }
    }
    
    /**
     * 创建默认副手限制配置文件
     */
    private void createDefaultOffHandRestrictionConfig(Path configPath) throws ConfigLoadException {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 从资源文件读取默认配置
            try (InputStream resourceStream = getClass().getClassLoader()
                    .getResourceAsStream("assets/better_experience/offhand/offhand_restrictions.json")) {
                
                if (resourceStream != null) {
                    // 复制默认配置到配置文件
                    Files.copy(resourceStream, configPath);
                    LOGGER.info("创建默认副手限制配置文件: {}", configPath);
                } else {
                    // 创建空的默认配置
                    OffHandRestrictionConfig defaultConfig = new OffHandRestrictionConfig();
                    try (Writer writer = Files.newBufferedWriter(configPath)) {
                        GSON.toJson(defaultConfig, writer);
                    }
                    LOGGER.info("创建默认副手限制配置文件: {}", configPath);
                }
            }
        } catch (Exception e) {
            LOGGER.error("创建默认副手限制配置文件失败: " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to create default offhand restriction config", e);
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath(String... parts) {
        return Paths.get(configDir, parts);
    }
}
