package com.aeolyn.better_experience.util;

import com.aeolyn.better_experience.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.config.manager.ConfigManager;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 副手限制工具类
 * 提供副手限制相关的辅助方法
 */
public class OffHandRestrictionUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-OffHandUtil");
    
    /**
     * 检查物品是否为方块
     * @param item 要检查的物品
     * @return true表示是方块，false表示不是
     */
    public static boolean isBlockItem(Item item) {
        try {
            // 通过物品ID判断是否为方块
            String itemId = Registries.ITEM.getId(item).toString();
            return itemId.contains("block") || 
                   itemId.contains("stone") || 
                   itemId.contains("dirt") || 
                   itemId.contains("wood") || 
                   itemId.contains("log") || 
                   itemId.contains("planks") ||
                   itemId.contains("torch") ||
                   itemId.contains("lantern") ||
                   itemId.contains("sign") ||
                   itemId.contains("fence") ||
                   itemId.contains("wall") ||
                   itemId.contains("stairs") ||
                   itemId.contains("slab") ||
                   itemId.contains("door") ||
                   itemId.contains("trapdoor") ||
                   itemId.contains("gate") ||
                   itemId.contains("button") ||
                   itemId.contains("pressure_plate") ||
                   itemId.contains("lever") ||
                   itemId.contains("repeater") ||
                   itemId.contains("comparator") ||
                   itemId.contains("redstone_wire") ||
                   itemId.contains("redstone_torch") ||
                   itemId.contains("redstone_block") ||
                   itemId.contains("target") ||
                   itemId.contains("observer") ||
                   itemId.contains("dispenser") ||
                   itemId.contains("dropper") ||
                   itemId.contains("hopper") ||
                   itemId.contains("chest") ||
                   itemId.contains("trapped_chest") ||
                   itemId.contains("furnace") ||
                   itemId.contains("blast_furnace") ||
                   itemId.contains("smoker") ||
                   itemId.contains("campfire") ||
                   itemId.contains("soul_campfire") ||
                   itemId.contains("anvil") ||
                   itemId.contains("enchanting_table") ||
                   itemId.contains("crafting_table") ||
                   itemId.contains("loom") ||
                   itemId.contains("cartography_table") ||
                   itemId.contains("fletching_table") ||
                   itemId.contains("smithing_table") ||
                   itemId.contains("grindstone") ||
                   itemId.contains("stonecutter") ||
                   itemId.contains("brewing_stand") ||
                   itemId.contains("cauldron") ||
                   itemId.contains("beehive") ||
                   itemId.contains("bee_nest") ||
                   itemId.contains("jukebox") ||
                   itemId.contains("note_block") ||
                   itemId.contains("jukebox") ||
                   itemId.contains("jukebox") ||
                   itemId.contains("jukebox");
        } catch (Exception e) {
            LOGGER.error("检查方块物品失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查物品是否为工具
     * @param item 要检查的物品
     * @return true表示是工具，false表示不是
     */
    public static boolean isToolItem(Item item) {
        try {
            String itemId = Registries.ITEM.getId(item).toString();
            return itemId.contains("sword") || 
                   itemId.contains("axe") || 
                   itemId.contains("pickaxe") || 
                   itemId.contains("shovel") || 
                   itemId.contains("hoe") ||
                   itemId.contains("shears") ||
                   itemId.contains("fishing_rod") ||
                   itemId.contains("bow") ||
                   itemId.contains("crossbow") ||
                   itemId.contains("trident") ||
                   itemId.contains("shield") ||
                   itemId.contains("totem") ||
                   itemId.contains("bucket") ||
                   itemId.contains("water_bucket") ||
                   itemId.contains("lava_bucket") ||
                   itemId.contains("milk_bucket") ||
                   itemId.contains("powder_snow_bucket") ||
                   itemId.contains("axolotl_bucket") ||
                   itemId.contains("cod_bucket") ||
                   itemId.contains("pufferfish_bucket") ||
                   itemId.contains("salmon_bucket") ||
                   itemId.contains("tropical_fish_bucket") ||
                   itemId.contains("tadpole_bucket") ||
                   itemId.contains("flint_and_steel") ||
                   itemId.contains("fire_charge") ||
                   itemId.contains("firework_rocket") ||
                   itemId.contains("firework_star") ||
                   itemId.contains("spyglass") ||
                   itemId.contains("compass") ||
                   itemId.contains("clock") ||
                   itemId.contains("name_tag") ||
                   itemId.contains("lead") ||
                   itemId.contains("carrot_on_a_stick") ||
                   itemId.contains("warped_fungus_on_a_stick") ||
                   itemId.contains("brush") ||
                   itemId.contains("goat_horn") ||
                   itemId.contains("music_disc") ||
                   itemId.contains("disc_fragment");
        } catch (Exception e) {
            LOGGER.error("检查工具物品失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取物品的友好名称
     * @param item 物品
     * @return 物品的友好名称
     */
    public static String getItemFriendlyName(Item item) {
        try {
            String itemId = Registries.ITEM.getId(item).toString();
            String[] parts = itemId.split(":");
            if (parts.length == 2) {
                String name = parts[1];
                // 将下划线替换为空格，并首字母大写
                name = name.replace("_", " ");
                if (name.length() > 0) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }
                return name;
            }
            return itemId;
        } catch (Exception e) {
            LOGGER.error("获取物品友好名称失败: " + e.getMessage(), e);
            return "Unknown Item";
        }
    }
    
    /**
     * 检查副手限制配置是否启用
     * @return true表示启用，false表示未启用
     */
    public static boolean isOffHandRestrictionEnabled() {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            return config != null && 
                   (config.getBlockPlacement().isEnabled() || 
                    config.getItemUsage().isEnabled());
        } catch (Exception e) {
            LOGGER.error("检查副手限制配置状态失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取副手限制配置的摘要信息
     * @return 配置摘要字符串
     */
    public static String getOffHandRestrictionSummary() {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
            
            if (config == null) {
                return "副手限制配置未加载";
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append("副手限制配置:\n");
            
            // 方块放置限制
            OffHandRestrictionConfig.BlockPlacementRestriction blockPlacement = config.getBlockPlacement();
            summary.append("- 方块放置限制: ").append(blockPlacement.isEnabled() ? "启用" : "禁用");
            if (blockPlacement.isEnabled()) {
                summary.append(" (允许 ").append(blockPlacement.getAllowedItems().size()).append(" 个物品)");
            }
            summary.append("\n");
            
            // 道具使用限制
            OffHandRestrictionConfig.ItemUsageRestriction itemUsage = config.getItemUsage();
            summary.append("- 道具使用限制: ").append(itemUsage.isEnabled() ? "启用" : "禁用");
            if (itemUsage.isEnabled()) {
                summary.append(" (允许 ").append(itemUsage.getAllowedItems().size()).append(" 个物品)");
            }
            
            return summary.toString();
        } catch (Exception e) {
            LOGGER.error("获取副手限制配置摘要失败: " + e.getMessage(), e);
            return "获取配置摘要失败";
        }
    }
}
