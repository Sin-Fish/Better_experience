package com.aeolyn.better_experience.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ItemsConfig {
    @SerializedName("enabled_items")
    private List<String> enabledItems;
    
    @SerializedName("settings")
    private Settings settings;
    
    public static class Settings {
        @SerializedName("enable_debug_logs")
        private boolean enableDebugLogs = true;
        
        @SerializedName("default_scale")
        private float defaultScale = 1.0f;
        
        @SerializedName("default_rotation_x")
        private float defaultRotationX = 0.0f;
        
        @SerializedName("default_rotation_y")
        private float defaultRotationY = 0.0f;
        
        @SerializedName("default_rotation_z")
        private float defaultRotationZ = 0.0f;
        
        @SerializedName("default_translate_x")
        private float defaultTranslateX = 0.0f;
        
        @SerializedName("default_translate_y")
        private float defaultTranslateY = 0.0f;
        
        @SerializedName("default_translate_z")
        private float defaultTranslateZ = 0.0f;
        
        // Getters
        public boolean isEnableDebugLogs() { return enableDebugLogs; }
        public float getDefaultScale() { return defaultScale; }
        public float getDefaultRotationX() { return defaultRotationX; }
        public float getDefaultRotationY() { return defaultRotationY; }
        public float getDefaultRotationZ() { return defaultRotationZ; }
        public float getDefaultTranslateX() { return defaultTranslateX; }
        public float getDefaultTranslateY() { return defaultTranslateY; }
        public float getDefaultTranslateZ() { return defaultTranslateZ; }
    }
    
    // Getters
    public List<String> getEnabledItems() { return enabledItems; }
    public Settings getSettings() { return settings; }
}
