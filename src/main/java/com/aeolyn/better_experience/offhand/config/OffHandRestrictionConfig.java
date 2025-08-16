package com.aeolyn.better_experience.offhand.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * 副手限制配置类
 * 定义副手使用限制的配置结构
 */
public class OffHandRestrictionConfig {
    
    @SerializedName("enabled")
    private boolean enabled = false;
    
    @SerializedName("allowed_items")
    private List<String> allowedItems = new ArrayList<>();
    
    // 向后兼容字段 - 保留但不使用
    @SerializedName("disable_block_placement")
    private BlockPlacementRestriction blockPlacement;
    
    @SerializedName("disable_item_usage")
    private ItemUsageRestriction itemUsage;
    
    public OffHandRestrictionConfig() {
        // 默认配置从JSON文件中加载，这里只初始化空列表
        this.allowedItems = new ArrayList<>();
        
        // 初始化向后兼容字段
        this.blockPlacement = new BlockPlacementRestriction();
        this.itemUsage = new ItemUsageRestriction();
    }
    
    /**
     * 迁移旧配置到新格式
     * 如果新的统一列表为空，但旧的分离列表有数据，则进行迁移
     */
    public void migrateFromLegacyFormat() {
        // 如果新的统一列表为空，但旧的分离列表有数据，进行迁移
        if (allowedItems.isEmpty() && (blockPlacement != null || itemUsage != null)) {
            // 合并两个旧列表的数据
            if (blockPlacement != null && blockPlacement.getAllowedItems() != null) {
                for (String itemId : blockPlacement.getAllowedItems()) {
                    if (!allowedItems.contains(itemId)) {
                        allowedItems.add(itemId);
                    }
                }
            }
            
            if (itemUsage != null && itemUsage.getAllowedItems() != null) {
                for (String itemId : itemUsage.getAllowedItems()) {
                    if (!allowedItems.contains(itemId)) {
                        allowedItems.add(itemId);
                    }
                }
            }
            
            // 设置启用状态（如果任一旧配置启用，则新配置也启用）
            if (blockPlacement != null && blockPlacement.isEnabled()) {
                this.enabled = true;
            }
            if (itemUsage != null && itemUsage.isEnabled()) {
                this.enabled = true;
            }
        }
    }
    
    /**
     * 检查物品是否被允许在副手中使用
     * @param itemId 物品ID
     * @return true表示允许，false表示被阻止
     */
    public boolean isItemAllowed(String itemId) {
        return allowedItems.contains(itemId);
    }
    
    /**
     * 添加允许的物品
     * @param itemId 物品ID
     */
    public void addAllowedItem(String itemId) {
        if (!allowedItems.contains(itemId)) {
            allowedItems.add(itemId);
        }
    }
    
    /**
     * 移除允许的物品
     * @param itemId 物品ID
     */
    public void removeAllowedItem(String itemId) {
        allowedItems.remove(itemId);
    }
    
    /**
     * 获取所有允许的物品列表
     * @return 允许的物品列表
     */
    public List<String> getAllowedItems() {
        return new ArrayList<>(allowedItems);
    }
    
    /**
     * 设置允许的物品列表
     * @param allowedItems 新的物品列表
     */
    public void setAllowedItems(List<String> allowedItems) {
        this.allowedItems = new ArrayList<>(allowedItems);
    }
    
    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    // 向后兼容方法 - 保持API兼容性
    public BlockPlacementRestriction getBlockPlacement() { 
        if (blockPlacement == null) {
            blockPlacement = new BlockPlacementRestriction();
        }
        return blockPlacement; 
    }
    
    public ItemUsageRestriction getItemUsage() { 
        if (itemUsage == null) {
            itemUsage = new ItemUsageRestriction();
        }
        return itemUsage; 
    }
    
    public void setBlockPlacement(BlockPlacementRestriction blockPlacement) { this.blockPlacement = blockPlacement; }
    public void setItemUsage(ItemUsageRestriction itemUsage) { this.itemUsage = itemUsage; }
    
    /**
     * 方块放置限制配置（向后兼容）
     * @deprecated 使用统一的白名单配置
     */
    @Deprecated
    public static class BlockPlacementRestriction {
        @SerializedName("enabled")
        private boolean enabled = false;
        
        @SerializedName("allowed_items")
        private List<String> allowedItems = new ArrayList<>();
        
        public BlockPlacementRestriction() {
            // 默认允许一些常用物品
            allowedItems.add("minecraft:torch");
            allowedItems.add("minecraft:soul_torch");
            allowedItems.add("minecraft:lantern");
            allowedItems.add("minecraft:soul_lantern");
        }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public List<String> getAllowedItems() { return allowedItems; }
        public void setAllowedItems(List<String> allowedItems) { this.allowedItems = allowedItems; }
        
        public void addAllowedItem(String itemId) {
            if (!allowedItems.contains(itemId)) {
                allowedItems.add(itemId);
            }
        }
        
        public void removeAllowedItem(String itemId) {
            allowedItems.remove(itemId);
        }
        
        public boolean isItemAllowed(String itemId) {
            return allowedItems.contains(itemId);
        }
    }
    
    /**
     * 道具使用限制配置（向后兼容）
     * @deprecated 使用统一的白名单配置
     */
    @Deprecated
    public static class ItemUsageRestriction {
        @SerializedName("enabled")
        private boolean enabled = false;
        
        @SerializedName("allowed_items")
        private List<String> allowedItems = new ArrayList<>();
        
        public ItemUsageRestriction() {
            // 默认允许一些常用物品
            allowedItems.add("minecraft:shield");
            allowedItems.add("minecraft:totem_of_undying");
            allowedItems.add("minecraft:torch");
            allowedItems.add("minecraft:soul_torch");
        }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public List<String> getAllowedItems() { return allowedItems; }
        public void setAllowedItems(List<String> allowedItems) { this.allowedItems = allowedItems; }
        
        public void addAllowedItem(String itemId) {
            if (!allowedItems.contains(itemId)) {
                allowedItems.add(itemId);
            }
        }
        
        public void removeAllowedItem(String itemId) {
            allowedItems.remove(itemId);
        }
        
        public boolean isItemAllowed(String itemId) {
            return allowedItems.contains(itemId);
        }
    }
}
