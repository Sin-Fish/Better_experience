package com.aeolyn.better_experience.inventory.config;

import com.google.gson.annotations.SerializedName;

/**
 * 背包整理配置
 */
public class InventorySortConfig {
    
    @SerializedName("enabled")
    private boolean enabled = true;
    
    @SerializedName("sort_key")
    private String sortKey = "R";
    
    @SerializedName("smart_transfer_key")
    private String smartTransferKey = "R";
    
    @SerializedName("default_sort_mode")
    private SortMode defaultSortMode = SortMode.NAME;
    
    @SerializedName("smart_transfer_logic")
    private SmartTransferLogic smartTransferLogic = SmartTransferLogic.MOUSE_POSITION;
    
    @SerializedName("sort_settings")
    private SortSettings sortSettings = new SortSettings();
    
    public InventorySortConfig() {}
    
    // ==================== Getters and Setters ====================
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getSortKey() {
        return sortKey;
    }
    
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }
    
    public String getSmartTransferKey() {
        return smartTransferKey;
    }
    
    public void setSmartTransferKey(String smartTransferKey) {
        this.smartTransferKey = smartTransferKey;
    }
    
    public SortMode getDefaultSortMode() {
        return defaultSortMode;
    }
    
    public void setDefaultSortMode(SortMode defaultSortMode) {
        this.defaultSortMode = defaultSortMode;
    }
    
    public SmartTransferLogic getSmartTransferLogic() {
        return smartTransferLogic;
    }
    
    public void setSmartTransferLogic(SmartTransferLogic smartTransferLogic) {
        this.smartTransferLogic = smartTransferLogic;
    }
    
    public SortSettings getSortSettings() {
        return sortSettings;
    }
    
    public void setSortSettings(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 排序模式枚举
     */
    public enum SortMode {
        @SerializedName("name")
        NAME("按名称"),
        
        @SerializedName("quantity")
        QUANTITY("按数量"),
        
        @SerializedName("type")
        TYPE("按类型");
        
        private final String displayName;
        
        SortMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 智能转移逻辑枚举
     */
    public enum SmartTransferLogic {
        @SerializedName("mouse_position")
        MOUSE_POSITION("根据鼠标位置"),
        
        @SerializedName("empty_slots")
        EMPTY_SLOTS("根据空位数量"),
        
        @SerializedName("item_count")
        ITEM_COUNT("根据物品数量");
        
        private final String displayName;
        
        SmartTransferLogic(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 排序设置
     */
    public static class SortSettings {
        @SerializedName("name_ascending")
        private boolean nameAscending = true;
        
        @SerializedName("quantity_descending")
        private boolean quantityDescending = true;
        
        public boolean isNameAscending() {
            return nameAscending;
        }
        
        public void setNameAscending(boolean nameAscending) {
            this.nameAscending = nameAscending;
        }
        
        public boolean isQuantityDescending() {
            return quantityDescending;
        }
        
        public void setQuantityDescending(boolean quantityDescending) {
            this.quantityDescending = quantityDescending;
        }
    }
}
