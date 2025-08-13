package com.example.handheld3d.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerArmRendererMixin {
    
    @Inject(method = "renderArm", at = @At("HEAD"))
    private void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, 
                            AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> model, 
                            CallbackInfo ci) {
        
        if (player == null) return;
        
        // 检查玩家是否手持灯笼
        ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHandStack = player.getStackInHand(Hand.OFF_HAND);
        
        if ((mainHandStack != null && mainHandStack.isOf(Items.LANTERN)) || 
            (offHandStack != null && offHandStack.isOf(Items.LANTERN))) {
            
            // 调整手臂位置，让手位于灯笼上方
            matrices.translate(0.0, -0.15, 0.0); // 降低手臂位置
            matrices.translate(0.0, 0.0, -0.05); // 稍微向前
        }
    }
}
