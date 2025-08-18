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
    
    @SerializedName("deposit_key")
    private String depositKey = "";
    
    @SerializedName("withdraw_key")
    private String withdrawKey = "";
    
    @SerializedName("sort_container_key")
    private String sortContainerKey = "";
    
    @SerializedName("default_sort_mode")
    private SortMode defaultSortMode = SortMode.NAME;
    
    @SerializedName("auto_sort_on_open")
    private boolean autoSortOnOpen = false;
    
    @SerializedName("show_sort_buttons")
    private boolean showSortButtons = true;
    
    @SerializedName("show_container_buttons")
    private boolean showContainerButtons = true;
    
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
    
    public String getDepositKey() {
        return depositKey;
    }
    
    public void setDepositKey(String depositKey) {
        this.depositKey = depositKey;
    }
    
    public String getWithdrawKey() {
        return withdrawKey;
    }
    
    public void setWithdrawKey(String withdrawKey) {
        this.withdrawKey = withdrawKey;
    }
    
    public String getSortContainerKey() {
        return sortContainerKey;
    }
    
    public void setSortContainerKey(String sortContainerKey) {
        this.sortContainerKey = sortContainerKey;
    }
    
    public SortMode getDefaultSortMode() {
        return defaultSortMode;
    }
    
    public void setDefaultSortMode(SortMode defaultSortMode) {
        this.defaultSortMode = defaultSortMode;
    }
    
    public boolean isAutoSortOnOpen() {
        return autoSortOnOpen;
    }
    
    public void setAutoSortOnOpen(boolean autoSortOnOpen) {
        this.autoSortOnOpen = autoSortOnOpen;
    }
    
    public boolean isShowSortButtons() {
        return showSortButtons;
    }
    
    public void setShowSortButtons(boolean showSortButtons) {
        this.showSortButtons = showSortButtons;
    }
    
    public boolean isShowContainerButtons() {
        return showContainerButtons;
    }
    
    public void setShowContainerButtons(boolean showContainerButtons) {
        this.showContainerButtons = showContainerButtons;
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
        
        @SerializedName("category")
        CATEGORY("按分类"),
        
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
     * 排序设置
     */
    public static class SortSettings {
        @SerializedName("name_ascending")
        private boolean nameAscending = true;
        
        @SerializedName("quantity_descending")
        private boolean quantityDescending = true;
        
        @SerializedName("category_order")
        private String[] categoryOrder = {
            "工具", "武器", "防具", "食物", "材料", "装饰", "其他"
        };
        
        @SerializedName("type_order")
        private String[] typeOrder = {
            "方块", "物品", "工具", "武器", "防具", "食物", "药水", "其他"
        };
        
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
        
        public String[] getCategoryOrder() {
            return categoryOrder;
        }
        
        public void setCategoryOrder(String[] categoryOrder) {
            this.categoryOrder = categoryOrder;
        }
        
        public String[] getTypeOrder() {
            return typeOrder;
        }
        
        public void setTypeOrder(String[] typeOrder) {
            this.typeOrder = typeOrder;
        }
    }
}
