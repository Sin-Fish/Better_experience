package com.aeolyn.better_experience.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试配置管理器
 * 用于控制各个模块的调试信息显示
 */
public class DebugConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Debug");
    private static volatile DebugConfig instance;
    
    @SerializedName("debug_enabled")
    private boolean debugEnabled = true;
    
    @SerializedName("modules")
    private Map<String, ModuleConfig> modules = new HashMap<>();
    
    @SerializedName("log_levels")
    private Map<String, Integer> logLevels = new HashMap<>();
    
    private DebugConfig() {
        // 初始化默认配置
        initializeDefaultConfig();
    }
    
    /**
     * 获取单例实例
     */
    public static DebugConfig getInstance() {
        if (instance == null) {
            synchronized (DebugConfig.class) {
                if (instance == null) {
                    instance = new DebugConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化默认配置
     */
    private void initializeDefaultConfig() {
        // 设置默认模块配置
        modules.put("inventory", new ModuleConfig(false, "INFO"));
        modules.put("render3d", new ModuleConfig(false, "INFO"));
        modules.put("offhand", new ModuleConfig(false, "INFO"));
        modules.put("importexport", new ModuleConfig(false, "INFO"));
        modules.put("config", new ModuleConfig(false, "INFO"));
        modules.put("keybindings", new ModuleConfig(false, "INFO"));
        modules.put("gui", new ModuleConfig(false, "INFO"));
        modules.put("general", new ModuleConfig(true, "INFO"));
        
        // 设置日志级别
        logLevels.put("TRACE", 0);
        logLevels.put("DEBUG", 1);
        logLevels.put("INFO", 2);
        logLevels.put("WARN", 3);
        logLevels.put("ERROR", 4);
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig(ResourceManager resourceManager) {
        try {
            Identifier configId = Identifier.of("better_experience", "debug_config.json");
            Resource resource = resourceManager.getResource(configId).orElse(null);
            
            if (resource != null) {
                try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    DebugConfig loadedConfig = gson.fromJson(reader, DebugConfig.class);
                    
                    if (loadedConfig != null) {
                        this.debugEnabled = loadedConfig.debugEnabled;
                        this.modules = loadedConfig.modules;
                        this.logLevels = loadedConfig.logLevels;
                        
                        LOGGER.info("调试配置加载成功");
                    }
                }
            } else {
                LOGGER.warn("未找到调试配置文件，使用默认配置");
            }
        } catch (Exception e) {
            LOGGER.error("加载调试配置失败", e);
        }
    }
    
    /**
     * 检查模块是否启用调试
     */
    public boolean isModuleEnabled(String moduleName) {
        if (!debugEnabled) {
            return false;
        }
        
        ModuleConfig moduleConfig = modules.get(moduleName);
        return moduleConfig != null && moduleConfig.isEnabled();
    }
    
    /**
     * 检查模块的日志级别
     */
    public boolean shouldLog(String moduleName, String level) {
        if (!isModuleEnabled(moduleName)) {
            return false;
        }
        
        ModuleConfig moduleConfig = modules.get(moduleName);
        if (moduleConfig == null) {
            return false;
        }
        
        Integer moduleLevel = logLevels.get(moduleConfig.getLevel());
        Integer currentLevel = logLevels.get(level);
        
        if (moduleLevel == null || currentLevel == null) {
            return false;
        }
        
        return currentLevel >= moduleLevel;
    }
    
    /**
     * 获取模块配置
     */
    public ModuleConfig getModuleConfig(String moduleName) {
        return modules.get(moduleName);
    }
    
    /**
     * 设置模块调试状态
     */
    public void setModuleEnabled(String moduleName, boolean enabled) {
        ModuleConfig moduleConfig = modules.get(moduleName);
        if (moduleConfig != null) {
            moduleConfig.setEnabled(enabled);
        }
    }
    
    /**
     * 设置模块日志级别
     */
    public void setModuleLevel(String moduleName, String level) {
        ModuleConfig moduleConfig = modules.get(moduleName);
        if (moduleConfig != null) {
            moduleConfig.setLevel(level);
        }
    }
    
    // ==================== Getters and Setters ====================
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
    
    public Map<String, ModuleConfig> getModules() {
        return modules;
    }
    
    public Map<String, Integer> getLogLevels() {
        return logLevels;
    }
    
    /**
     * 模块配置内部类
     */
    public static class ModuleConfig {
        @SerializedName("enabled")
        private boolean enabled;
        
        @SerializedName("level")
        private String level;
        
        public ModuleConfig(boolean enabled, String level) {
            this.enabled = enabled;
            this.level = level;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
    }
}
