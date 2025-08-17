package com.aeolyn.better_experience.offhand;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.offhand.core.OffHandRestrictionController;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 副手限制模块主类
 * 负责管理副手物品限制功能
 */
public class OffHandMod implements ModInitializer {
    public static final String MOD_ID = "better_experience_offhand";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("初始化副手限制模块");
        
        try {
            // 初始化副手限制核心功能
            OffHandRestrictionController.initialize();
            
            LOGGER.info("副手限制模块初始化成功");
        } catch (Exception e) {
            LOGGER.error("副手限制模块初始化失败", e);
        }
    }
}
