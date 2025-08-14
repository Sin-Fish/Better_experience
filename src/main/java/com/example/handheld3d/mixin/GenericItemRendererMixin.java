package com.example.handheld3d.mixin;

import com.example.handheld3d.config.ConfigManager;
import com.example.handheld3d.core.Item3DRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public class GenericItemRendererMixin {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("Handheld3D-Mixin");
    private static boolean hasShownMessage = false;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        LOGGER.info("通用物品渲染器Mixin已加载!");
    }
    
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V", 
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void onRenderItem(ItemDisplayContext displayContext, MatrixStack matrices, 
                                    VertexConsumerProvider vertexConsumers, int light, int overlay,
                                    int[] tints, List quads, RenderLayer layer, 
                                    ItemRenderState.Glint glint, CallbackInfo ci) {
        
        try {
            // 检查是否是手持模式
            if (isHandheldMode(displayContext)) {
                if (ConfigManager.isDebugEnabled()) {
                    LOGGER.debug("检测到手持渲染模式: " + displayContext.name());
                }
                
                // 显示检测消息（只显示一次）
                if (!hasShownMessage) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("🎯 [Handheld3D] 通用3D渲染系统已激活!"), false);
                        hasShownMessage = true;
                    }
                }
                
                // 检查当前渲染的物品是否在配置中
                String currentItemId = getCurrentItemId(displayContext);
                if (currentItemId != null && ConfigManager.isItemEnabled(currentItemId)) {
                    if (ConfigManager.isDebugEnabled()) {
                        LOGGER.info("检测到配置物品，开始3D渲染: " + currentItemId);
                    }
                    
                    // 取消原始渲染
                    ci.cancel();
                    
                    // 渲染3D物品
                    Item3DRenderer.render3DItem(matrices, vertexConsumers, light, overlay, displayContext, currentItemId);
                } else if (ConfigManager.isDebugEnabled()) {
                    LOGGER.debug("物品不在配置中或未启用: " + currentItemId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("物品渲染器检测错误: " + e.getMessage(), e);
        }
    }
    
    private static boolean isHandheldMode(ItemDisplayContext displayContext) {
        try {
            String contextName = displayContext.name();
            boolean isHandheld = contextName.contains("FIRST_PERSON_LEFT_HAND") ||
                               contextName.contains("FIRST_PERSON_RIGHT_HAND") ||
                               contextName.contains("THIRD_PERSON_LEFT_HAND") ||
                               contextName.contains("THIRD_PERSON_RIGHT_HAND");
            
            if (ConfigManager.isDebugEnabled()) {
                LOGGER.debug("渲染上下文: " + contextName + ", 是否手持: " + isHandheld);
            }
            return isHandheld;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static String getCurrentItemId(ItemDisplayContext displayContext) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                String contextName = displayContext.name();
                ItemStack mainHand = client.player.getMainHandStack();
                ItemStack offHand = client.player.getOffHandStack();
                
                boolean isMainHand = contextName.contains("RIGHT_HAND");
                boolean isOffHand = contextName.contains("LEFT_HAND");
                
                ItemStack currentStack = null;
                if (isMainHand) {
                    currentStack = mainHand;
                } else if (isOffHand) {
                    currentStack = offHand;
                }
                
                if (currentStack != null && !currentStack.isEmpty()) {
                    Identifier itemId = Registries.ITEM.getId(currentStack.getItem());
                    return itemId.toString();
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取当前物品ID错误: " + e.getMessage(), e);
        }
        return null;
    }
}
