package com.aeolyn.better_experience.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * 副手限制配置类
 * 定义副手使用限制的配置结构
 */
public class OffHandRestrictionConfig {
    
    @SerializedName("disable_block_placement")
    private BlockPlacementRestriction blockPlacement;
    
    @SerializedName("disable_item_usage")
    private ItemUsageRestriction itemUsage;
    
    public OffHandRestrictionConfig() {
        // 默认配置
        this.blockPlacement = new BlockPlacementRestriction();
        this.itemUsage = new ItemUsageRestriction();
    }
    
    /**
     * 方块放置限制配置
     */
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
     * 道具使用限制配置
     */
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
    
    // Getters
    public BlockPlacementRestriction getBlockPlacement() { return blockPlacement; }
    public ItemUsageRestriction getItemUsage() { return itemUsage; }
    
    // Setters
    public void setBlockPlacement(BlockPlacementRestriction blockPlacement) { this.blockPlacement = blockPlacement; }
    public void setItemUsage(ItemUsageRestriction itemUsage) { this.itemUsage = itemUsage; }
}
