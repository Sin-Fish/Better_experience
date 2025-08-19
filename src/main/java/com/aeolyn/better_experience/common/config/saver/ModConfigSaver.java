package com.aeolyn.better_experience.common.config.saver;

import com.aeolyn.better_experience.common.config.ModConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigSaveException;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 模组配置保存器
 * 负责保存模组的主配置文件
 */
public class ModConfigSaver {
    
    private static final String CONFIG_FILE_NAME = "mod_config.json";
    private static final String CONFIG_DIR = "config/better_experience";
    
    private final Gson gson;
    
    public ModConfigSaver() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    }
    
    /**
     * 保存模组配置
     * @param config 模组配置对象
     * @throws ConfigSaveException 如果保存失败
     */
    public void saveModConfig(ModConfig config) throws ConfigSaveException {
        try {
            LogUtil.logInitialization(LogUtil.MODULE_CONFIG, "保存模组配置");
            
            Path configPath = getConfigPath();
            Path configDir = configPath.getParent();
            
            // 确保配置目录存在
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LogUtil.info(LogUtil.MODULE_CONFIG, "创建配置目录: {}", configDir);
            }
            
            // 转换为JSON字符串
            String jsonContent = gson.toJson(config);
            
            // 写入文件
            Files.writeString(configPath, jsonContent);
            
            LogUtil.logCompletion(LogUtil.MODULE_CONFIG, "保存模组配置");
            LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "模组配置保存成功");
            
        } catch (IOException e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "保存配置文件失败: {}", e.getMessage(), e);
            throw new ConfigSaveException("保存配置文件失败", e);
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "保存模组配置失败: {}", e.getMessage(), e);
            throw new ConfigSaveException("保存模组配置失败", e);
        }
    }
    
    /**
     * 备份配置文件
     * @param config 模组配置对象
     * @param backupName 备份文件名
     * @throws ConfigSaveException 如果备份失败
     */
    public void backupModConfig(ModConfig config, String backupName) throws ConfigSaveException {
        try {
            Path backupPath = getBackupPath(backupName);
            Path backupDir = backupPath.getParent();
            
            // 确保备份目录存在
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            
            // 转换为JSON字符串
            String jsonContent = gson.toJson(config);
            
            // 写入备份文件
            Files.writeString(backupPath, jsonContent);
            
            LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "模组配置备份成功");
            
        } catch (IOException e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "备份配置文件失败: {}", e.getMessage(), e);
            throw new ConfigSaveException("备份配置文件失败", e);
        }
    }
    
    /**
     * 删除配置文件
     * @throws ConfigSaveException 如果删除失败
     */
    public void deleteModConfig() throws ConfigSaveException {
        try {
            Path configPath = getConfigPath();
            
            if (Files.exists(configPath)) {
                Files.delete(configPath);
                LogUtil.logSuccess(LogUtil.MODULE_CONFIG, "模组配置文件删除成功");
            } else {
                LogUtil.warn(LogUtil.MODULE_CONFIG, "模组配置文件不存在: {}", configPath);
            }
            
        } catch (IOException e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "删除配置文件失败: {}", e.getMessage(), e);
            throw new ConfigSaveException("删除配置文件失败", e);
        }
    }
    
    /**
     * 检查配置文件是否存在
     * @return true如果配置文件存在
     */
    public boolean configExists() {
        return Files.exists(getConfigPath());
    }
    
    /**
     * 获取配置文件路径
     * @return 配置文件路径
     */
    private Path getConfigPath() {
        return Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
    }
    
    /**
     * 获取备份文件路径
     * @param backupName 备份文件名
     * @return 备份文件路径
     */
    private Path getBackupPath(String backupName) {
        return Paths.get(CONFIG_DIR, "backups", backupName + ".json");
    }
    
    /**
     * 获取配置目录路径
     * @return 配置目录路径
     */
    public Path getConfigDirectory() {
        return Paths.get(CONFIG_DIR);
    }
}
