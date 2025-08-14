package com.aeolyn.better_experience.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private ConfigManager() {
        // 私有构造函数，防止外部实例化
    }
    
    private static ItemsConfig itemsConfig;
    private static Map<String, ItemConfig> itemConfigs = new HashMap<>();
    private static boolean initialized = false;
    private static ConfigManager instance;
    
    public static void initialize() {
        if (initialized) return;
        
        try {
            loadItemsConfig();
            loadItemConfigs();
            initialized = true;
            instance = new ConfigManager();
            LOGGER.info("配置管理器初始化完成");
        } catch (Exception e) {
            LOGGER.error("配置管理器初始化失败: " + e.getMessage(), e);
        }
    }
    
    public static ConfigManager getInstance() {
        if (!initialized) {
            initialize();
        }
        return instance;
    }
    
    private static void loadItemsConfig() {
        try {
            InputStream stream = ConfigManager.class.getClassLoader()
                .getResourceAsStream("assets/better_experience/config/items.json");
            
            if (stream != null) {
                itemsConfig = GSON.fromJson(new InputStreamReader(stream), ItemsConfig.class);
                LOGGER.info("主配置文件加载成功");
            } else {
                LOGGER.error("无法找到主配置文件");
                itemsConfig = new ItemsConfig();
            }
        } catch (Exception e) {
            LOGGER.error("加载主配置文件失败: " + e.getMessage(), e);
            itemsConfig = new ItemsConfig();
        }
    }
    
    private static void loadItemConfigs() {
        for (String itemId : itemsConfig.getEnabledItems()) {
            try {
                String configPath = "assets/better_experience/config/item_configs/" + itemId.replace(":", "_") + ".json";
                InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(configPath);
                
                if (stream != null) {
                    ItemConfig config = GSON.fromJson(new InputStreamReader(stream), ItemConfig.class);
                    itemConfigs.put(itemId, config);
                    LOGGER.info("物品配置加载成功: " + itemId);
                } else {
                    LOGGER.warn("无法找到物品配置文件: " + configPath);
                }
            } catch (Exception e) {
                LOGGER.error("加载物品配置失败 " + itemId + ": " + e.getMessage(), e);
            }
        }
    }
    
    public static boolean isItemEnabled(String itemId) {
        return itemsConfig != null && itemsConfig.getEnabledItems().contains(itemId);
    }
    
    public static ItemConfig getItemConfig(String itemId) {
        return itemConfigs.get(itemId);
    }
    
    public static boolean isDebugEnabled() {
        return itemsConfig != null && itemsConfig.getSettings().isEnableDebugLogs();
    }
    
    public static ItemsConfig.Settings getDefaultSettings() {
        return itemsConfig != null ? itemsConfig.getSettings() : null;
    }
    
    public static void reload() {
        initialized = false;
        itemConfigs.clear();
        initialize();
    }
    
    public static java.util.Set<String> getAllConfiguredItems() {
        return itemConfigs.keySet();
    }
    
    public static void saveConfig(String itemId) {
        try {
            ItemConfig config = itemConfigs.get(itemId);
            if (config != null) {
                // 确保配置已更新到内存中
                itemConfigs.put(itemId, config);
                LOGGER.info("配置已保存到内存: " + itemId + " (缩放: " + config.getFirstPerson().getScale() + ")");
            }
        } catch (Exception e) {
            LOGGER.error("保存配置失败 " + itemId + ": " + e.getMessage(), e);
        }
    }
    
    public static void saveAllConfigs() {
        try {
            // 保存所有配置到运行时内存
            for (String itemId : itemConfigs.keySet()) {
                saveConfig(itemId);
            }
            LOGGER.info("所有配置已保存到内存");
        } catch (Exception e) {
            LOGGER.error("保存所有配置失败: " + e.getMessage(), e);
        }
    }
}
