package com.aeolyn.better_experience;

import com.aeolyn.better_experience.common.config.DebugConfig;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.common.util.VersionCompatibilityUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
     * 
     * func: Better Experience mod主入口
     */
public class BetterExperienceMod implements ModInitializer {
    
    public static final String MOD_ID = "better_experience";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        VersionCompatibilityUtil.logVersionInfo();
        
        if (!VersionCompatibilityUtil.isVersionCompatible()) {
            LOGGER.error("❌ 当前Minecraft版本不兼容！Better Experience mod可能无法正常工作。");
            LOGGER.error("请使用Minecraft 1.21.0-1.21.10版本以获得最佳体验。");
        } else {
            LOGGER.info("✅ 版本兼容性检查通过！");
        }
        
        ConfigManager.initialize();
        
        DebugConfig.getInstance();
        
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            
            if (configManager.isRender3dEnabled()) {
                com.aeolyn.better_experience.render3d.core.ItemRenderer3D.initialize();
                LogUtil.info("General", "3D渲染模块已启用并初始化完成");
            } else {
                LogUtil.info("General", "3D渲染模块已禁用，跳过初始化");
            }
            
            if (configManager.isOffhandRestrictionEnabled()) {
                com.aeolyn.better_experience.offhand.core.OffHandRestrictionController.initialize();
                LogUtil.info("General", "副手限制模块已启用并初始化完成");
            } else {
                LogUtil.info("General", "副手限制模块已禁用，跳过初始化");
            }
            
            if (configManager.isInventorySortEnabled()) {
                com.aeolyn.better_experience.inventory.core.InventorySortController.initialize();
                com.aeolyn.better_experience.inventory.core.InventoryTransferController.initialize();
                LogUtil.info("General", "背包整理和智能转移模块已启用并初始化完成");
            } else {
                LogUtil.info("General", "背包整理模块已禁用，跳过初始化");
            }
            
            LogUtil.info("General", "Better Experience mod 初始化完成! 根据通用配置启用了相应模块!");
        } catch (Exception e) {
            LOGGER.error("模块初始化失败", e);
        }
        //向所有启用该mod的玩家发送消息
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.execute(() -> {
                try {
                    Thread.sleep(3000);
                    
                    var playerList = server.getPlayerManager().getPlayerList();
                    if (playerList != null && !playerList.isEmpty()) {
                        String versionInfo = VersionCompatibilityUtil.getCompatibilityInfo();
                        ConfigManager configManager = ConfigManager.getInstance();
                        
                        StringBuilder enabledModules = new StringBuilder();
                        if (configManager.isRender3dEnabled()) enabledModules.append("3D渲染 ");
                        if (configManager.isOffhandRestrictionEnabled()) enabledModules.append("副手限制 ");
                        if (configManager.isInventorySortEnabled()) enabledModules.append("背包整理 ");
                        
                        String moduleInfo = enabledModules.length() > 0 ? 
                            "已启用模块: " + enabledModules.toString().trim() : "所有模块已禁用";
                        for (var player : playerList) {
                            player.sendMessage(
                                  Text.literal("[Better Experience] " + moduleInfo + " " + versionInfo), false
                            );
                            }          
                   }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
    }
}
