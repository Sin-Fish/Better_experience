package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 容器界面Mixin
 * 在容器界面添加整理按钮
 */
@Mixin(HandledScreen.class)
public class ContainerScreenMixin {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        
        // 只对GenericContainerScreen（箱子等）添加按钮
        if (screen instanceof GenericContainerScreen) {
            LogUtil.info("ContainerScreenMixin", "检测到容器界面，准备添加按钮");
            
            // 检查是否显示整理按钮
            ConfigManager configManager = ConfigManager.getInstance();
            InventorySortConfig config = new InventorySortConfig(); // 暂时使用默认配置
            
            if (config != null && config.isShowSortButtons()) {
                addContainerButtons(screen);
            } else {
                LogUtil.info("ContainerScreenMixin", "按钮显示已禁用");
            }
        }
    }
    
    private void addContainerButtons(HandledScreen<?> screen) {
        // 获取容器
        net.minecraft.inventory.Inventory container = getContainerFromScreen(screen);
        if (container == null) {
            LogUtil.warn("ContainerScreenMixin", "无法获取容器实例");
            return;
        }
        
        // 在容器右侧添加整理按钮
        ButtonWidget sortButton = ButtonWidget.builder(
            Text.literal("整理容器"),
            button -> {
                InventorySortController controller = InventorySortController.getInstance();
                controller.sortContainer(container, InventorySortConfig.SortMode.NAME, false); // 普通模式
            }
        ).dimensions(screen.width - 100, 10, 80, 20).build();
        
        // 添加合并模式按钮
        ButtonWidget mergeButton = ButtonWidget.builder(
            Text.literal("合并整理"),
            button -> {
                InventorySortController controller = InventorySortController.getInstance();
                controller.sortContainer(container, InventorySortConfig.SortMode.NAME, true); // 合并模式
            }
        ).dimensions(screen.width - 100, 35, 80, 20).build();
        
        // 添加一键存入按钮
        ButtonWidget depositButton = ButtonWidget.builder(
            Text.literal("一键存入"),
            button -> {
                InventorySortController controller = InventorySortController.getInstance();
                controller.depositToContainer(container);
            }
        ).dimensions(screen.width - 100, 60, 80, 20).build();
        
        // 添加一键取出按钮
        ButtonWidget withdrawButton = ButtonWidget.builder(
            Text.literal("一键取出"),
            button -> {
                InventorySortController controller = InventorySortController.getInstance();
                controller.withdrawFromContainer(container);
            }
        ).dimensions(screen.width - 100, 85, 80, 20).build();
        
        // 使用反射添加按钮到界面
        try {
            // 尝试不同的字段名
            java.lang.reflect.Field childrenField = null;
            try {
                childrenField = screen.getClass().getDeclaredField("children");
            } catch (NoSuchFieldException e1) {
                try {
                    childrenField = screen.getClass().getDeclaredField("field_22787"); // 混淆后的字段名
                } catch (NoSuchFieldException e2) {
                    childrenField = screen.getClass().getDeclaredField("drawables"); // 可能的字段名
                }
            }
            
            if (childrenField != null) {
                childrenField.setAccessible(true);
                Object childrenObj = childrenField.get(screen);
                if (childrenObj instanceof java.util.List) {
                    java.util.List<net.minecraft.client.gui.Element> children = (java.util.List<net.minecraft.client.gui.Element>) childrenObj;
                    children.add(sortButton);
                    children.add(mergeButton);
                    children.add(depositButton);
                    children.add(withdrawButton);
                    LogUtil.info("ContainerScreenMixin", "成功添加容器按钮");
                }
            }
        } catch (Exception e) {
            LogUtil.warn("ContainerScreenMixin", "无法添加容器按钮: " + e.getMessage());
        }
    }
    
    private net.minecraft.inventory.Inventory getContainerFromScreen(HandledScreen<?> screen) {
        try {
            // 尝试获取容器实例
            java.lang.reflect.Field handlerField = screen.getClass().getDeclaredField("handler");
            handlerField.setAccessible(true);
            Object handler = handlerField.get(screen);
            if (handler != null) {
                java.lang.reflect.Field inventoryField = handler.getClass().getDeclaredField("inventory");
                inventoryField.setAccessible(true);
                return (net.minecraft.inventory.Inventory) inventoryField.get(handler);
            }
        } catch (Exception e) {
            LogUtil.warn("ContainerScreenMixin", "无法获取容器实例: " + e.getMessage());
        }
        return null;
    }
}
