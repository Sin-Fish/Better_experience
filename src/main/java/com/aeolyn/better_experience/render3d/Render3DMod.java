package com.aeolyn.better_experience.render3d;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.render3d.core.ItemRenderer3D;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 3D渲染模块主类
 * 负责管理物品3D渲染功能
 */
public class Render3DMod implements ModInitializer {
    public static final String MOD_ID = "better_experience_render3d";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("初始化3D渲染模块");
        
        try {
            // 初始化3D渲染核心功能
            ItemRenderer3D.initialize();
            
            LOGGER.info("3D渲染模块初始化成功");
        } catch (Exception e) {
            LOGGER.error("3D渲染模块初始化失败", e);
        }
    }
}
