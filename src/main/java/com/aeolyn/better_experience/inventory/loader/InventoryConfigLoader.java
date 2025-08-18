package com.aeolyn.better_experience.inventory.loader;

import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 背包整理配置加载器
 */
public class InventoryConfigLoader {
    
    private static final String CONFIG_PATH = "assets/better_experience/inventory/inventory_sort.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    /**
     * 加载背包整理配置
     */
    public static InventorySortConfig loadConfig(ResourceManager resourceManager) throws ConfigLoadException {
        try {
            Identifier configId = Identifier.of("better_experience", CONFIG_PATH);
            Resource resource = resourceManager.getResource(configId).orElse(null);
            
            if (resource == null) {
                LogUtil.warn("Inventory", "未找到配置文件，使用默认配置");
                return new InventorySortConfig();
            }
            
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                InventorySortConfig config = GSON.fromJson(reader, InventorySortConfig.class);
                LogUtil.info("Inventory", "背包整理配置加载成功");
                return config;
            }
            
        } catch (IOException e) {
            LogUtil.error("Inventory", "加载背包整理配置失败", e);
            throw new ConfigLoadException("无法加载背包整理配置", e);
        } catch (Exception e) {
            LogUtil.error("Inventory", "解析背包整理配置失败", e);
            throw new ConfigLoadException("背包整理配置格式错误", e);
        }
    }
    
    /**
     * 保存背包整理配置
     */
    public static String saveConfig(InventorySortConfig config) {
        try {
            String json = GSON.toJson(config);
            LogUtil.info("Inventory", "背包整理配置序列化成功");
            return json;
        } catch (Exception e) {
            LogUtil.error("Inventory", "序列化背包整理配置失败", e);
            throw new RuntimeException("无法序列化背包整理配置", e);
        }
    }
}
