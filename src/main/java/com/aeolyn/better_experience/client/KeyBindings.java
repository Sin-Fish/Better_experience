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

public class KeyBindings {
    private static KeyBinding openConfigKey;
    private static KeyBinding sortInventoryKey;
    private static KeyBinding depositToContainerKey;
    private static KeyBinding withdrawFromContainerKey;
    private static KeyBinding sortContainerKey;
    
    public static void register() {
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
    }
    
    public static void tick() {
        // 检查配置界面按键是否被按下
        if (openConfigKey.wasPressed()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new ModConfigScreen(null, ConfigManager.getInstance()));
            }
        }
        
        // 检查背包整理按键是否被按下
        if (sortInventoryKey.wasPressed()) {
            LogUtil.info("KeyBindings", "检测到R键按下，开始执行背包整理");
            InventorySortController controller = InventorySortController.getInstance();
            controller.sortInventory(InventorySortConfig.SortMode.NAME); // 默认按名称排序
        }
        
        if (depositToContainerKey.wasPressed()) {
            // 一键存入容器功能
            // 这里需要检测当前是否在容器界面
            InventorySortController controller = InventorySortController.getInstance();
            // TODO: 获取当前容器并执行存入操作
        }
        
        if (withdrawFromContainerKey.wasPressed()) {
            // 一键从容器拿取功能
            // 这里需要检测当前是否在容器界面
            InventorySortController controller = InventorySortController.getInstance();
            // TODO: 获取当前容器并执行拿取操作
        }
        
        if (sortContainerKey.wasPressed()) {
            // 整理容器功能
            // 这里需要检测当前是否在容器界面
            InventorySortController controller = InventorySortController.getInstance();
            // TODO: 获取当前容器并执行整理操作
        }
    }
}
