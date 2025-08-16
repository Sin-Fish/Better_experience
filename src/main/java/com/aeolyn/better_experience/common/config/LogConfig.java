package com.aeolyn.better_experience.common.config;

import com.google.gson.annotations.SerializedName;

/**
 * 日志配置类
 * 用于管理不同模块的日志开关
 */
public class LogConfig {
    
    @SerializedName("enable_debug_logs")
    private boolean enableDebugLogs = true;
    
    @SerializedName("enable_render_logs")
    private boolean enableRenderLogs = false;
    
    @SerializedName("enable_config_logs")
    private boolean enableConfigLogs = true;
    
    @SerializedName("enable_gui_logs")
    private boolean enableGuiLogs = false;
    
    @SerializedName("enable_mixin_logs")
    private boolean enableMixinLogs = false;
    
    @SerializedName("enable_performance_logs")
    private boolean enablePerformanceLogs = false;
    
    // 默认构造函数
    public LogConfig() {}
    
    // 全参数构造函数
    public LogConfig(boolean enableDebugLogs, boolean enableRenderLogs, boolean enableConfigLogs, 
                    boolean enableGuiLogs, boolean enableMixinLogs, boolean enablePerformanceLogs) {
        this.enableDebugLogs = enableDebugLogs;
        this.enableRenderLogs = enableRenderLogs;
        this.enableConfigLogs = enableConfigLogs;
        this.enableGuiLogs = enableGuiLogs;
        this.enableMixinLogs = enableMixinLogs;
        this.enablePerformanceLogs = enablePerformanceLogs;
    }
    
    // Getters
    public boolean isEnableDebugLogs() {
        return enableDebugLogs;
    }
    
    public boolean isEnableRenderLogs() {
        return enableRenderLogs;
    }
    
    public boolean isEnableConfigLogs() {
        return enableConfigLogs;
    }
    
    public boolean isEnableGuiLogs() {
        return enableGuiLogs;
    }
    
    public boolean isEnableMixinLogs() {
        return enableMixinLogs;
    }
    
    public boolean isEnablePerformanceLogs() {
        return enablePerformanceLogs;
    }
    
    // Setters
    public void setEnableDebugLogs(boolean enableDebugLogs) {
        this.enableDebugLogs = enableDebugLogs;
    }
    
    public void setEnableRenderLogs(boolean enableRenderLogs) {
        this.enableRenderLogs = enableRenderLogs;
    }
    
    public void setEnableConfigLogs(boolean enableConfigLogs) {
        this.enableConfigLogs = enableConfigLogs;
    }
    
    public void setEnableGuiLogs(boolean enableGuiLogs) {
        this.enableGuiLogs = enableGuiLogs;
    }
    
    public void setEnableMixinLogs(boolean enableMixinLogs) {
        this.enableMixinLogs = enableMixinLogs;
    }
    
    public void setEnablePerformanceLogs(boolean enablePerformanceLogs) {
        this.enablePerformanceLogs = enablePerformanceLogs;
    }
    
    /**
     * 获取默认配置
     */
    public static LogConfig getDefault() {
        return new LogConfig(true, false, true, false, false, false);
    }
    
    /**
     * 启用所有日志（调试模式）
     */
    public static LogConfig getDebugMode() {
        return new LogConfig(true, true, true, true, true, true);
    }
    
    /**
     * 禁用所有日志（生产模式）
     */
    public static LogConfig getProductionMode() {
        return new LogConfig(false, false, false, false, false, false);
    }
}
