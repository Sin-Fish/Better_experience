package com.aeolyn.better_experience.client;

import com.aeolyn.better_experience.client.gui.ModConfigScreen;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding openConfigKey;
    
    public static void register() {
        // 注册按键绑定
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.open_config", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_B, // 默认按键 (B键)
            "category.better_experience.general" // 分类
        ));
    }
    
    public static void tick() {
        // 检查按键是否被按下
        if (openConfigKey.wasPressed()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new ModConfigScreen(null, ConfigManager.getInstance()));
            }
        }
    }
}
