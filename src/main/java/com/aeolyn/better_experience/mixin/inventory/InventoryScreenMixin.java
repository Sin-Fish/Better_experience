package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 背包界面Mixin
 * 在背包界面添加整理按钮
 */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    
    private ButtonWidget sortButton;
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        
        // 检查是否显示整理按钮
        ConfigManager configManager = ConfigManager.getInstance();
        InventorySortConfig config = new InventorySortConfig(); // 暂时使用默认配置
        
        if (config != null && config.isShowSortButtons()) {
            // 在背包右侧添加整理按钮
            sortButton = ButtonWidget.builder(
                Text.literal("整理背包"),
                button -> {
                    InventorySortController controller = InventorySortController.getInstance();
                    controller.sortInventory(config.getDefaultSortMode());
                }
            ).dimensions(screen.width - 100, 10, 80, 20).build();
            
            // 使用反射添加按钮到界面
            try {
                java.lang.reflect.Field childrenField = net.minecraft.client.gui.screen.Screen.class.getDeclaredField("children");
                childrenField.setAccessible(true);
                java.util.List<net.minecraft.client.gui.Element> children = (java.util.List<net.minecraft.client.gui.Element>) childrenField.get(screen);
                children.add(sortButton);
            } catch (Exception e) {
                // 如果反射失败，记录错误但不崩溃
                com.aeolyn.better_experience.BetterExperienceMod.LOGGER.warn("无法添加整理按钮到背包界面", e);
            }
        }
    }
}
