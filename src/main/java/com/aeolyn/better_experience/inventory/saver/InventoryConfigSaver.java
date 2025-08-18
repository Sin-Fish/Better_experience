package com.aeolyn.better_experience.inventory.saver;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 背包配置保存器
 */
public class InventoryConfigSaver {
    
    private static final String CONFIG_FILE_NAME = "inventory_sort.json";
    private final Gson gson;
    
    public InventoryConfigSaver() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }
    
    /**
     * 保存背包排序配置
     */
    public void saveInventorySortConfig(InventorySortConfig config) {
        try {
            Path configPath = getConfigPath();
            
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            String jsonContent = gson.toJson(config);
            Files.writeString(configPath, jsonContent);
            
            LogUtil.info("Inventory", "成功保存背包排序配置");
            
        } catch (Exception e) {
            LogUtil.error("Inventory", "保存背包排序配置失败", e);
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("better_experience").resolve(CONFIG_FILE_NAME);
    }
}
