package com.example.handheld3d.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class LanternItemRendererMixin {
    
    private boolean hasShownMessage = false;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // 在游戏tick时检查是否手持灯笼
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                ItemStack mainHand = client.player.getMainHandStack();
                ItemStack offHand = client.player.getOffHandStack();
                
                if ((mainHand != null && mainHand.isOf(Items.LANTERN)) || 
                    (offHand != null && offHand.isOf(Items.LANTERN))) {
                    
                    if (!hasShownMessage) {
                        client.player.sendMessage(Text.literal("🎯 [Handheld3D] 检测到手持灯笼!"), false);
                        hasShownMessage = true;
                        System.out.println("🎯 [Handheld3D] 检测到灯笼，已发送消息!");
                    }
                } else {
                    // 重置消息状态，这样下次手持灯笼时还会显示
                    hasShownMessage = false;
                }
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 检测错误: " + e.getMessage());
        }
    }
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("🎯 [Handheld3D] MinecraftClient Mixin 已加载!");
    }
}
