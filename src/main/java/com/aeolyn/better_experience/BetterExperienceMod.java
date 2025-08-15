package com.aeolyn.better_experience;

import com.aeolyn.better_experience.config.manager.ConfigManager;
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
        
        // 测试配置持久化功能
        ConfigManager.getInstance().testConfigPersistence();
        
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Better Experience mod 初始化完成! 通用3D渲染系统和副手限制系统已启用!");
        
        // 注册服务器启动事件，在游戏完全加载后显示消息
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 使用延迟任务避免阻塞主线程
            server.execute(() -> {
                try {
                    // 等待一段时间确保游戏完全加载
                    Thread.sleep(3000);
                    
                    // 向所有在线玩家发送消息
                    if (server.getPlayerManager().getPlayerList().size() > 0) {
                        server.getPlayerManager().getPlayerList().get(0).sendMessage(
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
