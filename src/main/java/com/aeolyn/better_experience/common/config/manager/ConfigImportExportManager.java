package com.aeolyn.better_experience.common.config.manager;

import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 配置导入导出管理器
 * 负责处理配置的导出和导入功能
 */
public class ConfigImportExportManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-ImportExport");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * 导出配置到指定目录
     * @param configManager 配置管理器
     * @param exportPath 导出路径
     * @return 是否成功
     */
    public static boolean exportConfigs(ConfigManager configManager, String exportPath) {
        try {
            Path exportDir = Paths.get(exportPath);
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
            
            // 导出主配置文件
            ItemsConfig itemsConfig = configManager.getItemsConfig();
            if (itemsConfig != null) {
                String mainConfigJson = GSON.toJson(itemsConfig);
                Path mainConfigPath = exportDir.resolve("items.json");
                Files.write(mainConfigPath, mainConfigJson.getBytes(StandardCharsets.UTF_8));
                LOGGER.info("主配置文件导出成功: {}", mainConfigPath);
            }
            
            // 创建item_configs目录
            Path itemConfigsDir = exportDir.resolve("item_configs");
            if (!Files.exists(itemConfigsDir)) {
                Files.createDirectories(itemConfigsDir);
            }
            
            // 导出所有物品配置
            Set<String> configuredItems = configManager.getAllConfiguredItems();
            for (String itemId : configuredItems) {
                ItemConfig itemConfig = configManager.getItemConfig(itemId);
                if (itemConfig != null) {
                    String fileName = itemId.replace(":", "_") + ".json";
                    Path itemConfigPath = itemConfigsDir.resolve(fileName);
                    String itemConfigJson = GSON.toJson(itemConfig);
                    Files.write(itemConfigPath, itemConfigJson.getBytes(StandardCharsets.UTF_8));
                    LOGGER.info("物品配置导出成功: {} -> {}", itemId, itemConfigPath);
                }
            }
            
            LOGGER.info("配置导出完成，共导出 {} 个物品配置", configuredItems.size());
            return true;
            
        } catch (Exception e) {
            LOGGER.error("配置导出失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从指定目录导入配置
     * @param configManager 配置管理器
     * @param importPath 导入路径
     * @return 导入结果信息
     */
    public static ImportResult importConfigs(ConfigManager configManager, String importPath) {
        ImportResult result = new ImportResult();
        
        try {
            Path importDir = Paths.get(importPath);
            if (!Files.exists(importDir)) {
                result.setSuccess(false);
                result.setMessage("导入目录不存在: " + importPath);
                return result;
            }
            
            // 导入主配置文件
            Path mainConfigPath = importDir.resolve("items.json");
            if (Files.exists(mainConfigPath)) {
                try {
                    String mainConfigJson = Files.readString(mainConfigPath, StandardCharsets.UTF_8);
                    ItemsConfig importedItemsConfig = GSON.fromJson(mainConfigJson, ItemsConfig.class);
                    if (importedItemsConfig != null) {
                        // 更新主配置
                        configManager.updateItemsConfig(importedItemsConfig);
                        result.addImportedMainConfig();
                        LOGGER.info("主配置文件导入成功: {}", mainConfigPath);
                    }
                } catch (Exception e) {
                    result.addFailedMainConfig();
                    LOGGER.error("主配置文件导入失败: " + e.getMessage(), e);
                }
            }
            
            // 导入物品配置
            Path itemConfigsDir = importDir.resolve("item_configs");
            if (Files.exists(itemConfigsDir) && Files.isDirectory(itemConfigsDir)) {
                Files.list(itemConfigsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(itemConfigPath -> {
                        try {
                            String itemConfigJson = Files.readString(itemConfigPath, StandardCharsets.UTF_8);
                            ItemConfig itemConfig = GSON.fromJson(itemConfigJson, ItemConfig.class);
                            
                            if (itemConfig != null && itemConfig.getItemId() != null) {
                                // 验证物品ID格式
                                try {
                                    Identifier.of(itemConfig.getItemId());
                                    
                                    // 添加到配置管理器
                                    boolean success = configManager.addItemConfig(itemConfig.getItemId(), itemConfig);
                                    if (success) {
                                        result.addImportedItemConfig(itemConfig.getItemId());
                                        LOGGER.info("物品配置导入成功: {} -> {}", itemConfigPath.getFileName(), itemConfig.getItemId());
                                    } else {
                                        result.addFailedItemConfig(itemConfig.getItemId(), "配置添加失败");
                                        LOGGER.warn("物品配置添加失败: {}", itemConfig.getItemId());
                                    }
                                } catch (Exception e) {
                                    result.addFailedItemConfig(itemConfig.getItemId(), "物品ID格式错误");
                                    LOGGER.error("物品ID格式错误: {}", itemConfig.getItemId());
                                }
                            } else {
                                result.addFailedItemConfig(itemConfigPath.getFileName().toString(), "配置格式错误");
                                LOGGER.error("物品配置格式错误: {}", itemConfigPath.getFileName());
                            }
                        } catch (Exception e) {
                            result.addFailedItemConfig(itemConfigPath.getFileName().toString(), e.getMessage());
                            LOGGER.error("物品配置导入失败 {}: {}", itemConfigPath.getFileName(), e.getMessage());
                        }
                    });
            }
            
            LOGGER.info("配置导入完成，成功: {}, 失败: {}", 
                result.getImportedItemConfigs().size(), result.getFailedItemConfigs().size());
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("配置导入失败: " + e.getMessage());
            LOGGER.error("配置导入失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 验证导入的配置
     * @param importPath 导入路径
     * @return 验证结果
     */
    public static ValidationResult validateImportConfigs(String importPath) {
        ValidationResult result = new ValidationResult();
        
        try {
            Path importDir = Paths.get(importPath);
            if (!Files.exists(importDir)) {
                result.setValid(false);
                result.setMessage("导入目录不存在: " + importPath);
                return result;
            }
            
            // 验证主配置文件
            Path mainConfigPath = importDir.resolve("items.json");
            if (Files.exists(mainConfigPath)) {
                try {
                    String mainConfigJson = Files.readString(mainConfigPath, StandardCharsets.UTF_8);
                    ItemsConfig itemsConfig = GSON.fromJson(mainConfigJson, ItemsConfig.class);
                    if (itemsConfig != null) {
                        result.setMainConfigValid(true);
                        result.setMainConfigItemsCount(itemsConfig.getEnabledItems() != null ? itemsConfig.getEnabledItems().size() : 0);
                    } else {
                        result.setMainConfigValid(false);
                        result.addError("主配置文件格式错误");
                    }
                } catch (Exception e) {
                    result.setMainConfigValid(false);
                    result.addError("主配置文件解析失败: " + e.getMessage());
                }
            } else {
                result.addWarning("主配置文件不存在");
            }
            
            // 验证物品配置文件
            Path itemConfigsDir = importDir.resolve("item_configs");
            if (Files.exists(itemConfigsDir) && Files.isDirectory(itemConfigsDir)) {
                Files.list(itemConfigsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(itemConfigPath -> {
                        try {
                            String itemConfigJson = Files.readString(itemConfigPath, StandardCharsets.UTF_8);
                            ItemConfig itemConfig = GSON.fromJson(itemConfigJson, ItemConfig.class);
                            
                            if (itemConfig != null && itemConfig.getItemId() != null) {
                                // 验证物品ID格式
                                try {
                                    Identifier.of(itemConfig.getItemId());
                                    result.addValidItemConfig(itemConfig.getItemId());
                                } catch (Exception e) {
                                    result.addInvalidItemConfig(itemConfig.getItemId(), "物品ID格式错误");
                                }
                            } else {
                                result.addInvalidItemConfig(itemConfigPath.getFileName().toString(), "配置格式错误");
                            }
                        } catch (Exception e) {
                            result.addInvalidItemConfig(itemConfigPath.getFileName().toString(), "解析失败: " + e.getMessage());
                        }
                    });
            } else {
                result.addWarning("物品配置目录不存在");
            }
            
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 导入结果类
     */
    public static class ImportResult {
        private boolean success = true;
        private String message = "";
        private boolean mainConfigImported = false;
        private boolean mainConfigFailed = false;
        private final Map<String, String> importedItemConfigs = new HashMap<>();
        private final Map<String, String> failedItemConfigs = new HashMap<>();
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public boolean isMainConfigImported() { return mainConfigImported; }
        public void addImportedMainConfig() { this.mainConfigImported = true; }
        
        public boolean isMainConfigFailed() { return mainConfigFailed; }
        public void addFailedMainConfig() { this.mainConfigFailed = true; }
        
        public Map<String, String> getImportedItemConfigs() { return importedItemConfigs; }
        public void addImportedItemConfig(String itemId) { importedItemConfigs.put(itemId, "成功"); }
        
        public Map<String, String> getFailedItemConfigs() { return failedItemConfigs; }
        public void addFailedItemConfig(String itemId, String reason) { failedItemConfigs.put(itemId, reason); }
        
        public int getTotalImported() { return importedItemConfigs.size(); }
        public int getTotalFailed() { return failedItemConfigs.size(); }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid = true;
        private String message = "";
        private boolean mainConfigValid = false;
        private int mainConfigItemsCount = 0;
        private final Map<String, String> validItemConfigs = new HashMap<>();
        private final Map<String, String> invalidItemConfigs = new HashMap<>();
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public boolean isMainConfigValid() { return mainConfigValid; }
        public void setMainConfigValid(boolean mainConfigValid) { this.mainConfigValid = mainConfigValid; }
        
        public int getMainConfigItemsCount() { return mainConfigItemsCount; }
        public void setMainConfigItemsCount(int mainConfigItemsCount) { this.mainConfigItemsCount = mainConfigItemsCount; }
        
        public Map<String, String> getValidItemConfigs() { return validItemConfigs; }
        public void addValidItemConfig(String itemId) { validItemConfigs.put(itemId, "有效"); }
        
        public Map<String, String> getInvalidItemConfigs() { return invalidItemConfigs; }
        public void addInvalidItemConfig(String itemId, String reason) { invalidItemConfigs.put(itemId, reason); }
        
        public java.util.List<String> getErrors() { return errors; }
        public void addError(String error) { errors.add(error); }
        
        public java.util.List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { warnings.add(warning); }
        
        public int getTotalValid() { return validItemConfigs.size(); }
        public int getTotalInvalid() { return invalidItemConfigs.size(); }
    }
}
