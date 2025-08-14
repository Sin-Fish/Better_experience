package com.example.handheld3d;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handheld3DMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it is clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "handheld3d";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Handheld3D mod 初始化完成! 灯笼3D渲染已启用!");
        
        // 注册服务器启动事件，在游戏完全加载后显示消息
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 延迟显示消息，确保游戏已完全加载
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // 等待5秒
                    if (server.getPlayerManager().getPlayerList().size() > 0) {
                        server.getPlayerManager().getPlayerList().get(0).sendMessage(
                            Text.literal("🎯 [Handheld3D] 主mod已成功加载!"), false
                        );
                    }
                } catch (Exception e) {
                    LOGGER.error("显示消息失败: " + e.getMessage());
                }
            }).start();
        });
    }
}
