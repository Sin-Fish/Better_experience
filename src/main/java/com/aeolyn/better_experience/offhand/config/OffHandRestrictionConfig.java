package com.aeolyn.better_experience.offhand.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * 副手限制配置类
 * 定义副手使用限制的配置结构
 * 使用统一的白名单，支持分离的开关控制
 */
public class OffHandRestrictionConfig {
    
    @SerializedName("enabled")
    private boolean enabled = false;
    
    // 统一白名单 - 所有允许的物品都在这里
    @SerializedName("allowed_items")
    private List<String> allowedItems = new ArrayList<>();
    
    // 分离的开关控制
    @SerializedName("disable_block_placement")
    private BlockPlacementRestriction blockPlacement;
    
    @SerializedName("disable_item_usage")
    private ItemUsageRestriction itemUsage;
    
    public OffHandRestrictionConfig() {
        // 默认配置从JSON文件中加载，这里只初始化空列表
        this.allowedItems = new ArrayList<>();
        
        // 初始化分离配置（只用于开关控制）
        this.blockPlacement = new BlockPlacementRestriction();
        this.itemUsage = new ItemUsageRestriction();
    }
    
    /**
     * 迁移旧配置到新格式
     * 如果新的分离配置为空，但旧的统一列表有数据，则进行迁移
     */
    public void migrateFromLegacyFormat() {
        // 如果分离配置都为空，但统一列表有数据，进行迁移
        if (blockPlacement.getAllowedItems().isEmpty() && 
            itemUsage.getAllowedItems().isEmpty() && 
            !allowedItems.isEmpty()) {
            
            // 将统一列表的数据复制到两个分离配置中（保持向后兼容）
            blockPlacement.setAllowedItems(new ArrayList<>(allowedItems));
            itemUsage.setAllowedItems(new ArrayList<>(allowedItems));
            
            // 设置启用状态
            blockPlacement.setEnabled(enabled);
            itemUsage.setEnabled(enabled);
        }
    }
    
    /**
     * 检查物品是否被允许在副手中使用（统一白名单检查）
     * @param itemId 物品ID
     * @return true表示允许，false表示被阻止
     */
    public boolean isItemAllowed(String itemId) {
        return allowedItems.contains(itemId);
    }
    
    /**
     * 检查物品是否被允许在副手中放置方块
     * @param itemId 物品ID
     * @return true表示允许，false表示被阻止
     */
    public boolean isBlockPlacementAllowed(String itemId) {
        if (!blockPlacement.isEnabled()) {
            return true; // 如果方块放置限制未启用，则允许所有物品
        }
        return allowedItems.contains(itemId); // 使用统一白名单
    }
    
    /**
     * 检查物品是否被允许在副手中使用
     * @param itemId 物品ID
     * @return true表示允许，false表示被阻止
     */
    public boolean isItemUsageAllowed(String itemId) {
        if (!itemUsage.isEnabled()) {
            return true; // 如果道具使用限制未启用，则允许所有物品
        }
        return allowedItems.contains(itemId); // 使用统一白名单
    }
    
    /**
     * 添加允许的物品到统一白名单
     * @param itemId 物品ID
     */
    public void addAllowedItem(String itemId) {
        if (!allowedItems.contains(itemId)) {
            allowedItems.add(itemId);
        }
    }
    
    /**
     * 移除允许的物品从统一白名单
     * @param itemId 物品ID
     */
    public void removeAllowedItem(String itemId) {
        allowedItems.remove(itemId);
    }
    
    /**
     * 获取所有允许的物品列表（统一白名单）
     * @return 允许的物品列表
     */
    public List<String> getAllowedItems() {
        return new ArrayList<>(allowedItems);
    }
    
    /**
     * 设置允许的物品列表（统一白名单）
     * @param allowedItems 新的物品列表
     */
    public void setAllowedItems(List<String> allowedItems) {
        this.allowedItems = new ArrayList<>(allowedItems);
    }
    
    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
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
     * 方块放置限制配置（只用于开关控制）
     */
    public static class BlockPlacementRestriction {
        @SerializedName("enabled")
        private boolean enabled = false;
        
        // 向后兼容字段，但不实际使用
        @SerializedName("allowed_items")
        private List<String> allowedItems = new ArrayList<>();
        
        public BlockPlacementRestriction() {
            // 默认允许一些常用方块
            allowedItems.add("minecraft:torch");
            allowedItems.add("minecraft:soul_torch");
            allowedItems.add("minecraft:lantern");
            allowedItems.add("minecraft:soul_lantern");
            allowedItems.add("minecraft:campfire");
            allowedItems.add("minecraft:oak_sign");
            allowedItems.add("minecraft:lever");
            allowedItems.add("minecraft:repeater");
            allowedItems.add("minecraft:comparator");
            allowedItems.add("minecraft:redstone_torch");
            allowedItems.add("minecraft:chain");
            allowedItems.add("minecraft:flower_pot");
            allowedItems.add("minecraft:item_frame");
        }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        // 向后兼容方法，但不实际使用
        public List<String> getAllowedItems() { return new ArrayList<>(allowedItems); }
        public void setAllowedItems(List<String> allowedItems) { this.allowedItems = new ArrayList<>(allowedItems); }
        
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
     * 道具使用限制配置（只用于开关控制）
     */
    public static class ItemUsageRestriction {
        @SerializedName("enabled")
        private boolean enabled = false;
        
        // 向后兼容字段，但不实际使用
        @SerializedName("allowed_items")
        private List<String> allowedItems = new ArrayList<>();
        
        public ItemUsageRestriction() {
            // 默认允许一些常用道具
            allowedItems.add("minecraft:shield");
            allowedItems.add("minecraft:totem_of_undying");
            allowedItems.add("minecraft:arrow");
            allowedItems.add("minecraft:spectral_arrow");
            allowedItems.add("minecraft:tipped_arrow");
            allowedItems.add("minecraft:firework_rocket");
            allowedItems.add("minecraft:wind_charge");
            allowedItems.add("minecraft:bamboo");
            allowedItems.add("minecraft:minecart");
        }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        // 向后兼容方法，但不实际使用
        public List<String> getAllowedItems() { return new ArrayList<>(allowedItems); }
        public void setAllowedItems(List<String> allowedItems) { this.allowedItems = new ArrayList<>(allowedItems); }
        
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
