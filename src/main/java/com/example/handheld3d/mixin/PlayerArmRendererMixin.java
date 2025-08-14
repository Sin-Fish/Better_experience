package com.example.handheld3d.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerArmRendererMixin {
    
    private boolean hasShownArmMessage = false;
    
    @Inject(method = "setupTransforms", at = @At("TAIL"))
    private void onSetupTransforms(AbstractClientPlayerEntity player, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
        // 在游戏tick时检查是否手持灯笼
        try {
            if (player != null) {
                ItemStack mainHand = player.getMainHandStack();
                ItemStack offHand = player.getOffHandStack();
                
                if ((mainHand != null && mainHand.isOf(Items.LANTERN)) || 
                    (offHand != null && offHand.isOf(Items.LANTERN))) {
                    
                    if (!hasShownArmMessage) {
                        player.sendMessage(Text.literal("🎯 [Handheld3D] 检测到手持灯笼，准备调整手臂位置..."), false);
                        hasShownArmMessage = true;
                        System.out.println("🎯 [Handheld3D] 检测到灯笼，准备调整手臂!");
                    }
                    
                    // 调整手臂位置 - 抬起手臂
                    adjustArmPosition(matrices, mainHand, offHand);
                } else {
                    // 重置消息状态，这样下次手持灯笼时还会显示
                    hasShownArmMessage = false;
                }
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 手臂检测错误: " + e.getMessage());
        }
    }
    
    private void adjustArmPosition(MatrixStack matrices, ItemStack mainHand, ItemStack offHand) {
        try {
            // 检查主手是否持有灯笼
            if (mainHand != null && mainHand.isOf(Items.LANTERN)) {
                // 调整主手手臂位置
                matrices.translate(0.0, -0.1, 0.0); // 稍微抬起
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(-15.0f)); // 向前倾斜
            }
            
            // 检查副手是否持有灯笼
            if (offHand != null && offHand.isOf(Items.LANTERN)) {
                // 调整副手手臂位置
                matrices.translate(0.0, -0.1, 0.0); // 稍微抬起
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(-15.0f)); // 向前倾斜
            }
            
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 手臂位置调整错误: " + e.getMessage());
        }
    }
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("🎯 [Handheld3D] PlayerEntityRenderer Mixin 已加载!");
    }
}
