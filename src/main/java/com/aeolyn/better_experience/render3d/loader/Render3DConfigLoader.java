package com.aeolyn.better_experience.render3d.loader;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;
import com.aeolyn.better_experience.common.config.factory.ConfigFactory;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * 3D渲染配置加载器实现
 */
public class Render3DConfigLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Render3D-Loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final String configDir;
    private final String itemsConfigFile;
    private final String itemConfigsDir;
    private final ConfigFactory configFactory;
    
    public Render3DConfigLoader(String configDir, ConfigFactory configFactory) {
        this.configDir = configDir;
        this.itemsConfigFile = "items.json";
        this.itemConfigsDir = "item_configs";
        this.configFactory = configFactory;
    }
    
    public Render3DConfigLoader(ConfigFactory configFactory) {
        this("config/better_experience", configFactory);
    }
    
    public ItemsConfig loadItemsConfig() throws ConfigLoadException {
        Path configPath = getConfigPath(itemsConfigFile);
        
        try {
            // 如果配置文件不存在，创建默认配置
            if (!Files.exists(configPath)) {
                createDefaultItemsConfig(configPath);
            }
            
            // 读取配置文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ItemsConfig config = GSON.fromJson(reader, ItemsConfig.class);
                if (config == null) {
                    config = configFactory.createDefaultItemsConfig();
                }
                LogUtil.info("Render3D", "3D渲染主配置文件加载成功: {}", configPath);
                return config;
            }
        } catch (Exception e) {
            LogUtil.error("Render3D", "加载3D渲染主配置文件失败: " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to load items config from " + configPath, e);
        }
    }
    
    public ItemConfig loadItemConfig(String itemId) throws ConfigLoadException {
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(itemConfigsDir, fileName);
        
        try {
            // 如果配置文件不存在，创建默认配置
            if (!Files.exists(configPath)) {
                createDefaultItemConfig(itemId, configPath);
            }
            
            // 读取配置文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ItemConfig config = GSON.fromJson(reader, ItemConfig.class);
                if (config != null) {
                    LogUtil.debug("Render3D", "3D渲染物品配置加载成功: {}", itemId);
                    return config;
                } else {
                    LogUtil.warn("Render3D", "3D渲染物品配置文件为空: {}", itemId);
                    return configFactory.createDefaultItemConfig(itemId);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Render3D", "加载3D渲染物品配置失败 " + itemId + ": " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to load item config for " + itemId, e);
        }
    }
    
    public boolean configExists(String itemId) {
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(itemConfigsDir, fileName);
        return Files.exists(configPath);
    }
    
    public Set<String> getAvailableItemConfigs() {
        Set<String> availableConfigs = new HashSet<>();
        Path itemConfigsPath = getConfigPath(itemConfigsDir);
        
        if (Files.exists(itemConfigsPath) && Files.isDirectory(itemConfigsPath)) {
            try {
                Files.list(itemConfigsPath)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String itemId = fileName.replace(".json", "").replace("_", ":");
                        availableConfigs.add(itemId);
                    });
            } catch (IOException e) {
                LogUtil.error("Render3D", "获取可用3D渲染配置列表失败: " + e.getMessage(), e);
            }
        }
        
        return availableConfigs;
    }
    
    /**
     * 创建默认主配置文件
     */
    private void createDefaultItemsConfig(Path configPath) throws ConfigLoadException {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 从资源文件读取默认配置
            try (InputStream resourceStream = getClass().getClassLoader()
                    .getResourceAsStream("assets/better_experience/render3d/items.json")) {
                
                if (resourceStream != null) {
                    // 复制默认配置到配置文件
                    Files.copy(resourceStream, configPath);
                    LogUtil.info("Render3D", "创建默认3D渲染主配置文件: {}", configPath);
                } else {
                    // 创建空的默认配置
                    ItemsConfig defaultConfig = configFactory.createDefaultItemsConfig();
                    try (Writer writer = Files.newBufferedWriter(configPath)) {
                        GSON.toJson(defaultConfig, writer);
                    }
                    LogUtil.info("Render3D", "创建默认3D渲染主配置文件: {}", configPath);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Render3D", "创建默认3D渲染主配置文件失败: " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to create default items config", e);
        }
    }
    
    /**
     * 创建默认物品配置文件
     */
    private void createDefaultItemConfig(String itemId, Path configPath) throws ConfigLoadException {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 从资源文件读取默认配置
            String resourcePath = "assets/better_experience/render3d/item_configs/" + itemId.replace(":", "_") + ".json";
            try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                
                if (resourceStream != null) {
                    // 复制默认配置到配置文件
                    Files.copy(resourceStream, configPath);
                    LogUtil.info("Render3D", "创建默认3D渲染物品配置文件: {}", configPath);
                } else {
                    // 创建空的默认配置
                    ItemConfig defaultConfig = configFactory.createDefaultItemConfig(itemId);
                    try (Writer writer = Files.newBufferedWriter(configPath)) {
                        GSON.toJson(defaultConfig, writer);
                    }
                    LogUtil.info("Render3D", "创建默认3D渲染物品配置文件: {}", configPath);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Render3D", "创建默认3D渲染物品配置文件失败 " + itemId + ": " + e.getMessage(), e);
            throw new ConfigLoadException("Failed to create default item config for " + itemId, e);
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath(String... parts) {
        return Paths.get(configDir, parts);
    }
}
