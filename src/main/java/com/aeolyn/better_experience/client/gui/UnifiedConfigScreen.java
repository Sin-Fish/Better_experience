package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.manager.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnifiedConfigScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-UnifiedConfig");
    
    private final ConfigManager configManager;
    
    public UnifiedConfigScreen(ConfigManager configManager) {
        super(Text.translatable("better_experience.config.unified.title"));
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 50;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        
        // 3D渲染配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.3d_rendering"), 
            button -> {
                this.client.setScreen(new ModConfigScreen(configManager));
            }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());
        
        // 副手限制配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.offhand_restrictions"), 
            button -> {
                this.client.setScreen(new OffHandRestrictionConfigScreen(this, configManager));
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());
        
        // 关闭按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.close"), 
            button -> {
                this.close();
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        Text description = Text.translatable("better_experience.config.unified.description");
        context.drawCenteredTextWithShadow(this.textRenderer, description, this.width / 2, 40, 0xAAAAAA);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
