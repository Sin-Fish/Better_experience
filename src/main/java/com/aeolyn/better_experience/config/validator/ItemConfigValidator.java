package com.aeolyn.better_experience.config.validator;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品配置验证器实现
 */
public class ItemConfigValidator implements ConfigValidator {
    
    @Override
    public ValidationResult validate(ItemsConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config == null) {
            errors.add("ItemsConfig cannot be null");
            return ValidationResult.failure(errors);
        }
        
        if (config.getEnabledItems() == null) {
            errors.add("Enabled items list cannot be null");
        }
        
        if (config.getSettings() == null) {
            warnings.add("Settings is null, using defaults");
        }
        
        if (config.getLogConfig() == null) {
            warnings.add("Log config is null, using defaults");
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public ValidationResult validate(ItemConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config == null) {
            errors.add("ItemConfig cannot be null");
            return ValidationResult.failure(errors);
        }
        
        // 验证物品ID
        ValidationResult idResult = validateItemId(config.getItemId());
        if (!idResult.isValid()) {
            errors.addAll(idResult.getErrors());
        }
        
        // 验证渲染设置
        if (config.getFirstPerson() != null) {
            ValidationResult firstPersonResult = validateRenderSettings(config.getFirstPerson());
            if (!firstPersonResult.isValid()) {
                errors.addAll(firstPersonResult.getErrors());
            }
            if (firstPersonResult.hasWarnings()) {
                warnings.addAll(firstPersonResult.getWarnings());
            }
        } else {
            errors.add("First person render settings cannot be null");
        }
        
        if (config.getThirdPerson() != null) {
            ValidationResult thirdPersonResult = validateRenderSettings(config.getThirdPerson());
            if (!thirdPersonResult.isValid()) {
                errors.addAll(thirdPersonResult.getErrors());
            }
            if (thirdPersonResult.hasWarnings()) {
                warnings.addAll(thirdPersonResult.getWarnings());
            }
        } else {
            errors.add("Third person render settings cannot be null");
        }
        
        // 验证渲染模式
        if (!config.isRenderAsBlock() && !config.isRenderAsEntity()) {
            warnings.add("Neither block nor entity rendering is enabled");
        }
        
        if (config.isRenderAsBlock() && (config.getBlockId() == null || config.getBlockId().isEmpty())) {
            errors.add("Block ID is required when render_as_block is true");
        }
        
        if (config.isRenderAsEntity() && (config.getEntityType() == null || config.getEntityType().isEmpty())) {
            errors.add("Entity type is required when render_as_entity is true");
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
    
    @Override
    public ValidationResult validateItemId(String itemId) {
        List<String> errors = new ArrayList<>();
        
        if (itemId == null || itemId.isEmpty()) {
            errors.add("Item ID cannot be null or empty");
            return ValidationResult.failure(errors);
        }
        
        if (!itemId.contains(":")) {
            errors.add("Item ID must be in format 'namespace:item_name'");
        }
        
        if (itemId.length() > 256) {
            errors.add("Item ID is too long (max 256 characters)");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public ValidationResult validateRenderSettings(ItemConfig.RenderSettings settings) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (settings == null) {
            errors.add("Render settings cannot be null");
            return ValidationResult.failure(errors);
        }
        
        // 验证缩放值
        if (settings.getScale() <= 0) {
            errors.add("Scale must be greater than 0");
        } else if (settings.getScale() > 10.0f) {
            warnings.add("Scale value is very large: " + settings.getScale());
        }
        
        // 验证旋转值
        if (Math.abs(settings.getRotationX()) > 360) {
            warnings.add("Rotation X value is outside normal range: " + settings.getRotationX());
        }
        if (Math.abs(settings.getRotationY()) > 360) {
            warnings.add("Rotation Y value is outside normal range: " + settings.getRotationY());
        }
        if (Math.abs(settings.getRotationZ()) > 360) {
            warnings.add("Rotation Z value is outside normal range: " + settings.getRotationZ());
        }
        
        // 验证平移值
        if (Math.abs(settings.getTranslateX()) > 100) {
            warnings.add("Translate X value is very large: " + settings.getTranslateX());
        }
        if (Math.abs(settings.getTranslateY()) > 100) {
            warnings.add("Translate Y value is very large: " + settings.getTranslateY());
        }
        if (Math.abs(settings.getTranslateZ()) > 100) {
            warnings.add("Translate Z value is very large: " + settings.getTranslateZ());
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
}
