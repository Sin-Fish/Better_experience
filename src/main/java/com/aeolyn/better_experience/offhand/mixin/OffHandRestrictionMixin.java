package com.aeolyn.better_experience.offhand.mixin;

import com.aeolyn.better_experience.offhand.core.OffHandRestrictionController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 副手限制Mixin拦截器
 * 拦截副手物品使用和方块放置，根据配置决定是否阻止
 */
@Mixin(ClientPlayerInteractionManager.class)
public class OffHandRestrictionMixin {
    
    /**
     * 拦截物品使用（右键点击）
     * 检查是否为副手使用，如果是则根据配置决定是否阻止
     */
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItem(net.minecraft.entity.player.PlayerEntity player, Hand hand, CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        // 检查是否为副手使用
        if (hand == Hand.OFF_HAND) {
            ItemStack offHandStack = player.getOffHandStack();
            
            if (!offHandStack.isEmpty()) {
                OffHandRestrictionController controller = OffHandRestrictionController.getInstance();
                
                // 检查道具使用是否被允许
                if (!controller.isItemUsageAllowed(offHandStack.getItem())) {
                    // 静默取消，不显示任何提示
                    cir.setReturnValue(net.minecraft.util.ActionResult.PASS);
                    cir.cancel();
                }
            }
        }
    }
    
    /**
     * 拦截对方块使用物品（右键点击方块）
     * 检查是否为副手使用，如果是则根据配置决定是否阻止
     */
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        // 检查是否为副手使用
        if (hand == Hand.OFF_HAND) {
            ItemStack offHandStack = player.getOffHandStack();
            
            if (!offHandStack.isEmpty()) {
                OffHandRestrictionController controller = OffHandRestrictionController.getInstance();
                
                // 检查方块放置是否被允许
                if (!controller.isBlockPlacementAllowed(offHandStack.getItem())) {
                    // 静默取消，不显示任何提示
                    cir.setReturnValue(net.minecraft.util.ActionResult.PASS);
                    cir.cancel();
                }
            }
        }
    }
}
