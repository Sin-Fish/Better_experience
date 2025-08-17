package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.client.gui.Render3DConfigScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfirmDeleteScreen extends Screen {
    private final Screen parentScreen;
    private final ConfigManager configManager;
    private final String itemId;
    
    public ConfirmDeleteScreen(Screen parentScreen, ConfigManager configManager, String itemId) {
        super(Text.literal("确认删除"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
        this.itemId = itemId;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // 确认删除按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("确认删除"),
            button -> {
                configManager.removeItemConfig(itemId);
                if (parentScreen instanceof Render3DConfigScreen) {
                    ((Render3DConfigScreen) parentScreen).refreshItemList();
                } else if (parentScreen instanceof ModConfigScreen) {
                    ((ModConfigScreen) parentScreen).refreshItemList();
                }
                this.close();
            }
        ).dimensions(centerX - 100, centerY + 20, 200, 20).build());
        
        // 取消按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("取消"),
            button -> this.close()
        ).dimensions(centerX - 100, centerY + 50, 200, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        
        // 绘制确认信息
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("确定要删除物品配置吗？"), 
            this.width / 2, this.height / 2 - 20, 0xCCCCCC);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("物品ID: " + itemId), 
            this.width / 2, this.height / 2, 0xCCCCCC);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parentScreen);
    }
}
