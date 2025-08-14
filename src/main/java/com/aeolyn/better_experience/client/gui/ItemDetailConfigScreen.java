package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.ConfigManager;
import com.aeolyn.better_experience.config.ItemConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemDetailConfigScreen extends Screen {
    private final ConfigManager configManager;
    private final ItemConfig config;
    
    private TextFieldWidget rotationXField;
    private TextFieldWidget rotationYField;
    private TextFieldWidget rotationZField;
    private TextFieldWidget translateXField;
    private TextFieldWidget translateYField;
    private TextFieldWidget translateZField;
    private SliderWidget scaleSlider;
    
    public ItemDetailConfigScreen(ConfigManager configManager, ItemConfig config) {
        super(Text.literal("配置: " + config.getItemId()));
        this.configManager = configManager;
        this.config = config;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 80;
        int fieldWidth = 80;
        int fieldHeight = 20;
        int spacing = 10;
        
        // 缩放滑块
        scaleSlider = new SliderWidget(centerX - 100, startY, 200, 20, 
            Text.literal("缩放: " + String.format("%.1f", config.getFirstPerson().getScale())),
            config.getFirstPerson().getScale() / 5.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("缩放: " + String.format("%.1f", this.value * 5.0)));
            }
            
            @Override
            protected void applyValue() {
                float scale = (float) (this.value * 5.0);
                config.getFirstPerson().setScale(scale);
                config.getThirdPerson().setScale(scale);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            }
        };
        
        // 旋转字段
        rotationXField = new TextFieldWidget(this.textRenderer, centerX - 150, startY + 40, fieldWidth, fieldHeight, Text.literal("X"));
        rotationXField.setText(String.format("%.1f", config.getFirstPerson().getRotationX()));
        rotationXField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setRotationX(value);
                config.getThirdPerson().setRotationX(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        rotationYField = new TextFieldWidget(this.textRenderer, centerX - 50, startY + 40, fieldWidth, fieldHeight, Text.literal("Y"));
        rotationYField.setText(String.format("%.1f", config.getFirstPerson().getRotationY()));
        rotationYField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setRotationY(value);
                config.getThirdPerson().setRotationY(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        rotationZField = new TextFieldWidget(this.textRenderer, centerX + 50, startY + 40, fieldWidth, fieldHeight, Text.literal("Z"));
        rotationZField.setText(String.format("%.1f", config.getFirstPerson().getRotationZ()));
        rotationZField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setRotationZ(value);
                config.getThirdPerson().setRotationZ(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        // 平移字段
        translateXField = new TextFieldWidget(this.textRenderer, centerX - 150, startY + 80, fieldWidth, fieldHeight, Text.literal("X"));
        translateXField.setText(String.format("%.1f", config.getFirstPerson().getTranslateX()));
        translateXField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setTranslateX(value);
                config.getThirdPerson().setTranslateX(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        translateYField = new TextFieldWidget(this.textRenderer, centerX - 50, startY + 80, fieldWidth, fieldHeight, Text.literal("Y"));
        translateYField.setText(String.format("%.1f", config.getFirstPerson().getTranslateY()));
        translateYField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setTranslateY(value);
                config.getThirdPerson().setTranslateY(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        translateZField = new TextFieldWidget(this.textRenderer, centerX + 50, startY + 80, fieldWidth, fieldHeight, Text.literal("Z"));
        translateZField.setText(String.format("%.1f", config.getFirstPerson().getTranslateZ()));
        translateZField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                config.getFirstPerson().setTranslateZ(value);
                config.getThirdPerson().setTranslateZ(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        // 添加输入框和滑块到GUI
        this.addDrawableChild(scaleSlider);
        this.addDrawableChild(rotationXField);
        this.addDrawableChild(rotationYField);
        this.addDrawableChild(rotationZField);
        this.addDrawableChild(translateXField);
        this.addDrawableChild(translateYField);
        this.addDrawableChild(translateZField);
        
        // 按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("保存"), button -> {
            configManager.saveConfig(config.getItemId());
            this.close();
        }).dimensions(centerX - 100, this.height - 40, 200, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("返回"), button -> {
            this.close();
        }).dimensions(centerX - 100, this.height - 70, 200, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制物品图标
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        ItemStack stack = new ItemStack(item);
        context.drawItem(stack, this.width / 2 - 10, 40);
        
        // 渲染控件
        scaleSlider.render(context, mouseX, mouseY, delta);
        rotationXField.render(context, mouseX, mouseY, delta);
        rotationYField.render(context, mouseX, mouseY, delta);
        rotationZField.render(context, mouseX, mouseY, delta);
        translateXField.render(context, mouseX, mouseY, delta);
        translateYField.render(context, mouseX, mouseY, delta);
        translateZField.render(context, mouseX, mouseY, delta);
        
        // 绘制标签 - 在控件后面渲染，确保显示在上层，调整Y坐标让标签更明显
        context.drawTextWithShadow(this.textRenderer, Text.literal("缩放:"), this.width / 2 - 100, 65, 0xFFFFFF);
        
        context.drawTextWithShadow(this.textRenderer, Text.literal("旋转 (度):"), this.width / 2 - 150, 105, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("X轴旋转:"), this.width / 2 - 150, 125, 0xFFFF00);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Y轴旋转:"), this.width / 2 - 50, 125, 0xFFFF00);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Z轴旋转:"), this.width / 2 + 50, 125, 0xFFFF00);
        
        context.drawTextWithShadow(this.textRenderer, Text.literal("平移:"), this.width / 2 - 150, 145, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("X轴平移:"), this.width / 2 - 150, 165, 0x00FFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Y轴平移:"), this.width / 2 - 50, 165, 0x00FFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Z轴平移:"), this.width / 2 + 50, 165, 0x00FFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }
}
