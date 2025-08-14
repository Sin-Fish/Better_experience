package com.aeolyn.better_experience;

import com.aeolyn.better_experience.client.KeyBindings;
import com.aeolyn.better_experience.client.command.ConfigCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterExperienceClientMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Better Experience 客户端初始化完成!");
        
        // 注册按键绑定
        KeyBindings.register();
        
        // 注册客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ConfigCommand.register(dispatcher, registryAccess);
        });
        
        // 注册客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeyBindings.tick();
        });
    }
}
