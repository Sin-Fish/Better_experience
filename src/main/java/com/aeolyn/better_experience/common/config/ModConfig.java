package com.aeolyn.better_experience.common.config;

import com.google.gson.annotations.SerializedName;

/**
 * 模组通用配置类
 * 统一管理所有模块的开关和通用设置
 */
public class ModConfig {
    
    // ==================== 模块开关 ====================
    
    @SerializedName("render3d_enabled")
    private boolean render3dEnabled = true;
    
    @SerializedName("offhand_restriction_enabled")
    private boolean offhandRestrictionEnabled = false;
    
    @SerializedName("inventory_sort_enabled")
    private boolean inventorySortEnabled = true;
    
    // ==================== 通用设置 ====================
    
    @SerializedName("debug_mode")
    private boolean debugMode = false;
    
    @SerializedName("auto_save_interval")
    private int autoSaveInterval = 300; // 5分钟
    
    @SerializedName("config_version")
    private String configVersion = "1.0.0";
    
    // ==================== 日志设置 ====================
    
    @SerializedName("log_config")
    private LogConfig logConfig = new LogConfig();
    
    // ==================== 性能设置 ====================
    
    @SerializedName("performance_config")
    private PerformanceConfig performanceConfig = new PerformanceConfig();
    
    // ==================== 界面设置 ====================
    
    @SerializedName("ui_config")
    private UIConfig uiConfig = new UIConfig();
    
    // ==================== 构造函数 ====================
    
    public ModConfig() {
        // 使用默认值初始化
    }
    
    // ==================== Getters and Setters ====================
    
    // 模块开关
    public boolean isRender3dEnabled() { return render3dEnabled; }
    public void setRender3dEnabled(boolean render3dEnabled) { this.render3dEnabled = render3dEnabled; }
    
    public boolean isOffhandRestrictionEnabled() { return offhandRestrictionEnabled; }
    public void setOffhandRestrictionEnabled(boolean offhandRestrictionEnabled) { this.offhandRestrictionEnabled = offhandRestrictionEnabled; }
    
    public boolean isInventorySortEnabled() { return inventorySortEnabled; }
    public void setInventorySortEnabled(boolean inventorySortEnabled) { this.inventorySortEnabled = inventorySortEnabled; }
    
    // 通用设置
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    
    public int getAutoSaveInterval() { return autoSaveInterval; }
    public void setAutoSaveInterval(int autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
    
    public String getConfigVersion() { return configVersion; }
    public void setConfigVersion(String configVersion) { this.configVersion = configVersion; }
    
    // 日志配置
    public LogConfig getLogConfig() { return logConfig; }
    public void setLogConfig(LogConfig logConfig) { this.logConfig = logConfig; }
    
    // 性能配置
    public PerformanceConfig getPerformanceConfig() { return performanceConfig; }
    public void setPerformanceConfig(PerformanceConfig performanceConfig) { this.performanceConfig = performanceConfig; }
    
    // 界面配置
    public UIConfig getUiConfig() { return uiConfig; }
    public void setUiConfig(UIConfig uiConfig) { this.uiConfig = uiConfig; }
    
    // ==================== 内部配置类 ====================
    
    /**
     * 性能配置
     */
    public static class PerformanceConfig {
        @SerializedName("enable_cache")
        private boolean enableCache = true;
        
        @SerializedName("cache_size")
        private int cacheSize = 1000;
        
        @SerializedName("enable_async_loading")
        private boolean enableAsyncLoading = true;
        
        @SerializedName("max_concurrent_operations")
        private int maxConcurrentOperations = 4;
        
        // Getters and Setters
        public boolean isEnableCache() { return enableCache; }
        public void setEnableCache(boolean enableCache) { this.enableCache = enableCache; }
        
        public int getCacheSize() { return cacheSize; }
        public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }
        
        public boolean isEnableAsyncLoading() { return enableAsyncLoading; }
        public void setEnableAsyncLoading(boolean enableAsyncLoading) { this.enableAsyncLoading = enableAsyncLoading; }
        
        public int getMaxConcurrentOperations() { return maxConcurrentOperations; }
        public void setMaxConcurrentOperations(int maxConcurrentOperations) { this.maxConcurrentOperations = maxConcurrentOperations; }
    }
    
    /**
     * 界面配置
     */
    public static class UIConfig {
        @SerializedName("theme")
        private String theme = "default";
        
        @SerializedName("language")
        private String language = "zh_cn";
        
        @SerializedName("show_tooltips")
        private boolean showTooltips = true;
        
        @SerializedName("auto_close_delay")
        private int autoCloseDelay = 3000; // 3秒
        
        // Getters and Setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public boolean isShowTooltips() { return showTooltips; }
        public void setShowTooltips(boolean showTooltips) { this.showTooltips = showTooltips; }
        
        public int getAutoCloseDelay() { return autoCloseDelay; }
        public void setAutoCloseDelay(int autoCloseDelay) { this.autoCloseDelay = autoCloseDelay; }
    }
}
