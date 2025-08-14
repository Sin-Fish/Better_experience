package com.example.handheld3d.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class LanternItemRendererMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // 在游戏tick时检查是否是第一次tick
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            // 只在第一次tick时显示消息
            if (client.player.age == 1) {
                client.player.sendMessage(Text.literal("🎯 [Handheld3D] Mixin 已成功加载!"), false);
            }
        }
    }
}
