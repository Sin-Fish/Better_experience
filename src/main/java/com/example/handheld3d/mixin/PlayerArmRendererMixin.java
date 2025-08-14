package com.example.handheld3d.mixin;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerArmRendererMixin {
    
    private boolean hasShownArmMessage = false;
    
    @Inject(method = "renderArm", at = @At("HEAD"))
    private void onRenderArm(Object matrices, Object vertexConsumers, int light, 
                            Object player, Object model, CallbackInfo ci) {
        
        if (player == null) return;
        
        try {
            // 使用反射获取玩家手中的物品
            var playerClass = player.getClass();
            var getStackInHandMethod = playerClass.getMethod("getStackInHand", Hand.class);
            
            ItemStack mainHandStack = (ItemStack) getStackInHandMethod.invoke(player, Hand.MAIN_HAND);
            ItemStack offHandStack = (ItemStack) getStackInHandMethod.invoke(player, Hand.OFF_HAND);
            
            if ((mainHandStack != null && mainHandStack.isOf(Items.LANTERN)) || 
                (offHandStack != null && offHandStack.isOf(Items.LANTERN))) {
                
                // 显示检测消息（只显示一次）
                if (!hasShownArmMessage) {
                    try {
                        var sendMessageMethod = playerClass.getMethod("sendMessage", Text.class, boolean.class);
                        sendMessageMethod.invoke(player, Text.literal("🎯 [Handheld3D] 检测到手持灯笼，调整手臂位置..."), false);
                        hasShownArmMessage = true;
                    } catch (Exception e) {
                        System.out.println("🎯 [Handheld3D] 无法发送手臂调整消息: " + e.getMessage());
                    }
                }
                
                // 暂时注释掉矩阵变换，避免黑屏
                /*
                // 使用反射调用矩阵变换方法
                var matricesClass = matrices.getClass();
                var translateMethod = matricesClass.getMethod("translate", double.class, double.class, double.class);
                
                // 调整手臂位置，让手位于灯笼上方
                translateMethod.invoke(matrices, 0.0, -0.15, 0.0); // 降低手臂位置
                translateMethod.invoke(matrices, 0.0, 0.0, -0.05); // 稍微向前
                */
            }
            
        } catch (Exception e) {
            // 如果反射失败，记录错误但不崩溃
            System.err.println("🎯 [Handheld3D] 手臂渲染调整失败: " + e.getMessage());
        }
    }
}
