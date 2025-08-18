package com.aeolyn.better_experience.common.config.validator.impl;

import com.aeolyn.better_experience.common.config.validator.BaseConfigValidator;
import com.aeolyn.better_experience.common.config.validator.ValidationResult;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;

import java.util.List;

/**
 * 副手配置验证器实现
 * 继承基础验证器，使用统一的验证逻辑
 */
public class OffHandConfigValidator extends BaseConfigValidator<OffHandRestrictionConfig> {
    
    @Override
    public ValidationResult validate(OffHandRestrictionConfig config) {
        if (config == null) {
            return ValidationResult.failure(List.of("副手配置不能为空"));
        }
        
        // 验证允许的物品列表
        if (config.getAllowedItems() != null) {
            return validateIdList(config.getAllowedItems(), "允许物品列表", "物品ID");
        }
        
        return ValidationResult.success();
    }
}
