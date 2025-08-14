package com.example.handheld3d.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public class LanternItemRendererMixin {
    
    private static boolean hasShownMessage = false;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("🎯 [Handheld3D] ItemRenderer Mixin 已加载!");
    }
    
    // 根据官方Yarn映射使用正确的参数类型
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V", 
            at = @At("HEAD"), cancellable = false, require = 0)
    private static void onRenderItem(ItemDisplayContext displayContext, MatrixStack matrices, 
                                    VertexConsumerProvider vertexConsumers, int light, int overlay,
                                    int[] tints, List quads, RenderLayer layer, 
                                    ItemRenderState.Glint glint, CallbackInfo ci) {
        
        try {
            // 检查是否是手持模式
            if (isHandheldMode(displayContext)) {
                System.out.println("🎯 [Handheld3D] 检测到手持渲染模式!");
                
                // 显示检测消息（只显示一次）
                if (!hasShownMessage) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("🎯 [Handheld3D] 3D渲染系统已激活!"), false);
                        hasShownMessage = true;
                    }
                }
                
                // 这里可以添加3D渲染逻辑
                // 暂时不取消原始渲染，避免黑屏
                // ci.cancel();
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] ItemRenderer检测错误: " + e.getMessage());
        }
    }
    
    private static boolean isHandheldMode(ItemDisplayContext displayContext) {
        try {
            String contextName = displayContext.name();
            return contextName.contains("FIRST_PERSON_LEFT_HAND") ||
                   contextName.contains("FIRST_PERSON_RIGHT_HAND") ||
                   contextName.contains("THIRD_PERSON_LEFT_HAND") ||
                   contextName.contains("THIRD_PERSON_RIGHT_HAND");
        } catch (Exception e) {
            return false;
        }
    }
}
