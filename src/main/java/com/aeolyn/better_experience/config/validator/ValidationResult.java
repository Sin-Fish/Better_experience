package com.aeolyn.better_experience.config.validator;

import java.util.Collections;
import java.util.List;

/**
 * 配置验证结果
 */
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    
    private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }
    
    public static ValidationResult success(List<String> warnings) {
        return new ValidationResult(true, Collections.emptyList(), warnings);
    }
    
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, Collections.emptyList());
    }
    
    public static ValidationResult failure(List<String> errors, List<String> warnings) {
        return new ValidationResult(false, errors, warnings);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true, warnings=" + warnings.size() + "}";
        } else {
            return "ValidationResult{valid=false, errors=" + errors.size() + ", warnings=" + warnings.size() + "}";
        }
    }
}
