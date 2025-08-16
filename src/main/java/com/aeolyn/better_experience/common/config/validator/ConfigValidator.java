package com.aeolyn.better_experience.common.config.validator;

import com.aeolyn.better_experience.common.config.ItemsConfig;
import com.aeolyn.better_experience.common.config.ItemConfig;

/**
 * 配置验证器接�?
 */
public interface ConfigValidator {
    
    /**
     * 验证主配�?
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
