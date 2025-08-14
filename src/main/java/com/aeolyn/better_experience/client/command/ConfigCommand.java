package com.aeolyn.better_experience.client.command;

import com.aeolyn.better_experience.client.gui.ModConfigScreen;
import com.aeolyn.better_experience.config.ConfigManager;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class ConfigCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        dispatcher.register(ClientCommandManager.literal("betterexperience")
            .then(ClientCommandManager.literal("config")
                .executes(context -> {
                    // 在客户端执行
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.execute(() -> {
                            client.setScreen(new ModConfigScreen(ConfigManager.getInstance()));
                        });
                    }
                    context.getSource().sendFeedback(Text.literal("打开Better Experience配置界面"));
                    return 1;
                })
            )
        );
    }
}
