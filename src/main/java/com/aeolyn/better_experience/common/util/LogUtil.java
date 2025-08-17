package com.aeolyn.better_experience.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一日志工具类
 * 提供模块化日志管理和性能监控功能
 */
public class LogUtil {
    
    // 预定义的模块日志器
    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger("BetterExperience-Main");
    private static final Logger CONFIG_LOGGER = LoggerFactory.getLogger("BetterExperience-Config");
    private static final Logger GUI_LOGGER = LoggerFactory.getLogger("BetterExperience-GUI");
    private static final Logger RENDER_LOGGER = LoggerFactory.getLogger("BetterExperience-Render");
    private static final Logger OFFHAND_LOGGER = LoggerFactory.getLogger("BetterExperience-Offhand");
    private static final Logger VALIDATION_LOGGER = LoggerFactory.getLogger("BetterExperience-Validation");
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("BetterExperience-Performance");
    
    // 模块名称常量
    public static final String MODULE_MAIN = "Main";
    public static final String MODULE_CONFIG = "Config";
    public static final String MODULE_GUI = "GUI";
    public static final String MODULE_RENDER = "Render";
    public static final String MODULE_OFFHAND = "Offhand";
    public static final String MODULE_VALIDATION = "Validation";
    public static final String MODULE_PERFORMANCE = "Performance";
    
    /**
     * 获取指定模块的日志器
     */
    public static Logger getLogger(String module) {
        return LoggerFactory.getLogger("BetterExperience-" + module);
    }
    
    /**
     * 获取预定义的模块日志器
     */
    public static Logger getMainLogger() { return MAIN_LOGGER; }
    public static Logger getConfigLogger() { return CONFIG_LOGGER; }
    public static Logger getGuiLogger() { return GUI_LOGGER; }
    public static Logger getRenderLogger() { return RENDER_LOGGER; }
    public static Logger getOffhandLogger() { return OFFHAND_LOGGER; }
    public static Logger getValidationLogger() { return VALIDATION_LOGGER; }
    public static Logger getPerformanceLogger() { return PERFORMANCE_LOGGER; }
    
    // ==================== 基础日志方法 ====================
    
    /**
     * 信息日志
     */
    public static void info(String module, String message) {
        getLogger(module).info(message);
    }
    
    public static void info(String module, String message, Object... args) {
        getLogger(module).info(message, args);
    }
    
    public static void info(String module, String message, Throwable throwable) {
        getLogger(module).info(message, throwable);
    }
    
    /**
     * 警告日志
     */
    public static void warn(String module, String message) {
        getLogger(module).warn(message);
    }
    
    public static void warn(String module, String message, Object... args) {
        getLogger(module).warn(message, args);
    }
    
    public static void warn(String module, String message, Throwable throwable) {
        getLogger(module).warn(message, throwable);
    }
    
    /**
     * 错误日志
     */
    public static void error(String module, String message) {
        getLogger(module).error(message);
    }
    
    public static void error(String module, String message, Object... args) {
        getLogger(module).error(message, args);
    }
    
    public static void error(String module, String message, Throwable throwable) {
        getLogger(module).error(message, throwable);
    }
    
    /**
     * 调试日志
     */
    public static void debug(String module, String message) {
        getLogger(module).debug(message);
    }
    
    public static void debug(String module, String message, Object... args) {
        getLogger(module).debug(message, args);
    }
    
    // ==================== 业务日志方法 ====================
    
    /**
     * 配置操作日志
     */
    public static void logConfigOperation(String operation, String itemId, Object result) {
        CONFIG_LOGGER.info("配置操作: {} | 物品ID: {} | 结果: {}", operation, itemId, result);
    }
    
    public static void logConfigLoad(String itemId) {
        CONFIG_LOGGER.info("加载配置: {}", itemId);
    }
    
    public static void logConfigSave(String itemId) {
        CONFIG_LOGGER.info("保存配置: {}", itemId);
    }
    
    public static void logConfigValidation(String itemId, boolean isValid, String details) {
        if (isValid) {
            CONFIG_LOGGER.debug("配置验证通过: {} | {}", itemId, details);
        } else {
            CONFIG_LOGGER.warn("配置验证失败: {} | {}", itemId, details);
        }
    }
    
    /**
     * GUI操作日志
     */
    public static void logGuiAction(String action, String screen, Object details) {
        GUI_LOGGER.info("GUI操作: {} | 界面: {} | 详情: {}", action, screen, details);
    }
    
    public static void logScreenOpen(String screenName) {
        GUI_LOGGER.info("打开界面: {}", screenName);
    }
    
    public static void logScreenClose(String screenName) {
        GUI_LOGGER.info("关闭界面: {}", screenName);
    }
    
    public static void logButtonClick(String screenName, String buttonName) {
        GUI_LOGGER.debug("按钮点击: {} | {}", screenName, buttonName);
    }
    
    /**
     * 渲染操作日志
     */
    public static void logRenderOperation(String operation, String itemId, Object details) {
        RENDER_LOGGER.debug("渲染操作: {} | 物品ID: {} | 详情: {}", operation, itemId, details);
    }
    
    public static void logRenderStart(String itemId) {
        RENDER_LOGGER.debug("开始渲染: {}", itemId);
    }
    
    public static void logRenderEnd(String itemId, long duration) {
        RENDER_LOGGER.debug("渲染完成: {} | 耗时: {}ms", itemId, duration);
    }
    
    /**
     * 副手操作日志
     */
    public static void logOffhandOperation(String operation, String itemId, Object details) {
        OFFHAND_LOGGER.info("副手操作: {} | 物品ID: {} | 详情: {}", operation, itemId, details);
    }
    
    public static void logOffhandRestriction(String itemId, boolean restricted) {
        OFFHAND_LOGGER.debug("副手限制: {} | 状态: {}", itemId, restricted ? "限制" : "允许");
    }
    
    // ==================== 性能监控日志 ====================
    
    /**
     * 性能监控日志
     */
    public static void logPerformance(String module, String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        PERFORMANCE_LOGGER.info("性能统计: {} | 操作: {} | 耗时: {}ms", module, operation, duration);
    }
    
    public static void logPerformance(String module, String operation, long startTime, String details) {
        long duration = System.currentTimeMillis() - startTime;
        PERFORMANCE_LOGGER.info("性能统计: {} | 操作: {} | 耗时: {}ms | 详情: {}", 
            module, operation, duration, details);
    }
    
    /**
     * 内存使用日志
     */
    public static void logMemoryUsage(String module) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        PERFORMANCE_LOGGER.info("内存使用: {} | 已用: {}MB | 空闲: {}MB | 总计: {}MB | 最大: {}MB",
            module,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            totalMemory / 1024 / 1024,
            maxMemory / 1024 / 1024
        );
    }
    
    // ==================== 上下文日志 ====================
    
    /**
     * 带上下文的日志
     */
    public static void logWithContext(String module, String message, Map<String, Object> context) {
        StringBuilder contextStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (contextStr.length() > 0) {
                contextStr.append(", ");
            }
            contextStr.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        getLogger(module).info("{} | 上下文: {}", message, contextStr.toString());
    }
    
    /**
     * 带上下文的错误日志
     */
    public static void errorWithContext(String module, String message, Throwable throwable, Map<String, Object> context) {
        StringBuilder contextStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (contextStr.length() > 0) {
                contextStr.append(", ");
            }
            contextStr.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        getLogger(module).error("{} | 上下文: {}", message, contextStr.toString(), throwable);
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 初始化日志
     */
    public static void logInitialization(String module, String component) {
        info(module, "初始化组件: {}", component);
    }
    
    /**
     * 完成日志
     */
    public static void logCompletion(String module, String component) {
        info(module, "组件初始化完成: {}", component);
    }
    
    /**
     * 失败日志
     */
    public static void logFailure(String module, String operation, Throwable throwable) {
        error(module, "操作失败: {}", operation, throwable);
    }
    
    /**
     * 成功日志
     */
    public static void logSuccess(String module, String operation) {
        info(module, "操作成功: {}", operation);
    }
}
