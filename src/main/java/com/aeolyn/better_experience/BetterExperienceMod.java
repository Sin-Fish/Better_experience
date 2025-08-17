package com.aeolyn.better_experience;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterExperienceMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it is clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "better_experience";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // 初始化配置管理器
        ConfigManager.initialize();
        
        // 初始化各个模块
        try {
            // 初始化3D渲染模块
            com.aeolyn.better_experience.render3d.core.ItemRenderer3D.initialize();
            
            // 初始化副手限制模块
            com.aeolyn.better_experience.offhand.core.OffHandRestrictionController.initialize();
            
            LOGGER.info("Better Experience mod 初始化完成! 通用3D渲染系统和副手限制系统已启用!");
        } catch (Exception e) {
            LOGGER.error("模块初始化失败", e);
        }
        
        // 注册服务器启动事件，在游戏完全加载后显示消息
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 使用延迟任务避免阻塞主线程
            server.execute(() -> {
                try {
                    // 等待一段时间确保游戏完全加载
                    Thread.sleep(3000);
                    
                    // 向所有在线玩家发送消息
                    var playerList = server.getPlayerManager().getPlayerList();
                    if (playerList != null && !playerList.isEmpty()) {
                        playerList.get(0).sendMessage(
                            Text.literal("[Better Experience] 通用3D渲染和副手限制mod已成功加载!"), false
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
    }
}
