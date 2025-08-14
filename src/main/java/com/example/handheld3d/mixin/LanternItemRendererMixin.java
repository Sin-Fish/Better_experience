package com.example.handheld3d.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
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
            at = @At("HEAD"), cancellable = true, require = 0)
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
                
                // 检查当前渲染的物品是否是灯笼
                if (isRenderingLantern(quads)) {
                    System.out.println("🎯 [Handheld3D] 检测到灯笼，开始3D渲染!");
                    
                    // 取消原始渲染
                    ci.cancel();
                    
                    // 渲染3D灯笼
                    render3DLantern(matrices, vertexConsumers, light, overlay, displayContext);
                }
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] ItemRenderer检测错误: " + e.getMessage());
            e.printStackTrace();
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
    
    private static boolean isRenderingLantern(List quads) {
        // 检查当前玩家手持的物品是否是灯笼
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                ItemStack mainHand = client.player.getMainHandStack();
                ItemStack offHand = client.player.getOffHandStack();
                
                return (mainHand != null && mainHand.isOf(Items.LANTERN)) || 
                       (offHand != null && offHand.isOf(Items.LANTERN));
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 灯笼检测错误: " + e.getMessage());
        }
        return false;
    }
    
    private static void render3DLantern(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                       int light, int overlay, ItemDisplayContext displayContext) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) return;
            
            BlockRenderManager blockRenderManager = client.getBlockRenderManager();
            BlockState lanternState = Blocks.LANTERN.getDefaultState();
            
            // 根据渲染上下文调整矩阵变换
            adjustMatrixForContext(matrices, displayContext);
            
            // 渲染3D灯笼模型
            blockRenderManager.renderBlockAsEntity(lanternState, matrices, vertexConsumers, light, overlay);
            
            System.out.println("🎯 [Handheld3D] 3D灯笼渲染完成!");
            
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 3D渲染错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void adjustMatrixForContext(MatrixStack matrices, ItemDisplayContext displayContext) {
        String contextName = displayContext.name();
        
        // 根据不同的渲染上下文调整位置和大小
        if (contextName.contains("FIRST_PERSON")) {
            // 第一人称视角 - 调整位置和大小
            matrices.scale(1.2f, 1.2f, 1.2f);  // 增大灯笼尺寸
            matrices.translate(0.0, -0.2, 0.0); // 调整位置
        } else if (contextName.contains("THIRD_PERSON")) {
            // 第三人称视角 - 调整位置和大小
            matrices.scale(0.8f, 0.8f, 0.8f);   // 适中的尺寸
            matrices.translate(0.0, -0.1, 0.0); // 调整位置
        }
    }
}
