package com.example.handheld3d.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class PlayerArmRendererMixin {
    
    private boolean hasShownArmMessage = false;
    
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
                    
                    if (!hasShownArmMessage) {
                        client.player.sendMessage(Text.literal("🎯 [Handheld3D] 检测到手持灯笼，准备调整手臂位置..."), false);
                        hasShownArmMessage = true;
                        System.out.println("🎯 [Handheld3D] 检测到灯笼，准备调整手臂!");
                    }
                } else {
                    // 重置消息状态，这样下次手持灯笼时还会显示
                    hasShownArmMessage = false;
                }
            }
        } catch (Exception e) {
            System.err.println("🎯 [Handheld3D] 手臂检测错误: " + e.getMessage());
        }
    }
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("🎯 [Handheld3D] MinecraftClient Mixin 已加载!");
    }
}
