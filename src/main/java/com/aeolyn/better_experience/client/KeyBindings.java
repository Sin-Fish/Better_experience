package com.aeolyn.better_experience.client;

import com.aeolyn.better_experience.client.gui.ModConfigScreen;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-KeyBindings");
    private static KeyBinding openConfigKey;
    private static KeyBinding sortInventoryKey;
    private static KeyBinding smartTransferKey; // Shift+R 智能转移
    private static KeyBinding depositToContainerKey;
    private static KeyBinding withdrawFromContainerKey;
    private static KeyBinding sortContainerKey;
    
    public static void register() {
        LOGGER.info("开始注册按键绑定");
        LogUtil.info("KeyBindings", "开始注册按键绑定");
        
        // 注册配置界面按键绑定
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.open_config", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_B, // 默认按键 (B键)
            "category.better_experience.general" // 分类
        ));
        
        // 注册一键整理（R）按键绑定
        sortInventoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.sort_inventory",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.better_experience.inventory"
        ));
        LOGGER.info("R键（一键整理）绑定注册完成");
        LogUtil.info("KeyBindings", "R键（一键整理）绑定注册完成");
        
        // 注册智能转移（Shift+R）的基础按键绑定（基础键为R，是否按下Shift在mixin中判定）
        smartTransferKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.smart_transfer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.better_experience.inventory"
        ));
        LOGGER.info("Shift+R（智能转移）绑定注册完成（基础键为R）");
        LogUtil.info("KeyBindings", "Shift+R（智能转移）绑定注册完成（基础键为R）");
        
        depositToContainerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.deposit_to_container", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_UNKNOWN, // 默认不设置
            "category.better_experience.inventory" // 分类
        ));
        
        withdrawFromContainerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.withdraw_from_container", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_UNKNOWN, // 默认不设置
            "category.better_experience.inventory" // 分类
        ));
        
        sortContainerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.sort_container", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_UNKNOWN, // 默认不设置
            "category.better_experience.inventory" // 分类
        ));
        
        LOGGER.info("所有按键绑定注册完成");
        LogUtil.info("KeyBindings", "所有按键绑定注册完成");
    }
    
    public static void tick() {
        // 检查配置界面按键是否被按下
        if (openConfigKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到B键按下，打开配置界面");
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new com.aeolyn.better_experience.client.gui.ModConfigScreen(client.currentScreen, ConfigManager.getInstance()));
            }
        }
        
        // R键和Shift+R功能现在完全由 mixin 处理，不需要在这里检查
        // 在非界面状态下，这些功能没有意义，因为需要相应的界面
        
        // 智能转移功能现在完全由 mixin 处理，不需要在这里检查
        // 在非界面状态下，Shift+R 没有意义，因为需要容器界面
        
        // 检查其他按键
        if (depositToContainerKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到存入容器按键按下");
        }
        
        if (withdrawFromContainerKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到从容器拿取按键按下");
        }
        
        if (sortContainerKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到整理容器按键按下");
        }
    }
    
    public static KeyBinding getSortInventoryKey() {
        return sortInventoryKey;
    }
    
    public static KeyBinding getSmartTransferKey() {
        return smartTransferKey;
    }
}
