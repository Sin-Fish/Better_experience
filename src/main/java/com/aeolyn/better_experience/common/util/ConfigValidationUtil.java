package com.aeolyn.better_experience.common.util;

import com.aeolyn.better_experience.common.config.validator.ValidationResult;
import com.aeolyn.better_experience.common.config.validator.impl.ItemConfigValidator;
import com.aeolyn.better_experience.common.config.validator.impl.OffHandConfigValidator;
import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;

import java.util.List;

/**
 * 统一配置验证工具类
 * 提供标准化的验证逻辑和结果处理
 * 使用新的验证器框架
 */
public class ConfigValidationUtil {
    
    private static final ItemConfigValidator itemValidator = new ItemConfigValidator();
    private static final OffHandConfigValidator offHandValidator = new OffHandConfigValidator();
    
    // ==================== 类型化验证方法 ====================
    
    /**
     * 验证主配置
     */
    public static ValidationResult validateItemsConfig(ItemsConfig config) {
        return itemValidator.validateItemsConfig(config);
    }
    
    /**
     * 验证物品配置
     */
    public static ValidationResult validateItemConfig(ItemConfig config) {
        return itemValidator.validate(config);
    }
    
    /**
     * 验证副手配置
     */
    public static ValidationResult validateOffHandConfig(OffHandRestrictionConfig config) {
        return offHandValidator.validate(config);
    }
    
    /**
     * 验证物品ID格式
     */
    public static ValidationResult validateItemId(String itemId) {
        return itemValidator.validateId(itemId, "物品ID");
    }
    
    /**
     * 验证多个配置
     */
    public static ValidationResult validateAll(Object... configs) {
        List<String> allErrors = new java.util.ArrayList<>();
        List<String> allWarnings = new java.util.ArrayList<>();
        
        for (Object config : configs) {
            ValidationResult result = validate(config);
            if (!result.isValid()) {
                allErrors.addAll(result.getErrors());
            }
            if (result.hasWarnings()) {
                allWarnings.addAll(result.getWarnings());
            }
        }
        
        if (allErrors.isEmpty()) {
            return ValidationResult.success(allWarnings);
        } else {
            return ValidationResult.failure(allErrors, allWarnings);
        }
    }
    
    /**
     * 通用验证方法
     */
    public static ValidationResult validate(Object config) {
        if (config == null) {
            return ValidationResult.failure(List.of("配置对象不能为空"));
        }
        
        if (config instanceof ItemsConfig) {
            return validateItemsConfig((ItemsConfig) config);
        } else if (config instanceof ItemConfig) {
            return validateItemConfig((ItemConfig) config);
        } else if (config instanceof OffHandRestrictionConfig) {
            return validateOffHandConfig((OffHandRestrictionConfig) config);
        } else {
            return ValidationResult.failure(List.of("不支持的配置类型: " + config.getClass().getSimpleName()));
        }
    }
    
    // ==================== 结果处理方法 ====================
    
    /**
     * 记录验证结果
     */
    public static void logValidationResult(String context, ValidationResult result) {
        if (result.isValid()) {
            if (result.hasWarnings()) {
                LogUtil.warn("CONFIG", "{} 验证通过，但有警告: {}", context, formatValidationWarnings(result));
            } else {
                LogUtil.info("CONFIG", "{} 验证通过", context);
            }
        } else {
            LogUtil.error("CONFIG", "{} 验证失败: {}", context, formatValidationErrors(result));
        }
    }
    
    /**
     * 格式化验证错误
     */
    public static String formatValidationErrors(ValidationResult result) {
        if (result == null || result.getErrors().isEmpty()) {
            return "无错误";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.getErrors().size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(result.getErrors().get(i));
        }
        return sb.toString();
    }
    
    /**
     * 格式化验证警告
     */
    public static String formatValidationWarnings(ValidationResult result) {
        if (result == null || result.getWarnings().isEmpty()) {
            return "无警告";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.getWarnings().size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(result.getWarnings().get(i));
        }
        return sb.toString();
    }
    
    /**
     * 格式化完整验证结果
     */
    public static String formatValidationResult(ValidationResult result) {
        if (result == null) {
            return "验证结果为空";
        }
        
        StringBuilder sb = new StringBuilder();
        if (!result.isValid()) {
            sb.append("错误: ").append(formatValidationErrors(result));
        }
        
        if (result.hasWarnings()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("警告: ").append(formatValidationWarnings(result));
        }
        
        if (sb.length() == 0) {
            sb.append("验证通过");
        }
        
        return sb.toString();
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 检查字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 检查数值是否在指定范围内
     */
    public static boolean isInRange(float value, float min, float max) {
        return value >= min && value <= max;
    }
    
    /**
     * 检查数值是否在指定范围内
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 检查列表是否为空或null
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
    
    /**
     * 检查列表是否包含重复元素
     */
    public static boolean hasDuplicates(List<String> list) {
        if (list == null || list.size() <= 1) {
            return false;
        }
        
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String item : list) {
            if (!seen.add(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查两个列表是否有交集
     */
    public static boolean hasIntersection(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return false;
        }
        
        java.util.Set<String> set1 = new java.util.HashSet<>(list1);
        for (String item : list2) {
            if (set1.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
