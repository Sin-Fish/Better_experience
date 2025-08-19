package com.aeolyn.better_experience.common.config.loader;

import com.aeolyn.better_experience.common.config.ModConfig;
import com.aeolyn.better_experience.common.config.LogConfig;
import com.aeolyn.better_experience.common.config.exception.ConfigLoadException;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 模组配置加载器
 * 负责加载和解析模组的主配置文件
 */
public class ModConfigLoader {
    
    private static final String CONFIG_FILE_NAME = "mod_config.json";
    private static final String CONFIG_DIR = "config/better_experience";
    
    private final Gson gson;
    
    public ModConfigLoader() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    }
    
    /**
     * 加载模组配置
     * @return 模组配置对象
     * @throws ConfigLoadException 如果加载失败
     */
    public ModConfig loadModConfig() throws ConfigLoadException {
        try {
            LogUtil.logInitialization(LogUtil.MODULE_CONFIG, "模组配置");
            
            Path configPath = getConfigPath();
            
            // 如果配置文件不存在，创建默认配置
            if (!Files.exists(configPath)) {
                LogUtil.info(LogUtil.MODULE_CONFIG, "配置文件不存在，创建默认配置: {}", configPath);
                return createDefaultConfig();
            }
            
            // 读取配置文件
            String jsonContent = Files.readString(configPath);
            ModConfig config = gson.fromJson(jsonContent, ModConfig.class);
            
            // 验证配置
            validateConfig(config);
            
            LogUtil.logCompletion(LogUtil.MODULE_CONFIG, "模组配置");
            return config;
            
        } catch (JsonSyntaxException e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "配置文件格式错误: {}", e.getMessage(), e);
            throw new ConfigLoadException("配置文件格式错误", e);
        } catch (IOException e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "读取配置文件失败: {}", e.getMessage(), e);
            throw new ConfigLoadException("读取配置文件失败", e);
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "加载模组配置失败: {}", e.getMessage(), e);
            throw new ConfigLoadException("加载模组配置失败", e);
        }
    }
    
    /**
     * 从资源文件加载默认配置
     * @return 默认配置对象
     */
    public ModConfig loadDefaultConfig() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("assets/better_experience/default_config.json");
            
            if (inputStream == null) {
                LogUtil.warn(LogUtil.MODULE_CONFIG, "默认配置文件不存在，使用代码默认值");
                return createDefaultConfig();
            }
            
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                ModConfig config = gson.fromJson(reader, ModConfig.class);
                LogUtil.info(LogUtil.MODULE_CONFIG, "从资源文件加载默认配置");
                return config;
            }
            
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_CONFIG, "加载默认配置失败: {}", e.getMessage(), e);
            return createDefaultConfig();
        }
    }
    
    /**
     * 创建默认配置
     * @return 默认配置对象
     */
    private ModConfig createDefaultConfig() {
        ModConfig config = new ModConfig();
        
        // 设置默认值
        config.setRender3dEnabled(true);
        config.setOffhandRestrictionEnabled(false);
        config.setInventorySortEnabled(true);
        config.setDebugMode(false);
        config.setAutoSaveInterval(300);
        config.setConfigVersion("1.0.0");
        
        // 设置默认日志配置
        LogConfig logConfig = config.getLogConfig();
        logConfig.setEnableRenderLogs(false);
        logConfig.setEnableConfigLogs(true);
        logConfig.setEnableGuiLogs(false);
        logConfig.setEnableMixinLogs(false);
        logConfig.setEnablePerformanceLogs(false);
        
        // 设置默认性能配置
        ModConfig.PerformanceConfig perfConfig = config.getPerformanceConfig();
        perfConfig.setEnableCache(true);
        perfConfig.setCacheSize(1000);
        perfConfig.setEnableAsyncLoading(true);
        perfConfig.setMaxConcurrentOperations(4);
        
        // 设置默认界面配置
        ModConfig.UIConfig uiConfig = config.getUiConfig();
        uiConfig.setTheme("default");
        uiConfig.setLanguage("zh_cn");
        uiConfig.setShowTooltips(true);
        uiConfig.setAutoCloseDelay(3000);
        
        LogUtil.info(LogUtil.MODULE_CONFIG, "创建默认配置");
        return config;
    }
    
    /**
     * 验证配置
     * @param config 配置对象
     * @throws ConfigLoadException 如果配置无效
     */
    private void validateConfig(ModConfig config) throws ConfigLoadException {
        if (config == null) {
            throw new ConfigLoadException("配置对象为空");
        }
        
        // 验证自动保存间隔
        if (config.getAutoSaveInterval() < 0) {
            LogUtil.warn(LogUtil.MODULE_CONFIG, "自动保存间隔不能为负数，使用默认值300");
            config.setAutoSaveInterval(300);
        }
        
        // 验证性能配置
        ModConfig.PerformanceConfig perfConfig = config.getPerformanceConfig();
        if (perfConfig.getCacheSize() <= 0) {
            LogUtil.warn(LogUtil.MODULE_CONFIG, "缓存大小必须大于0，使用默认值1000");
            perfConfig.setCacheSize(1000);
        }
        
        if (perfConfig.getMaxConcurrentOperations() <= 0) {
            LogUtil.warn(LogUtil.MODULE_CONFIG, "最大并发操作数必须大于0，使用默认值4");
            perfConfig.setMaxConcurrentOperations(4);
        }
        
        // 验证界面配置
        ModConfig.UIConfig uiConfig = config.getUiConfig();
        if (uiConfig.getAutoCloseDelay() < 0) {
            LogUtil.warn(LogUtil.MODULE_CONFIG, "自动关闭延迟不能为负数，使用默认值3000");
            uiConfig.setAutoCloseDelay(3000);
        }
        
        LogUtil.info(LogUtil.MODULE_CONFIG, "配置验证通过");
    }
    
    /**
     * 获取配置文件路径
     * @return 配置文件路径
     */
    private Path getConfigPath() {
        return Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
    }
    
    /**
     * 获取配置目录路径
     * @return 配置目录路径
     */
    public Path getConfigDirectory() {
        return Paths.get(CONFIG_DIR);
    }
}
