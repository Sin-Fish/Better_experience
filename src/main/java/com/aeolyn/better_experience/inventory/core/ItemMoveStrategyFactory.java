package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * 物品移动策略工厂
 * 根据游戏模式选择合适的物品移动策略
 */
public class ItemMoveStrategyFactory {
    
    /**
     * 根据玩家游戏模式创建合适的物品移动策略
     * @param player 玩家实体
     * @return 物品移动策略
     */
    public static ItemMoveStrategy createStrategy(ClientPlayerEntity player) {
        if (player == null) {
            LogUtil.warn("Inventory", "玩家为空，使用默认生存模式策略");
            return new SurvivalItemMoveStrategy();
        }
        
        // 检查是否为创造模式
        if (player.getAbilities().creativeMode) {
            LogUtil.info("Inventory", "检测到创造模式，使用创造模式物品移动策略");
            return new CreativeItemMoveStrategy();
        } else {
            LogUtil.info("Inventory", "检测到生存模式，使用生存模式物品移动策略");
            return new SurvivalItemMoveStrategy();
        }
    }
    
    /**
     * 强制使用生存模式策略
     * @return 生存模式物品移动策略
     */
    public static ItemMoveStrategy createSurvivalStrategy() {
        return new SurvivalItemMoveStrategy();
    }
    
    /**
     * 强制使用创造模式策略
     * @return 创造模式物品移动策略
     */
    public static ItemMoveStrategy createCreativeStrategy() {
        return new CreativeItemMoveStrategy();
    }
}
