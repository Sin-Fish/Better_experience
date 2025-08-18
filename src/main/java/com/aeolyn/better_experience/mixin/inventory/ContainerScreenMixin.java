package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.client.KeyBindings;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import com.aeolyn.better_experience.inventory.core.InventoryTransferController;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 容器界面Mixin
 * 处理容器界面的按键事件
 */
@Mixin(HandledScreen.class)
public class ContainerScreenMixin {
    
    // 处理容器界面的 R 键和 Shift+R 键按下
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        // 检查按键匹配
        var sortKey = KeyBindings.getSortInventoryKey();
        var smartKey = KeyBindings.getSmartTransferKey();
        boolean sortMatch = sortKey != null && sortKey.matchesKey(keyCode, scanCode);
        boolean smartMatch = smartKey != null && smartKey.matchesKey(keyCode, scanCode);

        if (sortMatch || smartMatch) {
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            InventorySortController controller = InventorySortController.getInstance();

            if (smartMatch && (!sortMatch || isShiftPressed)) {
                LogUtil.info("ContainerScreenMixin", "在容器界面检测到 智能转移 快捷键，执行智能转移");
                InventoryTransferController.getInstance().smartTransferItems();
            } else if (sortMatch && (!smartMatch || !isShiftPressed)) {
                LogUtil.info("ContainerScreenMixin", "在容器界面检测到 一键整理 快捷键，执行智能排序");
                controller.smartSortByMousePosition();
            }

            cir.setReturnValue(true);
        }
    }
}
