package com.aeolyn.better_experience.common.config.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 基础配置验证器抽象类
 * 提供通用的验证逻辑和常量
 */
public abstract class BaseConfigValidator<T> implements ConfigValidator<T> {
    
    // 验证规则常量
    public static final float MIN_SCALE = 0.1f;
    public static final float MAX_SCALE = 10.0f;
    public static final float MIN_ROTATION = -360.0f;
    public static final float MAX_ROTATION = 360.0f;
    public static final float MIN_TRANSLATION = -10.0f;
    public static final float MAX_TRANSLATION = 10.0f;
    
    // 正则表达式
    protected static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9_]+:[a-z0-9_/]+$");
    
    // ==================== 通用验证方法 ====================
    
    /**
     * 验证对象是否为空
     */
    protected ValidationResult validateNotNull(Object obj, String objName) {
        if (obj == null) {
            return ValidationResult.failure(List.of(objName + "不能为空"));
        }
        return ValidationResult.success();
    }
    
    /**
     * 验证字符串是否为空
     */
    protected ValidationResult validateNotEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            return ValidationResult.failure(List.of(fieldName + "不能为空"));
        }
        return ValidationResult.success();
    }
    
    /**
     * 验证列表是否为空
     */
    protected ValidationResult validateNotEmpty(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            return ValidationResult.failure(List.of(fieldName + "不能为空"));
        }
        return ValidationResult.success();
    }
    
    /**
     * 验证ID格式
     */
    protected ValidationResult validateIdFormat(String id, String idType) {
        List<String> errors = new ArrayList<>();
        
        ValidationResult notEmptyResult = validateNotEmpty(id, idType);
        if (!notEmptyResult.isValid()) {
            return notEmptyResult;
        }
        
        if (!ID_PATTERN.matcher(id).matches()) {
            errors.add(idType + "格式无效，应为 'namespace:name' 格式");
        }
        
        if (id.length() > 256) {
            errors.add(idType + "过长，最大长度为256字符");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    /**
     * 验证缩放值
     */
    protected ValidationResult validateScale(float scale, String fieldName) {
        return validateRange(scale, MIN_SCALE, MAX_SCALE, fieldName);
    }
    
    /**
     * 验证旋转值
     */
    protected ValidationResult validateRotation(float rotation, String fieldName) {
        List<String> warnings = new ArrayList<>();
        
        if (Math.abs(rotation) > MAX_ROTATION) {
            warnings.add(fieldName + "值超出正常范围 [" + MIN_ROTATION + ", " + MAX_ROTATION + "]: " + rotation);
        }
        
        return ValidationResult.success(warnings);
    }
    
    /**
     * 验证平移值
     */
    protected ValidationResult validateTranslation(float translation, String fieldName) {
        List<String> warnings = new ArrayList<>();
        
        if (Math.abs(translation) > MAX_TRANSLATION) {
            warnings.add(fieldName + "值过大 [" + MIN_TRANSLATION + ", " + MAX_TRANSLATION + "]: " + translation);
        }
        
        return ValidationResult.success(warnings);
    }
    
    /**
     * 验证列表中的ID
     */
    protected ValidationResult validateIdList(List<String> idList, String listName, String idType) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (idList == null) {
            return ValidationResult.failure(List.of(listName + "不能为空"));
        }
        
        for (int i = 0; i < idList.size(); i++) {
            String id = idList.get(i);
            ValidationResult idResult = validateIdFormat(id, idType);
            if (!idResult.isValid()) {
                errors.add(listName + "[" + i + "] 中的" + idType + "无效: " + String.join(", ", idResult.getErrors()));
            }
        }
        
        // 检查重复
        if (hasDuplicates(idList)) {
            warnings.add(listName + "中存在重复的" + idType);
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
    
    /**
     * 检查列表是否包含重复元素
     */
    protected boolean hasDuplicates(List<String> list) {
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
     * 合并多个验证结果
     */
    protected ValidationResult mergeResults(ValidationResult... results) {
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        
        for (ValidationResult result : results) {
            if (result != null) {
                if (!result.isValid()) {
                    allErrors.addAll(result.getErrors());
                }
                if (result.hasWarnings()) {
                    allWarnings.addAll(result.getWarnings());
                }
            }
        }
        
        if (allErrors.isEmpty()) {
            return ValidationResult.success(allWarnings);
        } else {
            return ValidationResult.failure(allErrors, allWarnings);
        }
    }
}
