package com.aeolyn.better_experience.mixin;

import com.aeolyn.better_experience.BetterExperienceMod;
import com.aeolyn.better_experience.config.ConfigManager;
import com.aeolyn.better_experience.core.ItemRenderer3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.registry.Registries;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class GenericItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V",
            at = @At("HEAD"), cancellable = true)
    private static void render3DItem(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List quads, RenderLayer layer, ItemRenderState.Glint glint, CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Item item = null;
        String context = displayContext.name();
        if (context.contains("FIRST_PERSON_RIGHT_HAND") || context.contains("THIRD_PERSON_RIGHT_HAND")) {
            item = client.player.getMainHandStack().getItem();
        } else if (context.contains("FIRST_PERSON_LEFT_HAND") || context.contains("THIRD_PERSON_LEFT_HAND")) {
            item = client.player.getOffHandStack().getItem();
        }

        if (item != null && ConfigManager.isItemEnabled(Registries.ITEM.getId(item).toString())) {
            // 使用3D渲染器
            ItemRenderer3D renderer3D = new ItemRenderer3D(ConfigManager.getInstance());
            
            if (renderer3D.shouldRender3D(item, displayContext)) {
                // 取消原版渲染
                ci.cancel();
                
                // 执行3D渲染
                renderer3D.render3DItem(item, displayContext, matrices, vertexConsumers, light, overlay);
            }
        }
    }
}
