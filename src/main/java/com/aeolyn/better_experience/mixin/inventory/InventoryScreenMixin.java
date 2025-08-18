package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 背包界面Mixin
 * 在背包界面添加整理按钮和按键处理
 */
@Mixin(net.minecraft.client.gui.screen.Screen.class)
public class InventoryScreenMixin {
    
    private ButtonWidget sortButton;
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        net.minecraft.client.gui.screen.Screen screen = (net.minecraft.client.gui.screen.Screen) (Object) this;
        
        // 只对InventoryScreen添加按钮
        if (screen instanceof InventoryScreen) {
            // 检查是否显示整理按钮
            ConfigManager configManager = ConfigManager.getInstance();
            InventorySortConfig config = new InventorySortConfig(); // 暂时使用默认配置
            
            if (config != null && config.isShowSortButtons()) {
                // 在背包右侧添加整理按钮
                sortButton = ButtonWidget.builder(
                    Text.literal("整理背包"),
                    button -> {
                        InventorySortController controller = InventorySortController.getInstance();
                        controller.sortInventory(config.getDefaultSortMode(), false); // 普通模式
                    }
                ).dimensions(screen.width - 100, 10, 80, 20).build();
                
                // 添加合并模式按钮
                ButtonWidget mergeButton = ButtonWidget.builder(
                    Text.literal("合并整理"),
                    button -> {
                        InventorySortController controller = InventorySortController.getInstance();
                        controller.sortInventory(config.getDefaultSortMode(), true); // 合并模式
                    }
                ).dimensions(screen.width - 100, 35, 80, 20).build();
                
                // 使用反射添加按钮到界面
                try {
                    // 尝试不同的字段名
                    java.lang.reflect.Field childrenField = null;
                    try {
                        childrenField = net.minecraft.client.gui.screen.Screen.class.getDeclaredField("children");
                    } catch (NoSuchFieldException e1) {
                        try {
                            childrenField = net.minecraft.client.gui.screen.Screen.class.getDeclaredField("field_22787"); // 混淆后的字段名
                        } catch (NoSuchFieldException e2) {
                            childrenField = net.minecraft.client.gui.screen.Screen.class.getDeclaredField("drawables"); // 可能的字段名
                        }
                    }
                    
                    if (childrenField != null) {
                        childrenField.setAccessible(true);
                        Object childrenObj = childrenField.get(screen);
                        if (childrenObj instanceof java.util.List) {
                            java.util.List<net.minecraft.client.gui.Element> children = (java.util.List<net.minecraft.client.gui.Element>) childrenObj;
                            children.add(sortButton);
                            children.add(mergeButton);
                            LogUtil.info("InventoryScreenMixin", "成功添加整理按钮到背包界面");
                        }
                    }
                } catch (Exception e) {
                    // 如果反射失败，记录错误但不崩溃
                    LogUtil.warn("InventoryScreenMixin", "无法添加整理按钮到背包界面: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 处理按键输入，在物品栏界面中直接检测R键
     * 使用完整的方法签名
     */
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        net.minecraft.client.gui.screen.Screen screen = (net.minecraft.client.gui.screen.Screen) (Object) this;
        
        // 只对InventoryScreen处理R键
        if (screen instanceof InventoryScreen && keyCode == GLFW.GLFW_KEY_R) {
            LogUtil.info("InventoryScreenMixin", "在物品栏界面检测到R键按下");
            
            // 执行智能排序
            InventorySortController controller = InventorySortController.getInstance();
            controller.smartSortByMousePosition();
            
            // 阻止按键继续传播
            cir.setReturnValue(true);
            return;
        }
    }
}
