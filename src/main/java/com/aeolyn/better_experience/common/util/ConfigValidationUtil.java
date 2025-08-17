package com.aeolyn.better_experience.common.util;

import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.common.config.validator.ValidationResult;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 统一配置验证工具类
 * 提供标准化的验证逻辑和结果处理
 */
public class ConfigValidationUtil {
    
    // 验证规则常量
    public static final float MIN_SCALE = 0.1f;
    public static final float MAX_SCALE = 10.0f;
    public static final float MIN_ROTATION = -360.0f;
    public static final float MAX_ROTATION = 360.0f;
    public static final float MIN_TRANSLATION = -10.0f;
    public static final float MAX_TRANSLATION = 10.0f;
    
    // 正则表达式
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("^[a-z0-9_]+:[a-z0-9_/]+$");
    
    // ==================== 基础验证方法 ====================
    
    /**
     * 通用验证
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
    
    /**
     * 验证多个配置
     */
    public static ValidationResult validateAll(Object... configs) {
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        
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
    
    // ==================== 类型化验证方法 ====================
    
    /**
     * 验证主配置
     */
    public static ValidationResult validateItemsConfig(ItemsConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config == null) {
            return ValidationResult.failure(List.of("主配置不能为空"));
        }
        
        // 验证启用的物品列表
        if (config.getEnabledItems() != null) {
            for (String itemId : config.getEnabledItems()) {
                ValidationResult itemIdResult = validateItemId(itemId);
                if (!itemIdResult.isValid()) {
                    errors.add("启用物品列表中的无效物品ID: " + itemId + " - " + itemIdResult.getErrors());
                }
            }
        }
        
        // 验证设置
        if (config.getSettings() != null) {
            ValidationResult settingsResult = validateSettings(config.getSettings());
            if (!settingsResult.isValid()) {
                errors.addAll(settingsResult.getErrors());
            }
            if (settingsResult.hasWarnings()) {
                warnings.addAll(settingsResult.getWarnings());
            }
        }
        
        // 验证日志配置
        if (config.getLogConfig() != null) {
            // 日志配置的验证逻辑可以在这里添加
            // 目前没有特殊的验证要求
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        } else {
            return ValidationResult.failure(errors, warnings);
        }
    }
    
    /**
     * 验证物品配置
     */
    public static ValidationResult validateItemConfig(ItemConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config == null) {
            return ValidationResult.failure(List.of("物品配置不能为空"));
        }
        
        // 验证物品ID
        ValidationResult idResult = validateItemId(config.getItemId());
        if (!idResult.isValid()) {
            errors.addAll(idResult.getErrors());
        }
        
        // 验证第一人称设置
        if (config.getFirstPerson() != null) {
            ValidationResult firstPersonResult = validateRenderSettings(config.getFirstPerson(), "第一人称");
            if (!firstPersonResult.isValid()) {
                errors.addAll(firstPersonResult.getErrors());
            }
            if (firstPersonResult.hasWarnings()) {
                warnings.addAll(firstPersonResult.getWarnings());
            }
        }
        
        // 验证第三人称设置
        if (config.getThirdPerson() != null) {
            ValidationResult thirdPersonResult = validateRenderSettings(config.getThirdPerson(), "第三人称");
            if (!thirdPersonResult.isValid()) {
                errors.addAll(thirdPersonResult.getErrors());
            }
            if (thirdPersonResult.hasWarnings()) {
                warnings.addAll(thirdPersonResult.getWarnings());
            }
        }
        
        // 验证方块ID（如果渲染为方块）
        if (config.isRenderAsBlock() && config.getBlockId() != null) {
            ValidationResult blockIdResult = validateItemId(config.getBlockId());
            if (!blockIdResult.isValid()) {
                errors.add("无效的方块ID: " + config.getBlockId());
            }
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        } else {
            return ValidationResult.failure(errors, warnings);
        }
    }
    
    /**
     * 验证副手限制配置
     */
    public static ValidationResult validateOffHandConfig(OffHandRestrictionConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config == null) {
            return ValidationResult.failure(List.of("副手限制配置不能为空"));
        }
        
        // 验证白名单物品
        if (config.getAllowedItems() != null) {
            for (String itemId : config.getAllowedItems()) {
                ValidationResult itemIdResult = validateItemId(itemId);
                if (!itemIdResult.isValid()) {
                    errors.add("白名单中的无效物品ID: " + itemId + " - " + itemIdResult.getErrors());
                }
            }
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        } else {
            return ValidationResult.failure(errors, warnings);
        }
    }
    
    // ==================== 字段验证方法 ====================
    
    /**
     * 验证字符串
     */
    public static ValidationResult validateString(String value, String fieldName, boolean required) {
        List<String> errors = new ArrayList<>();
        
        if (required && (value == null || value.trim().isEmpty())) {
            errors.add(fieldName + "不能为空");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证字符串长度
     */
    public static ValidationResult validateString(String value, String fieldName, int minLength, int maxLength) {
        List<String> errors = new ArrayList<>();
        
        if (value == null) {
            errors.add(fieldName + "不能为空");
        } else if (value.length() < minLength) {
            errors.add(fieldName + "长度不能少于" + minLength + "个字符");
        } else if (value.length() > maxLength) {
            errors.add(fieldName + "长度不能超过" + maxLength + "个字符");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证数值范围
     */
    public static ValidationResult validateNumber(Number value, String fieldName, double min, double max) {
        List<String> errors = new ArrayList<>();
        
        if (value == null) {
            errors.add(fieldName + "不能为空");
        } else {
            double numValue = value.doubleValue();
            if (numValue < min) {
                errors.add(fieldName + "不能小于" + min);
            } else if (numValue > max) {
                errors.add(fieldName + "不能大于" + max);
            }
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证整数范围
     */
    public static ValidationResult validateInteger(int value, String fieldName, int min, int max) {
        List<String> errors = new ArrayList<>();
        
        if (value < min) {
            errors.add(fieldName + "不能小于" + min);
        } else if (value > max) {
            errors.add(fieldName + "不能大于" + max);
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证列表
     */
    public static ValidationResult validateList(List<?> list, String fieldName, boolean required) {
        List<String> errors = new ArrayList<>();
        
        if (required && (list == null || list.isEmpty())) {
            errors.add(fieldName + "不能为空");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证列表大小
     */
    public static ValidationResult validateList(List<?> list, String fieldName, int minSize, int maxSize) {
        List<String> errors = new ArrayList<>();
        
        if (list == null) {
            errors.add(fieldName + "不能为空");
        } else if (list.size() < minSize) {
            errors.add(fieldName + "元素数量不能少于" + minSize + "个");
        } else if (list.size() > maxSize) {
            errors.add(fieldName + "元素数量不能超过" + maxSize + "个");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    // ==================== 业务规则验证 ====================
    
    /**
     * 验证物品ID
     */
    public static ValidationResult validateItemId(String itemId) {
        List<String> errors = new ArrayList<>();
        
        if (itemId == null || itemId.trim().isEmpty()) {
            errors.add("物品ID不能为空");
        } else if (!ITEM_ID_PATTERN.matcher(itemId).matches()) {
            errors.add("物品ID格式无效: " + itemId + " (应为 namespace:path 格式)");
        } else {
            try {
                Identifier.of(itemId);
            } catch (Exception e) {
                errors.add("物品ID格式错误: " + itemId + " - " + e.getMessage());
            }
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    /**
     * 验证物品ID（检查注册表）
     */
    public static ValidationResult validateItemId(String itemId, boolean checkRegistry) {
        ValidationResult basicResult = validateItemId(itemId);
        if (!basicResult.isValid()) {
            return basicResult;
        }
        
        if (checkRegistry) {
            try {
                // 这里可以添加注册表检查逻辑
                // 暂时返回成功
                return ValidationResult.success();
            } catch (Exception e) {
                return ValidationResult.failure(List.of("物品ID在注册表中不存在: " + itemId));
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 验证渲染设置
     */
    public static ValidationResult validateRenderSettings(ItemConfig.RenderSettings settings, String context) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (settings == null) {
            return ValidationResult.failure(List.of(context + "渲染设置不能为空"));
        }
        
        // 验证缩放
        ValidationResult scaleResult = validateScale(settings.getScale());
        if (!scaleResult.isValid()) {
            errors.add(context + "缩放设置: " + scaleResult.getErrors());
        }
        
        // 验证旋转
        ValidationResult rotationXResult = validateRotation(settings.getRotationX());
        if (!rotationXResult.isValid()) {
            errors.add(context + "X轴旋转: " + rotationXResult.getErrors());
        }
        
        ValidationResult rotationYResult = validateRotation(settings.getRotationY());
        if (!rotationYResult.isValid()) {
            errors.add(context + "Y轴旋转: " + rotationYResult.getErrors());
        }
        
        ValidationResult rotationZResult = validateRotation(settings.getRotationZ());
        if (!rotationZResult.isValid()) {
            errors.add(context + "Z轴旋转: " + rotationZResult.getErrors());
        }
        
        // 验证平移
        ValidationResult translateXResult = validateTranslation(settings.getTranslateX());
        if (!translateXResult.isValid()) {
            errors.add(context + "X轴平移: " + translateXResult.getErrors());
        }
        
        ValidationResult translateYResult = validateTranslation(settings.getTranslateY());
        if (!translateYResult.isValid()) {
            errors.add(context + "Y轴平移: " + translateYResult.getErrors());
        }
        
        ValidationResult translateZResult = validateTranslation(settings.getTranslateZ());
        if (!translateZResult.isValid()) {
            errors.add(context + "Z轴平移: " + translateZResult.getErrors());
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        } else {
            return ValidationResult.failure(errors, warnings);
        }
    }
    
    /**
     * 验证缩放值
     */
    public static ValidationResult validateScale(float scale) {
        return validateNumber(scale, "缩放", MIN_SCALE, MAX_SCALE);
    }
    
    /**
     * 验证旋转值
     */
    public static ValidationResult validateRotation(float rotation) {
        return validateNumber(rotation, "旋转", MIN_ROTATION, MAX_ROTATION);
    }
    
    /**
     * 验证平移值
     */
    public static ValidationResult validateTranslation(float translation) {
        return validateNumber(translation, "平移", MIN_TRANSLATION, MAX_TRANSLATION);
    }
    
    // ==================== 验证规则管理 ====================
    
    /**
     * 验证规则接口
     */
    public interface ValidationRule {
        ValidationResult validate(Object value);
    }
    
    /**
     * 必填验证规则
     */
    public static ValidationRule required(String fieldName) {
        return value -> {
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                return ValidationResult.failure(List.of(fieldName + "不能为空"));
            }
            return ValidationResult.success();
        };
    }
    
    /**
     * 范围验证规则
     */
    public static ValidationRule range(String fieldName, double min, double max) {
        return value -> {
            if (value instanceof Number) {
                return validateNumber((Number) value, fieldName, min, max);
            }
            return ValidationResult.failure(List.of(fieldName + "必须是数值类型"));
        };
    }
    
    /**
     * 模式验证规则
     */
    public static ValidationRule pattern(String fieldName, String regex) {
        return value -> {
            if (value instanceof String) {
                if (!Pattern.matches(regex, (String) value)) {
                    return ValidationResult.failure(List.of(fieldName + "格式不正确"));
                }
                return ValidationResult.success();
            }
            return ValidationResult.failure(List.of(fieldName + "必须是字符串类型"));
        };
    }
    
    /**
     * 自定义验证规则
     */
    public static ValidationRule custom(String fieldName, Predicate<Object> validator) {
        return value -> {
            if (validator.test(value)) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure(List.of(fieldName + "验证失败"));
            }
        };
    }
    
    /**
     * 使用规则验证
     */
    public static ValidationResult validateWithRules(Object config, List<ValidationRule> rules) {
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        
        for (ValidationRule rule : rules) {
            ValidationResult result = rule.validate(config);
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
    
    // ==================== 验证结果处理 ====================
    
    /**
     * 检查验证结果是否有效
     */
    public static boolean isValid(ValidationResult result) {
        return result != null && result.isValid();
    }
    
    /**
     * 如果验证失败则抛出异常
     */
    public static void throwIfInvalid(ValidationResult result) {
        if (!isValid(result)) {
            throw new IllegalArgumentException("配置验证失败: " + formatValidationErrors(result));
        }
    }
    
    /**
     * 记录验证结果
     */
    public static void logValidationResult(String module, ValidationResult result) {
        if (isValid(result)) {
            LogUtil.logConfigValidation(module, true, "验证通过");
            if (result.hasWarnings()) {
                LogUtil.warn(LogUtil.MODULE_VALIDATION, "配置验证警告: {}", result.getWarnings());
            }
        } else {
            LogUtil.logConfigValidation(module, false, formatValidationErrors(result));
        }
    }
    
    /**
     * 格式化验证错误
     */
    public static String formatValidationErrors(ValidationResult result) {
        if (result == null || result.isValid()) {
            return "验证通过";
        }
        
        StringBuilder sb = new StringBuilder();
        for (String error : result.getErrors()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(error);
        }
        return sb.toString();
    }
    
    // ==================== 私有辅助方法 ====================
    
    private static ValidationResult validateSettings(ItemsConfig.Settings settings) {
        List<String> errors = new ArrayList<>();
        
        if (settings == null) {
            return ValidationResult.failure(List.of("设置不能为空"));
        }
        
        // 验证默认缩放
        ValidationResult scaleResult = validateScale(settings.getDefaultScale());
        if (!scaleResult.isValid()) {
            errors.add("默认缩放: " + scaleResult.getErrors());
        }
        
        // 验证默认旋转
        ValidationResult rotationResult = validateRotation(settings.getDefaultRotationX());
        if (!rotationResult.isValid()) {
            errors.add("默认X轴旋转: " + rotationResult.getErrors());
        }
        
        rotationResult = validateRotation(settings.getDefaultRotationY());
        if (!rotationResult.isValid()) {
            errors.add("默认Y轴旋转: " + rotationResult.getErrors());
        }
        
        rotationResult = validateRotation(settings.getDefaultRotationZ());
        if (!rotationResult.isValid()) {
            errors.add("默认Z轴旋转: " + rotationResult.getErrors());
        }
        
        // 验证默认平移
        ValidationResult translationResult = validateTranslation(settings.getDefaultTranslateX());
        if (!translationResult.isValid()) {
            errors.add("默认X轴平移: " + translationResult.getErrors());
        }
        
        translationResult = validateTranslation(settings.getDefaultTranslateY());
        if (!translationResult.isValid()) {
            errors.add("默认Y轴平移: " + translationResult.getErrors());
        }
        
        translationResult = validateTranslation(settings.getDefaultTranslateZ());
        if (!translationResult.isValid()) {
            errors.add("默认Z轴平移: " + translationResult.getErrors());
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    private static ValidationResult validateLogConfig(com.aeolyn.better_experience.common.config.LogConfig logConfig) {
        List<String> errors = new ArrayList<>();
        
        if (logConfig == null) {
            return ValidationResult.failure(List.of("日志配置不能为空"));
        }
        
        // 日志配置的验证逻辑可以在这里添加
        // 目前没有特殊的验证要求
        
        return ValidationResult.success();
    }
}
