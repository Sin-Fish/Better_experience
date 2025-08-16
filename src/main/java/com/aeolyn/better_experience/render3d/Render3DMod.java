package com.aeolyn.better_experience.render3d;

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
        // TODO: 初始化3D渲染相关功能
    }
}
