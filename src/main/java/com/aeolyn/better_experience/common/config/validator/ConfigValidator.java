package com.aeolyn.better_experience.common.config.validator;

import java.util.List;

/**
 * 通用配置验证器接口
 * 定义所有模块都可以使用的验证方法
 */
public interface ConfigValidator<T> {
    
    /**
     * 验证配置对象
     */
    ValidationResult validate(T config);
    
    /**
     * 验证配置列表
     */
    default ValidationResult validateAll(List<T> configs) {
        if (configs == null || configs.isEmpty()) {
            return ValidationResult.success();
        }
        
        List<String> allErrors = new java.util.ArrayList<>();
        List<String> allWarnings = new java.util.ArrayList<>();
        
        for (T config : configs) {
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
     * 验证字符串ID格式
     */
    default ValidationResult validateId(String id, String idType) {
        List<String> errors = new java.util.ArrayList<>();
        
        if (id == null || id.isEmpty()) {
            errors.add(idType + "不能为空");
            return ValidationResult.failure(errors);
        }
        
        if (!id.contains(":")) {
            errors.add(idType + "格式无效，应为 'namespace:name' 格式");
        }
        
        if (id.length() > 256) {
            errors.add(idType + "过长，最大长度为256字符");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    /**
     * 验证数值范围
     */
    default ValidationResult validateRange(float value, float min, float max, String fieldName) {
        List<String> errors = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();
        
        if (value < min || value > max) {
            errors.add(fieldName + "值超出范围 [" + min + ", " + max + "]: " + value);
        } else if (Math.abs(value) > max * 0.8) {
            warnings.add(fieldName + "值较大: " + value);
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
}
