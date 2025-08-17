package com.aeolyn.better_experience.common.config.manager;

import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
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
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

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
            
            // 导出3D渲染主配置文件
            ItemsConfig itemsConfig = configManager.getItemsConfig();
            if (itemsConfig != null) {
                String mainConfigJson = GSON.toJson(itemsConfig);
                Path mainConfigPath = exportDir.resolve("items.json");
                Files.write(mainConfigPath, mainConfigJson.getBytes(StandardCharsets.UTF_8));
                LOGGER.info("3D渲染主配置文件导出成功: {}", mainConfigPath);
            }
            
            // 创建item_configs目录
            Path itemConfigsDir = exportDir.resolve("item_configs");
            if (!Files.exists(itemConfigsDir)) {
                Files.createDirectories(itemConfigsDir);
            }
            
            // 导出所有3D渲染物品配置
            Set<String> configuredItems = configManager.getAllConfiguredItems();
            for (String itemId : configuredItems) {
                ItemConfig itemConfig = configManager.getItemConfig(itemId);
                if (itemConfig != null) {
                    String fileName = itemId.replace(":", "_") + ".json";
                    Path itemConfigPath = itemConfigsDir.resolve(fileName);
                    String itemConfigJson = GSON.toJson(itemConfig);
                    Files.write(itemConfigPath, itemConfigJson.getBytes(StandardCharsets.UTF_8));
                    LOGGER.info("3D渲染物品配置导出成功: {} -> {}", itemId, itemConfigPath);
                }
            }
            
            // 导出副手限制配置
            OffHandRestrictionConfig offHandConfig = configManager.getOffHandRestrictionConfig();
            if (offHandConfig != null) {
                String offHandConfigJson = GSON.toJson(offHandConfig);
                Path offHandConfigPath = exportDir.resolve("offhand_restrictions.json");
                Files.write(offHandConfigPath, offHandConfigJson.getBytes(StandardCharsets.UTF_8));
                LOGGER.info("副手限制配置文件导出成功: {}", offHandConfigPath);
            }
            
            LOGGER.info("配置导出完成，共导出 {} 个3D渲染物品配置 + 副手限制配置", configuredItems.size());
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
            
            // 先导入物品配置，避免主配置更新时的冲突
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
                                    
                                    // 直接保存物品配置，不通过addItemConfig避免主配置冲突
                                    boolean success = saveItemConfigDirectly(configManager, itemConfig.getItemId(), itemConfig);
                                    if (success) {
                                        result.addImportedItemConfig(itemConfig.getItemId());
                                        LOGGER.info("物品配置导入成功: {} -> {}", itemConfigPath.getFileName(), itemConfig.getItemId());
                                    } else {
                                        result.addFailedItemConfig(itemConfig.getItemId(), "配置保存失败");
                                        LOGGER.warn("物品配置保存失败: {}", itemConfig.getItemId());
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
            
            // 最后导入3D渲染主配置文件
            Path mainConfigPath = importDir.resolve("items.json");
            if (Files.exists(mainConfigPath)) {
                try {
                    String mainConfigJson = Files.readString(mainConfigPath, StandardCharsets.UTF_8);
                    ItemsConfig importedItemsConfig = GSON.fromJson(mainConfigJson, ItemsConfig.class);
                    if (importedItemsConfig != null) {
                        // 更新主配置
                        configManager.updateItemsConfig(importedItemsConfig);
                        result.addImportedMainConfig();
                        LOGGER.info("3D渲染主配置文件导入成功: {}", mainConfigPath);
                    }
                } catch (Exception e) {
                    result.addFailedMainConfig();
                    LOGGER.error("3D渲染主配置文件导入失败: " + e.getMessage(), e);
                }
            }
            
            // 导入副手限制配置
            Path offHandConfigPath = importDir.resolve("offhand_restrictions.json");
            if (Files.exists(offHandConfigPath)) {
                try {
                    String offHandConfigJson = Files.readString(offHandConfigPath, StandardCharsets.UTF_8);
                    OffHandRestrictionConfig importedOffHandConfig = GSON.fromJson(offHandConfigJson, OffHandRestrictionConfig.class);
                    if (importedOffHandConfig != null) {
                        // 更新副手限制配置
                        configManager.updateOffHandRestrictionConfig(importedOffHandConfig);
                        result.addImportedOffHandConfig();
                        LOGGER.info("副手限制配置文件导入成功: {}", offHandConfigPath);
                    }
                } catch (Exception e) {
                    result.addFailedOffHandConfig();
                    LOGGER.error("副手限制配置文件导入失败: " + e.getMessage(), e);
                }
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
     * 直接保存物品配置，避免主配置冲突
     */
    private static boolean saveItemConfigDirectly(ConfigManager configManager, String itemId, ItemConfig config) {
        try {
            // 使用专门的导入方法，不更新主配置
            return configManager.importItemConfig(itemId, config);
        } catch (Exception e) {
            LOGGER.error("直接保存物品配置失败: {} - {}", itemId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证导入的配置
     * @param importPath 导入路径
     * @return 验证结果
     */
    public static ValidationResult validateImportConfigs(String importPath) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 首先进行文件结构验证
            ValidationResult structureResult = validateFileStructure(importPath);
            if (!structureResult.isValid()) {
                // 如果文件结构验证失败，直接返回结果
                return structureResult;
            }
            
            // 合并结构验证的信息
            result.getInfos().addAll(structureResult.getInfos());
            result.getWarnings().addAll(structureResult.getWarnings());
            result.getErrors().addAll(structureResult.getErrors());
            
            Path importDir = Paths.get(importPath);
            
            // 验证3D渲染主配置文件
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
                        result.addError("3D渲染主配置文件格式错误");
                    }
                } catch (Exception e) {
                    result.setMainConfigValid(false);
                    result.addError("3D渲染主配置文件解析失败: " + e.getMessage());
                }
            } else {
                result.addWarning("3D渲染主配置文件不存在");
            }
            
            // 验证副手限制配置文件
            Path offHandConfigPath = importDir.resolve("offhand_restrictions.json");
            if (Files.exists(offHandConfigPath)) {
                try {
                    String offHandConfigJson = Files.readString(offHandConfigPath, StandardCharsets.UTF_8);
                    OffHandRestrictionConfig offHandConfig = GSON.fromJson(offHandConfigJson, OffHandRestrictionConfig.class);
                    if (offHandConfig != null) {
                        result.setOffHandConfigValid(true);
                        result.setOffHandConfigItemsCount(offHandConfig.getAllowedItems() != null ? offHandConfig.getAllowedItems().size() : 0);
                    } else {
                        result.setOffHandConfigValid(false);
                        result.addError("副手限制配置文件格式错误");
                    }
                } catch (Exception e) {
                    result.setOffHandConfigValid(false);
                    result.addError("副手限制配置文件解析失败: " + e.getMessage());
                }
            } else {
                result.addWarning("副手限制配置文件不存在");
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
     * 验证导入目录的文件结构
     * 检查文件结构是否与配置文件的结构匹配
     * @param importPath 导入路径
     * @return 验证结果
     */
    public static ValidationResult validateFileStructure(String importPath) {
        ValidationResult result = new ValidationResult();
        
        try {
            Path importDir = Paths.get(importPath);
            if (!Files.exists(importDir)) {
                result.setValid(false);
                result.setMessage("导入目录不存在: " + importPath);
                return result;
            }
            
            if (!Files.isDirectory(importDir)) {
                result.setValid(false);
                result.setMessage("导入路径不是目录: " + importPath);
                return result;
            }
            
            // 检查必需的文件和目录
            boolean hasMainConfig = Files.exists(importDir.resolve("items.json"));
            boolean hasOffHandConfig = Files.exists(importDir.resolve("offhand_restrictions.json"));
            boolean hasItemConfigsDir = Files.exists(importDir.resolve("item_configs")) && 
                                      Files.isDirectory(importDir.resolve("item_configs"));
            
            // 验证文件结构完整性
            if (!hasMainConfig && !hasOffHandConfig && !hasItemConfigsDir) {
                result.setValid(false);
                result.setMessage("导入目录为空或格式不正确");
                result.addError("未找到任何配置文件");
                return result;
            }
            
            // 检查3D渲染配置结构
            if (hasMainConfig) {
                result.setMainConfigValid(true);
                result.addInfo("找到3D渲染主配置文件: items.json");
                
                // 检查主配置文件是否引用的物品配置都存在
                if (hasItemConfigsDir) {
                    try {
                        String mainConfigJson = Files.readString(importDir.resolve("items.json"), StandardCharsets.UTF_8);
                        ItemsConfig itemsConfig = GSON.fromJson(mainConfigJson, ItemsConfig.class);
                        if (itemsConfig != null && itemsConfig.getEnabledItems() != null) {
                            int missingConfigs = 0;
                            for (String itemId : itemsConfig.getEnabledItems()) {
                                String fileName = itemId.replace(":", "_") + ".json";
                                Path itemConfigPath = importDir.resolve("item_configs").resolve(fileName);
                                if (!Files.exists(itemConfigPath)) {
                                    missingConfigs++;
                                    result.addWarning("主配置引用的物品配置不存在: " + fileName);
                                }
                            }
                            if (missingConfigs == 0) {
                                result.addInfo("所有引用的物品配置文件都存在");
                            } else {
                                result.addWarning("有 " + missingConfigs + " 个引用的物品配置文件缺失");
                            }
                        }
                    } catch (Exception e) {
                        result.addError("无法解析主配置文件: " + e.getMessage());
                    }
                } else {
                    result.addWarning("主配置文件存在但缺少item_configs目录");
                }
            } else {
                result.addWarning("未找到3D渲染主配置文件");
            }
            
            // 检查副手限制配置
            if (hasOffHandConfig) {
                result.setOffHandConfigValid(true);
                result.addInfo("找到副手限制配置文件: offhand_restrictions.json");
            } else {
                result.addWarning("未找到副手限制配置文件");
            }
            
            // 检查物品配置目录
            if (hasItemConfigsDir) {
                try {
                    long configFileCount = Files.list(importDir.resolve("item_configs"))
                        .filter(path -> path.toString().endsWith(".json"))
                        .count();
                    
                    if (configFileCount > 0) {
                        result.addInfo("找到 " + configFileCount + " 个物品配置文件");
                        
                        // 检查是否有孤立的配置文件（不在主配置中引用的）
                        if (hasMainConfig) {
                            try {
                                String mainConfigJson = Files.readString(importDir.resolve("items.json"), StandardCharsets.UTF_8);
                                ItemsConfig itemsConfig = GSON.fromJson(mainConfigJson, ItemsConfig.class);
                                if (itemsConfig != null && itemsConfig.getEnabledItems() != null) {
                                    Set<String> referencedItems = new HashSet<>(itemsConfig.getEnabledItems());
                                    Set<String> existingFiles = new HashSet<>();
                                    
                                    Files.list(importDir.resolve("item_configs"))
                                        .filter(path -> path.toString().endsWith(".json"))
                                        .forEach(path -> {
                                            String fileName = path.getFileName().toString();
                                            String itemId = fileName.replace(".json", "").replace("_", ":");
                                            existingFiles.add(itemId);
                                        });
                                    
                                    // 找出孤立的配置文件
                                    Set<String> orphanedFiles = new HashSet<>(existingFiles);
                                    orphanedFiles.removeAll(referencedItems);
                                    
                                    if (!orphanedFiles.isEmpty()) {
                                        result.addWarning("发现 " + orphanedFiles.size() + " 个孤立的物品配置文件（未在主配置中引用）");
                                        for (String itemId : orphanedFiles) {
                                            result.addWarning("  • " + itemId.replace(":", "_") + ".json");
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                result.addError("检查孤立配置文件时出错: " + e.getMessage());
                            }
                        }
                    } else {
                        result.addWarning("item_configs目录为空");
                    }
                } catch (Exception e) {
                    result.addError("检查物品配置目录时出错: " + e.getMessage());
                }
            } else {
                result.addWarning("未找到item_configs目录");
            }
            
            // 检查是否有其他无关文件
            try {
                List<String> unexpectedFiles = new ArrayList<>();
                Files.list(importDir)
                    .filter(path -> !path.getFileName().toString().equals("items.json") &&
                                   !path.getFileName().toString().equals("offhand_restrictions.json") &&
                                   !path.getFileName().toString().equals("item_configs"))
                    .forEach(path -> unexpectedFiles.add(path.getFileName().toString()));
                
                if (!unexpectedFiles.isEmpty()) {
                    result.addWarning("发现 " + unexpectedFiles.size() + " 个无关文件:");
                    for (String fileName : unexpectedFiles) {
                        result.addWarning("  • " + fileName);
                    }
                }
            } catch (Exception e) {
                result.addError("检查无关文件时出错: " + e.getMessage());
            }
            
            // 设置整体验证结果
            if (result.getErrors().isEmpty()) {
                result.setValid(true);
                result.setMessage("文件结构验证通过");
            } else {
                result.setValid(false);
                result.setMessage("文件结构验证失败，发现 " + result.getErrors().size() + " 个错误");
            }
            
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("文件结构验证失败: " + e.getMessage());
            result.addError("验证过程出错: " + e.getMessage());
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
        private boolean offHandConfigImported = false;
        private boolean offHandConfigFailed = false;
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
        
        public boolean isOffHandConfigImported() { return offHandConfigImported; }
        public void addImportedOffHandConfig() { this.offHandConfigImported = true; }
        
        public boolean isOffHandConfigFailed() { return offHandConfigFailed; }
        public void addFailedOffHandConfig() { this.offHandConfigFailed = true; }
        
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
        private boolean offHandConfigValid = false;
        private int offHandConfigItemsCount = 0;
        private final Map<String, String> validItemConfigs = new HashMap<>();
        private final Map<String, String> invalidItemConfigs = new HashMap<>();
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        private final java.util.List<String> infos = new java.util.ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public boolean isMainConfigValid() { return mainConfigValid; }
        public void setMainConfigValid(boolean mainConfigValid) { this.mainConfigValid = mainConfigValid; }
        
        public int getMainConfigItemsCount() { return mainConfigItemsCount; }
        public void setMainConfigItemsCount(int mainConfigItemsCount) { this.mainConfigItemsCount = mainConfigItemsCount; }
        
        public boolean isOffHandConfigValid() { return offHandConfigValid; }
        public void setOffHandConfigValid(boolean offHandConfigValid) { this.offHandConfigValid = offHandConfigValid; }
        
        public int getOffHandConfigItemsCount() { return offHandConfigItemsCount; }
        public void setOffHandConfigItemsCount(int offHandConfigItemsCount) { this.offHandConfigItemsCount = offHandConfigItemsCount; }
        
        public Map<String, String> getValidItemConfigs() { return validItemConfigs; }
        public void addValidItemConfig(String itemId) { validItemConfigs.put(itemId, "有效"); }
        
        public Map<String, String> getInvalidItemConfigs() { return invalidItemConfigs; }
        public void addInvalidItemConfig(String itemId, String reason) { invalidItemConfigs.put(itemId, reason); }
        
        public java.util.List<String> getErrors() { return errors; }
        public void addError(String error) { errors.add(error); }
        
        public java.util.List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { warnings.add(warning); }
        
        public java.util.List<String> getInfos() { return infos; }
        public void addInfo(String info) { infos.add(info); }
        
        public int getTotalValid() { return validItemConfigs.size(); }
        public int getTotalInvalid() { return invalidItemConfigs.size(); }
    }
}
