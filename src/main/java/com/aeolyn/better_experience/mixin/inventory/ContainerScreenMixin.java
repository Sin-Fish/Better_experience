package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.client.KeyBindings;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 容器界面Mixin
 * 处理快捷键排序功能
 */
@Mixin(HandledScreen.class)
public class ContainerScreenMixin {
    
    // 处理 R 键和 Shift+R 键按下（支持按键绑定界面可见与重映射）
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        // 扩展支持所有界面，不仅仅是容器界面
        var sortKey = KeyBindings.getSortInventoryKey();
        var smartKey = KeyBindings.getSmartTransferKey();
        boolean sortMatch = sortKey != null && sortKey.matchesKey(keyCode, scanCode);
        boolean smartMatch = smartKey != null && smartKey.matchesKey(keyCode, scanCode);

        if (sortMatch || smartMatch) {
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            InventorySortController controller = InventorySortController.getInstance();

            if (smartMatch && (!sortMatch || isShiftPressed)) {
                LogUtil.info("ContainerScreenMixin", "在界面 " + screen.getClass().getSimpleName() + " 检测到 智能转移 快捷键，执行智能转移");
                controller.smartTransferItems();
            } else if (sortMatch && (!smartMatch || !isShiftPressed)) {
                LogUtil.info("ContainerScreenMixin", "在界面 " + screen.getClass().getSimpleName() + " 检测到 一键整理 快捷键，执行一键整理");
                controller.smartSortByMousePosition();
            }

            cir.setReturnValue(true);
        }
    }
}
