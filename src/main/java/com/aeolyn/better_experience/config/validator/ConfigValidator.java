package com.aeolyn.better_experience.config.validator;

import com.aeolyn.better_experience.config.ItemsConfig;
import com.aeolyn.better_experience.config.ItemConfig;

/**
 * 配置验证器接口
 */
public interface ConfigValidator {
    
    /**
     * 验证主配置
     */
    ValidationResult validate(ItemsConfig config);
    
    /**
     * 验证物品配置
     */
    ValidationResult validate(ItemConfig config);
    
    /**
     * 验证物品ID格式
     */
    ValidationResult validateItemId(String itemId);
    
    /**
     * 验证渲染设置
     */
    ValidationResult validateRenderSettings(ItemConfig.RenderSettings settings);
}
