package com.aeolyn.better_experience.core;

import com.aeolyn.better_experience.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.config.manager.ConfigManager;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 副手限制控制器
 * 负责检查副手物品使用是否被允许
 */
public class OffHandRestrictionController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-OffHand");
    private static volatile OffHandRestrictionController instance;
    
    private OffHandRestrictionController() {}
    
    /**
     * 获取单例实例
     */
    public static OffHandRestrictionController getInstance() {
        if (instance == null) {
            synchronized (OffHandRestrictionController.class) {
                if (instance == null) {
                    instance = new OffHandRestrictionController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 检查副手方块放置是否被允许
     * @param item 要放置的物品
     * @return true表示允许，false表示被阻止
     */
    public boolean isBlockPlacementAllowed(Item item) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config == null || !config.getBlockPlacement().isEnabled()) {
                return true; // 未启用限制，允许使用
            }
            
            String itemId = Registries.ITEM.getId(item).toString();
            boolean isAllowed = config.getBlockPlacement().isItemAllowed(itemId);
            
            if (!isAllowed) {
                LOGGER.debug("副手方块放置被阻止: {}", itemId);
            }
            
            return isAllowed;
        } catch (Exception e) {
            LOGGER.error("检查副手方块放置权限时发生错误: " + e.getMessage(), e);
            return true; // 发生错误时默认允许
        }
    }
    
    /**
     * 检查副手道具使用是否被允许
     * @param item 要使用的物品
     * @return true表示允许，false表示被阻止
     */
    public boolean isItemUsageAllowed(Item item) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config == null || !config.getItemUsage().isEnabled()) {
                return true; // 未启用限制，允许使用
            }
            
            String itemId = Registries.ITEM.getId(item).toString();
            boolean isAllowed = config.getItemUsage().isItemAllowed(itemId);
            
            if (!isAllowed) {
                LOGGER.debug("副手道具使用被阻止: {}", itemId);
            }
            
            return isAllowed;
        } catch (Exception e) {
            LOGGER.error("检查副手道具使用权限时发生错误: " + e.getMessage(), e);
            return true; // 发生错误时默认允许
        }
    }
    
    /**
     * 添加允许的方块放置物品
     * @param itemId 物品ID
     */
    public void addAllowedBlockPlacementItem(String itemId) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config != null) {
                config.getBlockPlacement().addAllowedItem(itemId);
                configManager.saveOffHandRestrictionConfig();
                LOGGER.info("已添加允许的副手方块放置物品: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("添加允许的副手方块放置物品时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 添加允许的道具使用物品
     * @param itemId 物品ID
     */
    public void addAllowedItemUsageItem(String itemId) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config != null) {
                config.getItemUsage().addAllowedItem(itemId);
                configManager.saveOffHandRestrictionConfig();
                LOGGER.info("已添加允许的副手道具使用物品: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("添加允许的副手道具使用物品时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 移除允许的方块放置物品
     * @param itemId 物品ID
     */
    public void removeAllowedBlockPlacementItem(String itemId) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config != null) {
                config.getBlockPlacement().removeAllowedItem(itemId);
                configManager.saveOffHandRestrictionConfig();
                LOGGER.info("已移除允许的副手方块放置物品: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("移除允许的副手方块放置物品时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 移除允许的道具使用物品
     * @param itemId 物品ID
     */
    public void removeAllowedItemUsageItem(String itemId) {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config != null) {
                config.getItemUsage().removeAllowedItem(itemId);
                configManager.saveOffHandRestrictionConfig();
                LOGGER.info("已移除允许的副手道具使用物品: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("移除允许的副手道具使用物品时发生错误: " + e.getMessage(), e);
        }
    }
}
