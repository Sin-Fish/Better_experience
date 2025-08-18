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
        
        // 注册背包整理按键绑定
        sortInventoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.better_experience.sort_inventory", // 翻译键
            InputUtil.Type.KEYSYM, // 按键类型
            GLFW.GLFW_KEY_R, // 默认按键 (R键)
            "category.better_experience.inventory" // 分类
        ));
        LOGGER.info("R键绑定注册完成");
        LogUtil.info("KeyBindings", "R键绑定注册完成");
        
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
        
        // 检查背包整理按键是否被按下
        if (sortInventoryKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到R键按下，开始执行背包整理");
            
            // 检查当前是否在物品栏界面
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                String screenName = client.currentScreen != null ? client.currentScreen.getClass().getSimpleName() : "null";
                LogUtil.info("KeyBindings", "当前界面: {}", screenName);
                
                // 只有在非物品栏界面才执行排序，避免与Mixin冲突
                if (screenName == null || (!screenName.contains("InventoryScreen") && !screenName.contains("CreativeInventoryScreen"))) {
                    InventorySortController controller = InventorySortController.getInstance();
                    controller.sortInventory(InventorySortConfig.SortMode.NAME); // 默认按名称排序
                    LogUtil.info("KeyBindings", "背包整理执行完成");
                } else {
                    LogUtil.info("KeyBindings", "当前在物品栏界面，由Mixin处理R键");
                }
            } else {
                LogUtil.info("KeyBindings", "客户端不存在，跳过排序");
            }
        }
        
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
}
