package com.aeolyn.better_experience.mixin.inventory;

import com.aeolyn.better_experience.client.KeyBindings;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.core.InventorySortController;
import com.aeolyn.better_experience.inventory.core.InventoryTransferController;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 背包界面Mixin
 * 专门处理背包界面的按键事件
 */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    
    // 处理背包界面的 R 键和 Shift+R 键按下
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        InventoryScreen screen = (InventoryScreen) (Object) this;

        // 检查按键匹配
        var sortKey = KeyBindings.getSortInventoryKey();
        var smartKey = KeyBindings.getSmartTransferKey();
        boolean sortMatch = sortKey != null && sortKey.matchesKey(keyCode, scanCode);
        boolean smartMatch = smartKey != null && smartKey.matchesKey(keyCode, scanCode);

        if (sortMatch || smartMatch) {
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            InventorySortController controller = InventorySortController.getInstance();

            if (smartMatch && (!sortMatch || isShiftPressed)) {
                LogUtil.info("InventoryScreenMixin", "在背包界面检测到 智能转移 快捷键，执行智能转移");
                InventoryTransferController.getInstance().smartTransferItems();
            } else if (sortMatch && (!smartMatch || !isShiftPressed)) {
                LogUtil.info("InventoryScreenMixin", "在背包界面检测到 一键整理 快捷键，执行背包排序");
                // 直接调用背包排序，启用合并模式
                controller.sortInventory(com.aeolyn.better_experience.inventory.config.InventorySortConfig.SortMode.NAME, true);
            }

            cir.setReturnValue(true);
        }
    }
}
