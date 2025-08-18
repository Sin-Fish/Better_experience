package com.aeolyn.better_experience.common.config.validator.impl;

import com.aeolyn.better_experience.common.config.validator.BaseConfigValidator;
import com.aeolyn.better_experience.common.config.validator.ValidationResult;
import com.aeolyn.better_experience.render3d.config.ItemsConfig;
import com.aeolyn.better_experience.render3d.config.ItemConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品配置验证器实现
 * 继承基础验证器，使用统一的验证逻辑
 */
public class ItemConfigValidator extends BaseConfigValidator<ItemConfig> {
    
    @Override
    public ValidationResult validate(ItemConfig config) {
        if (config == null) {
            return ValidationResult.failure(List.of("物品配置不能为空"));
        }
        
        List<ValidationResult> results = new ArrayList<>();
        
        // 验证物品ID
        results.add(validateIdFormat(config.getItemId(), "物品ID"));
        
        // 验证第一人称渲染设置
        if (config.getFirstPerson() != null) {
            results.add(validateRenderSettings(config.getFirstPerson(), "第一人称"));
        } else {
            results.add(ValidationResult.failure(List.of("第一人称渲染设置不能为空")));
        }
        
        // 验证第三人称渲染设置
        if (config.getThirdPerson() != null) {
            results.add(validateRenderSettings(config.getThirdPerson(), "第三人称"));
        } else {
            results.add(ValidationResult.failure(List.of("第三人称渲染设置不能为空")));
        }
        
        // 验证渲染模式
        results.add(validateRenderMode(config));
        
        return mergeResults(results.toArray(new ValidationResult[0]));
    }
    
    /**
     * 验证渲染设置
     */
    private ValidationResult validateRenderSettings(ItemConfig.RenderSettings settings, String context) {
        if (settings == null) {
            return ValidationResult.failure(List.of(context + "渲染设置不能为空"));
        }
        
        List<ValidationResult> results = new ArrayList<>();
        
        // 验证缩放
        results.add(validateScale(settings.getScale(), context + "缩放"));
        
        // 验证旋转
        results.add(validateRotation(settings.getRotationX(), context + "X轴旋转"));
        results.add(validateRotation(settings.getRotationY(), context + "Y轴旋转"));
        results.add(validateRotation(settings.getRotationZ(), context + "Z轴旋转"));
        
        // 验证平移
        results.add(validateTranslation(settings.getTranslateX(), context + "X轴平移"));
        results.add(validateTranslation(settings.getTranslateY(), context + "Y轴平移"));
        results.add(validateTranslation(settings.getTranslateZ(), context + "Z轴平移"));
        
        return mergeResults(results.toArray(new ValidationResult[0]));
    }
    
    /**
     * 验证渲染模式
     */
    private ValidationResult validateRenderMode(ItemConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!config.isRenderAsBlock() && !config.isRenderAsEntity()) {
            warnings.add("既未启用方块渲染也未启用实体渲染");
        }
        
        if (config.isRenderAsBlock() && (config.getBlockId() == null || config.getBlockId().isEmpty())) {
            errors.add("启用方块渲染时必须指定方块ID");
        }
        
        if (config.isRenderAsEntity() && (config.getEntityType() == null || config.getEntityType().isEmpty())) {
            errors.add("启用实体渲染时必须指定实体类型");
        }
        
        return errors.isEmpty() ? ValidationResult.success(warnings) : ValidationResult.failure(errors, warnings);
    }
    
    /**
     * 验证主配置
     */
    public ValidationResult validateItemsConfig(ItemsConfig config) {
        if (config == null) {
            return ValidationResult.failure(List.of("主配置不能为空"));
        }
        
        List<ValidationResult> results = new ArrayList<>();
        
        // 验证启用的物品列表
        if (config.getEnabledItems() != null) {
            results.add(validateIdList(config.getEnabledItems(), "启用物品列表", "物品ID"));
        }
        
        // 验证设置
        if (config.getSettings() != null) {
            results.add(validateSettings(config.getSettings()));
        }
        
        // 验证日志配置
        if (config.getLogConfig() != null) {
            results.add(validateLogConfig(config.getLogConfig()));
        }
        
        return mergeResults(results.toArray(new ValidationResult[0]));
    }
    
    /**
     * 验证设置
     */
    private ValidationResult validateSettings(ItemsConfig.Settings settings) {
        if (settings == null) {
            return ValidationResult.failure(List.of("设置不能为空"));
        }
        
        // 这里可以添加更多设置验证逻辑
        // 目前Settings类没有太多需要验证的字段
        return ValidationResult.success();
    }
    
    /**
     * 验证日志配置
     */
    private ValidationResult validateLogConfig(com.aeolyn.better_experience.common.config.LogConfig logConfig) {
        if (logConfig == null) {
            return ValidationResult.failure(List.of("日志配置不能为空"));
        }
        
        // 这里可以添加更多日志配置验证逻辑
        // 目前LogConfig类没有太多需要验证的字段
        return ValidationResult.success();
    }
}
