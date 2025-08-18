package com.aeolyn.better_experience;

import com.aeolyn.better_experience.client.KeyBindings;
import com.aeolyn.better_experience.client.command.ConfigCommand;
import com.aeolyn.better_experience.client.command.InventorySortCommand;
import com.aeolyn.better_experience.common.util.LogUtil;
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
        LOGGER.info("开始注册按键绑定");
        LogUtil.info("Client", "开始注册按键绑定");
        KeyBindings.register();
        LOGGER.info("按键绑定注册完成");
        LogUtil.info("Client", "按键绑定注册完成");
        
        // 注册客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ConfigCommand.register(dispatcher, registryAccess);
            InventorySortCommand.register(dispatcher);
        });
        
        // 注册客户端tick事件
        LOGGER.info("注册客户端tick事件");
        LogUtil.info("Client", "注册客户端tick事件");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeyBindings.tick();
        });
        LOGGER.info("客户端tick事件注册完成");
        LogUtil.info("Client", "客户端tick事件注册完成");
    }
}
