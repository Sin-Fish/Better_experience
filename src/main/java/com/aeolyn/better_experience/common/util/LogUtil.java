package com.aeolyn.better_experience.common.util;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 提供便捷的日志方法，支持模块化日志控制
 */
public class LogUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience");
    
    /**
     * 渲染模块日志
     */
    public static void renderDebug(String message, Object... args) {
        if (ConfigManager.getInstance().isRenderLogsEnabled()) {
            LOGGER.debug("[RENDER] " + message, args);
        }
    }
    
    public static void renderInfo(String message, Object... args) {
        if (ConfigManager.getInstance().isRenderLogsEnabled()) {
            LOGGER.info("[RENDER] " + message, args);
        }
    }
    
    public static void renderWarn(String message, Object... args) {
        if (ConfigManager.getInstance().isRenderLogsEnabled()) {
            LOGGER.warn("[RENDER] " + message, args);
        }
    }
    
    public static void renderError(String message, Object... args) {
        if (ConfigManager.getInstance().isRenderLogsEnabled()) {
            LOGGER.error("[RENDER] " + message, args);
        }
    }
    
    /**
     * 配置模块日志
     */
    public static void configDebug(String message, Object... args) {
        if (ConfigManager.getInstance().isConfigLogsEnabled()) {
            LOGGER.debug("[CONFIG] " + message, args);
        }
    }
    
    public static void configInfo(String message, Object... args) {
        if (ConfigManager.getInstance().isConfigLogsEnabled()) {
            LOGGER.info("[CONFIG] " + message, args);
        }
    }
    
    public static void configWarn(String message, Object... args) {
        if (ConfigManager.getInstance().isConfigLogsEnabled()) {
            LOGGER.warn("[CONFIG] " + message, args);
        }
    }
    
    public static void configError(String message, Object... args) {
        if (ConfigManager.getInstance().isConfigLogsEnabled()) {
            LOGGER.error("[CONFIG] " + message, args);
        }
    }
    
    /**
     * GUI模块日志
     */
    public static void guiDebug(String message, Object... args) {
        if (ConfigManager.getInstance().isGuiLogsEnabled()) {
            LOGGER.debug("[GUI] " + message, args);
        }
    }
    
    public static void guiInfo(String message, Object... args) {
        if (ConfigManager.getInstance().isGuiLogsEnabled()) {
            LOGGER.info("[GUI] " + message, args);
        }
    }
    
    public static void guiWarn(String message, Object... args) {
        if (ConfigManager.getInstance().isGuiLogsEnabled()) {
            LOGGER.warn("[GUI] " + message, args);
        }
    }
    
    public static void guiError(String message, Object... args) {
        if (ConfigManager.getInstance().isGuiLogsEnabled()) {
            LOGGER.error("[GUI] " + message, args);
        }
    }
    
    /**
     * Mixin模块日志
     */
    public static void mixinDebug(String message, Object... args) {
        if (ConfigManager.getInstance().isMixinLogsEnabled()) {
            LOGGER.debug("[MIXIN] " + message, args);
        }
    }
    
    public static void mixinInfo(String message, Object... args) {
        if (ConfigManager.getInstance().isMixinLogsEnabled()) {
            LOGGER.info("[MIXIN] " + message, args);
        }
    }
    
    public static void mixinWarn(String message, Object... args) {
        if (ConfigManager.getInstance().isMixinLogsEnabled()) {
            LOGGER.warn("[MIXIN] " + message, args);
        }
    }
    
    public static void mixinError(String message, Object... args) {
        if (ConfigManager.getInstance().isMixinLogsEnabled()) {
            LOGGER.error("[MIXIN] " + message, args);
        }
    }
    
    /**
     * 性能模块日志
     */
    public static void perfDebug(String message, Object... args) {
        if (ConfigManager.getInstance().isPerformanceLogsEnabled()) {
            LOGGER.debug("[PERF] " + message, args);
        }
    }
    
    public static void perfInfo(String message, Object... args) {
        if (ConfigManager.getInstance().isPerformanceLogsEnabled()) {
            LOGGER.info("[PERF] " + message, args);
        }
    }
    
    public static void perfWarn(String message, Object... args) {
        if (ConfigManager.getInstance().isPerformanceLogsEnabled()) {
            LOGGER.warn("[PERF] " + message, args);
        }
    }
    
    public static void perfError(String message, Object... args) {
        if (ConfigManager.getInstance().isPerformanceLogsEnabled()) {
            LOGGER.error("[PERF] " + message, args);
        }
    }
    
    /**
     * 通用日志（受调试开关控制）
     */
    public static void debug(String message, Object... args) {
        if (ConfigManager.getInstance().isDebugEnabled()) {
            LOGGER.debug(message, args);
        }
    }
    
    public static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }
    
    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }
    
    public static void error(String message, Object... args) {
        LOGGER.error(message, args);
    }
}
