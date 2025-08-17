package com.aeolyn.better_experience.offhand.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
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
    private TextFieldWidget itemIdField;
    private String errorMessage = "";
    private int errorMessageTicks = 0;
    
    public AddOffHandItemScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.offhand_restrictions.add_item"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
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
            Text.literal("物品ID"));
        itemIdField.setPlaceholder(Text.literal("格式: namespace:item_name"));
        itemIdField.setChangedListener(text -> {
            // 实时验证物品ID
            validateItemId(text);
        });
        
        // 添加按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.offhand_restrictions.add"),
            button -> {
                addItem();
            }
        ).dimensions(centerX - buttonWidth - 10, startY + fieldHeight + 10, buttonWidth, buttonHeight).build());
        
        // 添加输入框到界面
        this.addDrawableChild(itemIdField);
        
        // 取消按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.cancel"),
            button -> {
                this.client.setScreen(parentScreen);
            }
        ).dimensions(centerX + 10, startY + fieldHeight + 10, buttonWidth, buttonHeight).build());
    }
    
    private void showError(String message) {
        errorMessage = message;
        errorMessageTicks = 120; // 显示6秒 (20 ticks/秒)
        LOGGER.warn("添加物品错误: {}", message);
    }
    
    private void validateItemId(String itemId) {
        if (itemId != null && !itemId.isEmpty()) {
            // 首先检查格式
            if (!itemId.contains(":")) {
                LOGGER.debug("物品ID格式错误，缺少命名空间: {}", itemId);
                return;
            }
            
            try {
                net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(itemId));
                if (item != null) {
                    // 物品存在，可以显示预览
                    LOGGER.debug("物品ID有效: {}", itemId);
                } else {
                    LOGGER.debug("物品ID无效: {}", itemId);
                }
            } catch (Exception e) {
                LOGGER.debug("物品ID格式错误: {}", itemId);
            }
        }
    }
    
    private void addItem() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            showError("物品ID不能为空");
            return;
        }
        
        // 验证物品ID格式
        if (!itemId.contains(":")) {
            showError("物品ID格式错误，必须包含命名空间 (例如: minecraft:torch)");
            return;
        }
        
        // 验证物品是否存在
        try {
            net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(itemId));
            if (item == null) {
                showError("物品不存在: " + itemId);
                return;
            }
        } catch (Exception e) {
            showError("物品ID格式错误: " + itemId);
            return;
        }
        
        OffHandRestrictionConfig config = configManager.getOffHandRestrictionConfig();
        
        // 检查是否已经存在于白名单中
        if (config.isItemAllowed(itemId)) {
            showError("物品已存在于白名单中: " + itemId);
            return;
        }
        
        // 使用配置类中的方法添加物品
        config.addAllowedItem(itemId);
        
        LOGGER.info("成功添加副手白名单物品: {}", itemId);
        
        // 保存配置
        try {
            configManager.updateOffHandRestrictionConfig(config);
            configManager.saveOffHandRestrictionConfig();
            
            // 返回父界面并刷新列表
            this.client.setScreen(parentScreen);
            if (parentScreen instanceof OffHandRestrictionConfigScreen) {
                // 父界面会自动重新初始化
            }
        } catch (Exception e) {
            LOGGER.error("保存副手限制配置失败: " + e.getMessage(), e);
            showError("保存配置失败，请重试");
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("输入要添加到副手白名单的物品ID"), this.width / 2, 40, 0xAAAAAA);
        
        // 绘制示例
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("示例: minecraft:torch"), this.width / 2, 60, 0x888888);
        
        // 显示错误消息
        if (errorMessageTicks > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(errorMessage), this.width / 2, 120, 0xFF5555);
            errorMessageTicks--;
        }
        
        // 绘制物品预览（如果物品ID有效）
        String itemId = itemIdField.getText().trim();
        if (!itemId.isEmpty()) {
            try {
                net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(itemId));
                if (item != null) {
                    net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item);
                    context.drawItem(stack, this.width / 2 + 120, 70);
                    context.drawTextWithShadow(this.textRenderer, Text.literal("✓ 物品有效"), this.width / 2 + 120, 90, 0x00FF00);
                } else {
                    context.drawTextWithShadow(this.textRenderer, Text.literal("✗ 物品无效"), this.width / 2 + 120, 90, 0xFF5555);
                }
            } catch (Exception ignored) {
                context.drawTextWithShadow(this.textRenderer, Text.literal("✗ 格式错误"), this.width / 2 + 120, 90, 0xFF5555);
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
        
        // 调试信息：显示输入框的文本内容（仅在调试模式下）
        if (LOGGER.isDebugEnabled()) {
            String currentText = itemIdField.getText();
            context.drawTextWithShadow(this.textRenderer, Text.literal("当前输入: " + currentText), 10, 10, 0xFFFFFF);
        }
    }
    

    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
