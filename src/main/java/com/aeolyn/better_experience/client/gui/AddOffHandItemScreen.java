package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.manager.ConfigManager;
import com.aeolyn.better_experience.config.OffHandRestrictionConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AddOffHandItemScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-AddOffHandItem");
    
    private final Screen parentScreen;
    private final ConfigManager configManager;
    private final OffHandRestrictionConfigScreen.DisplayMode mode;
    private TextFieldWidget itemIdField;
    
    public AddOffHandItemScreen(Screen parentScreen, ConfigManager configManager, OffHandRestrictionConfigScreen.DisplayMode mode) {
        super(Text.translatable("better_experience.config.offhand_restrictions.add_item"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
        this.mode = mode;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 30;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int buttonWidth = 100;
        int buttonHeight = 20;
        
        // 物品ID输入框
        itemIdField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight,
            Text.translatable("better_experience.config.offhand_restrictions.item_id_placeholder"));
        
        // 添加按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.offhand_restrictions.add"),
            button -> {
                addItem();
            }
        ).dimensions(centerX - buttonWidth - 10, startY + fieldHeight + 10, buttonWidth, buttonHeight).build());
        
        // 取消按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.cancel"),
            button -> {
                this.client.setScreen(parentScreen);
            }
        ).dimensions(centerX + 10, startY + fieldHeight + 10, buttonWidth, buttonHeight).build());
    }
    
    private void addItem() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            return;
        }
        
        OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
        
        if (mode == OffHandRestrictionConfigScreen.DisplayMode.BLOCK_PLACEMENT_LIST) {
            List<String> items = new ArrayList<>(config.getBlockPlacement().getAllowedItems());
            if (!items.contains(itemId)) {
                items.add(itemId);
                config.getBlockPlacement().setAllowedItems(items);
                LOGGER.info("添加方块放置允许物品: {}", itemId);
            }
        } else {
            List<String> items = new ArrayList<>(config.getItemUsage().getAllowedItems());
            if (!items.contains(itemId)) {
                items.add(itemId);
                config.getItemUsage().setAllowedItems(items);
                LOGGER.info("添加道具使用允许物品: {}", itemId);
            }
        }
        
        // 保存配置
        configManager.updateOffHandRestrictionConfig(config);
        configManager.saveOffHandRestrictionConfig();
        
        // 返回父界面
        this.client.setScreen(parentScreen);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        String description = mode == OffHandRestrictionConfigScreen.DisplayMode.BLOCK_PLACEMENT_LIST ? 
            "输入要添加到方块放置白名单的物品ID" : "输入要添加到道具使用白名单的物品ID";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(description), this.width / 2, 40, 0xAAAAAA);
        
        // 绘制示例
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("示例: minecraft:torch"), this.width / 2, 60, 0x888888);
        
        // 渲染输入框
        itemIdField.render(context, mouseX, mouseY, delta);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (itemIdField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (itemIdField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (itemIdField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
