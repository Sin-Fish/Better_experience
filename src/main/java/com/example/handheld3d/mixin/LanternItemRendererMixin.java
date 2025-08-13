package com.example.handheld3d.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class LanternItemRendererMixin {
    
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", 
            at = @At("HEAD"), cancellable = true)
    private void onRenderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, 
                             MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, 
                             int overlay, BakedModel model, CallbackInfo ci) {
        
        // 检查是否是灯笼且在手持模式
        if (stack != null && stack.isOf(Items.LANTERN) && isHandheldMode(renderMode)) {
            
            // 取消原版渲染
            ci.cancel();
            
            // 渲染3D灯笼实体
            render3DLantern(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay);
        }
    }
    
    private boolean isHandheldMode(ModelTransformationMode renderMode) {
        return renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || 
               renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND ||
               renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND ||
               renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
    }
    
    private void render3DLantern(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded,
                                MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        
        // 获取灯笼的方块状态
        var lanternBlock = Items.LANTERN.getBlock();
        if (lanternBlock == null) return;
        
        var blockState = lanternBlock.getDefaultState();
        
        // 调整矩阵变换
        matrices.push();
        
        // 根据渲染模式调整位置和旋转
        if (renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || 
            renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) {
            
            // 第一人称手持
            matrices.translate(0.0, 0.15, 0.0); // 稍微抬高
            matrices.scale(0.7f, 0.7f, 0.7f); // 缩小一点
            
            if (leftHanded) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
            }
            
        } else if (renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || 
                   renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
            
            // 第三人称手持
            matrices.translate(0.0, 0.25, 0.0); // 抬高更多
            matrices.scale(0.5f, 0.5f, 0.5f); // 更小一些
            
            if (leftHanded) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
            }
        }
        
        // 渲染灯笼方块
        var client = MinecraftClient.getInstance();
        if (client != null && client.getBlockRenderManager() != null) {
            client.getBlockRenderManager().renderBlockAsEntity(
                blockState, matrices, vertexConsumers, light, overlay
            );
        }
        
        matrices.pop();
    }
}
