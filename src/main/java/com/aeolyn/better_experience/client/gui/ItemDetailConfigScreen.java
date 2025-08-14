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
    
    // 视图模式切换
    private boolean isFirstPersonView = true;
    private ButtonWidget viewToggleButton;
    
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
        
        // 视图切换按钮
        viewToggleButton = ButtonWidget.builder(
            Text.literal(isFirstPersonView ? "第一人称" : "第三人称"),
            button -> {
                isFirstPersonView = !isFirstPersonView;
                button.setMessage(Text.literal(isFirstPersonView ? "第一人称" : "第三人称"));
                updateFieldsFromConfig();
            }
        ).dimensions(centerX - 100, 50, 200, 20).build();
        
        // 缩放滑块
        scaleSlider = new SliderWidget(centerX - 100, startY, 200, 20, 
            Text.literal("缩放: " + String.format("%.1f", getCurrentViewConfig().getScale())),
            getCurrentViewConfig().getScale() / 5.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("缩放: " + String.format("%.1f", this.value * 5.0)));
            }
            
            @Override
            protected void applyValue() {
                float scale = (float) (this.value * 5.0);
                getCurrentViewConfig().setScale(scale);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            }
        };
        
        // 旋转字段
        rotationXField = new TextFieldWidget(this.textRenderer, centerX - 150, startY + 40, fieldWidth, fieldHeight, Text.literal("X"));
        rotationXField.setText(String.format("%.1f", getCurrentViewConfig().getRotationX()));
        rotationXField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setRotationX(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        rotationYField = new TextFieldWidget(this.textRenderer, centerX - 50, startY + 40, fieldWidth, fieldHeight, Text.literal("Y"));
        rotationYField.setText(String.format("%.1f", getCurrentViewConfig().getRotationY()));
        rotationYField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setRotationY(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        rotationZField = new TextFieldWidget(this.textRenderer, centerX + 50, startY + 40, fieldWidth, fieldHeight, Text.literal("Z"));
        rotationZField.setText(String.format("%.1f", getCurrentViewConfig().getRotationZ()));
        rotationZField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setRotationZ(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        // 平移字段
        translateXField = new TextFieldWidget(this.textRenderer, centerX - 150, startY + 80, fieldWidth, fieldHeight, Text.literal("X"));
        translateXField.setText(String.format("%.1f", getCurrentViewConfig().getTranslateX()));
        translateXField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setTranslateX(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        translateYField = new TextFieldWidget(this.textRenderer, centerX - 50, startY + 80, fieldWidth, fieldHeight, Text.literal("Y"));
        translateYField.setText(String.format("%.1f", getCurrentViewConfig().getTranslateY()));
        translateYField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setTranslateY(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        translateZField = new TextFieldWidget(this.textRenderer, centerX + 50, startY + 80, fieldWidth, fieldHeight, Text.literal("Z"));
        translateZField.setText(String.format("%.1f", getCurrentViewConfig().getTranslateZ()));
        translateZField.setChangedListener(text -> {
            try {
                float value = Float.parseFloat(text);
                getCurrentViewConfig().setTranslateZ(value);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            } catch (NumberFormatException ignored) {}
        });
        
        // 添加所有控件到GUI
        this.addDrawableChild(viewToggleButton);
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
    
    /**
     * 获取当前视图的配置
     */
    private ItemConfig.RenderSettings getCurrentViewConfig() {
        return isFirstPersonView ? config.getFirstPerson() : config.getThirdPerson();
    }
    
    /**
     * 从当前配置更新字段值
     */
    private void updateFieldsFromConfig() {
        ItemConfig.RenderSettings viewConfig = getCurrentViewConfig();
        
        // 重新创建滑块以更新值
        scaleSlider = new SliderWidget(scaleSlider.getX(), scaleSlider.getY(), scaleSlider.getWidth(), scaleSlider.getHeight(),
            Text.literal("缩放: " + String.format("%.1f", viewConfig.getScale())),
            viewConfig.getScale() / 5.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("缩放: " + String.format("%.1f", this.value * 5.0)));
            }
            
            @Override
            protected void applyValue() {
                float scale = (float) (this.value * 5.0);
                getCurrentViewConfig().setScale(scale);
                // 立即保存到ConfigManager
                configManager.saveConfig(config.getItemId());
            }
        };
        
        // 更新输入框
        rotationXField.setText(String.format("%.1f", viewConfig.getRotationX()));
        rotationYField.setText(String.format("%.1f", viewConfig.getRotationY()));
        rotationZField.setText(String.format("%.1f", viewConfig.getRotationZ()));
        translateXField.setText(String.format("%.1f", viewConfig.getTranslateX()));
        translateYField.setText(String.format("%.1f", viewConfig.getTranslateY()));
        translateZField.setText(String.format("%.1f", viewConfig.getTranslateZ()));
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
        context.drawItem(stack, this.width / 2 - 10, 25);
        
        // 先绘制标签，确保在控件上方显示
        context.drawTextWithShadow(this.textRenderer, Text.literal("缩放:"), this.width / 2 - 100, 75, 0xFFFFFF);
        
        context.drawTextWithShadow(this.textRenderer, Text.literal("旋转 (度):"), this.width / 2 - 150, 115, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("X轴旋转:"), this.width / 2 - 150, 135, 0xFFFF00);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Y轴旋转:"), this.width / 2 - 50, 135, 0xFFFF00);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Z轴旋转:"), this.width / 2 + 50, 135, 0xFFFF00);
        
        context.drawTextWithShadow(this.textRenderer, Text.literal("平移:"), this.width / 2 - 150, 155, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("X轴平移:"), this.width / 2 - 150, 175, 0x00FFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Y轴平移:"), this.width / 2 - 50, 175, 0x00FFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Z轴平移:"), this.width / 2 + 50, 175, 0x00FFFF);
        
        // 渲染控件
        scaleSlider.render(context, mouseX, mouseY, delta);
        rotationXField.render(context, mouseX, mouseY, delta);
        rotationYField.render(context, mouseX, mouseY, delta);
        rotationZField.render(context, mouseX, mouseY, delta);
        translateXField.render(context, mouseX, mouseY, delta);
        translateYField.render(context, mouseX, mouseY, delta);
        translateZField.render(context, mouseX, mouseY, delta);
        
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
