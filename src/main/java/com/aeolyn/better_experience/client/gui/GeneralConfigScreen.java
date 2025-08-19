package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.ModConfig;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * 通用配置界面
 * 用于管理模组的开关和通用设置
 */
public class GeneralConfigScreen extends BaseConfigScreen {
    
    private ButtonWidget render3dToggleButton;
    private ButtonWidget offhandToggleButton;
    private ButtonWidget inventoryToggleButton;
    private ButtonWidget debugToggleButton;
    private ButtonWidget saveButton;
    private TextFieldWidget autoSaveIntervalField;
    
    private ModConfig modConfig;
    
    public GeneralConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.general.title"), parentScreen, configManager);
    }
    
    @Override
    protected void loadData() {
        modConfig = configManager.getModConfig();
        LogUtil.info(LogUtil.MODULE_GUI, "加载通用配置");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        // 渲染标题
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("通用配置"), width / 2, 20, 0xFFFFFF);
        
        // 渲染说明
        context.drawTextWithShadow(textRenderer, 
            Text.literal("在这里可以管理各个模块的开关和通用设置"), 10, 40, 0xAAAAAA);
        
        // 渲染自动保存间隔标签
        context.drawTextWithShadow(textRenderer, 
            Text.literal("自动保存间隔（秒）:"), 50, 200, 0xFFFFFF);
    }
    
    @Override
    protected void onAddClicked() {
        // 通用配置界面不需要添加功能
    }
    
    @Override
    protected void addStandardButtons() {
        int centerX = getCenterX();
        int buttonWidth = getButtonWidth();
        int buttonHeight = getButtonHeight();
        
        // 返回按钮（保存并返回）
        backButton = ButtonWidget.builder(
            Text.translatable("better_experience.config.back"),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "back");
                saveConfig();
                this.close();
            }
        ).dimensions(centerX - buttonWidth - 10, this.height - 30, buttonWidth, buttonHeight).build();
        this.addDrawableChild(backButton);
        
        // 保存按钮
        saveButton = ButtonWidget.builder(
            Text.translatable("better_experience.config.save"),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "save");
                saveConfig();
            }
        ).dimensions(centerX + 10, this.height - 30, buttonWidth, buttonHeight).build();
        this.addDrawableChild(saveButton);
    }
    
    @Override
    protected void addCustomButtons() {
        int startX = 50;
        int startY = 80;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        
        // 3D渲染模块开关
        render3dToggleButton = ButtonWidget.builder(
            Text.literal("3D渲染模块: " + (modConfig.isRender3dEnabled() ? "启用" : "禁用")),
            button -> {
                modConfig.setRender3dEnabled(!modConfig.isRender3dEnabled());
                updateToggleButtonText(render3dToggleButton, "3D渲染模块", modConfig.isRender3dEnabled());
                LogUtil.logButtonClick(getScreenName(), "toggle_render3d");
            }
        ).dimensions(startX, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(render3dToggleButton);
        
        // 副手限制模块开关
        offhandToggleButton = ButtonWidget.builder(
            Text.literal("副手限制模块: " + (modConfig.isOffhandRestrictionEnabled() ? "启用" : "禁用")),
            button -> {
                modConfig.setOffhandRestrictionEnabled(!modConfig.isOffhandRestrictionEnabled());
                updateToggleButtonText(offhandToggleButton, "副手限制模块", modConfig.isOffhandRestrictionEnabled());
                LogUtil.logButtonClick(getScreenName(), "toggle_offhand");
            }
        ).dimensions(startX, startY + spacing, buttonWidth, buttonHeight).build();
        this.addDrawableChild(offhandToggleButton);
        
        // 背包排序模块开关
        inventoryToggleButton = ButtonWidget.builder(
            Text.literal("背包排序模块: " + (modConfig.isInventorySortEnabled() ? "启用" : "禁用")),
            button -> {
                modConfig.setInventorySortEnabled(!modConfig.isInventorySortEnabled());
                updateToggleButtonText(inventoryToggleButton, "背包排序模块", modConfig.isInventorySortEnabled());
                LogUtil.logButtonClick(getScreenName(), "toggle_inventory");
            }
        ).dimensions(startX, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addDrawableChild(inventoryToggleButton);
        
        // 调试模式开关
        debugToggleButton = ButtonWidget.builder(
            Text.literal("调试模式: " + (modConfig.isDebugMode() ? "启用" : "禁用")),
            button -> {
                modConfig.setDebugMode(!modConfig.isDebugMode());
                updateToggleButtonText(debugToggleButton, "调试模式", modConfig.isDebugMode());
                LogUtil.logButtonClick(getScreenName(), "toggle_debug");
            }
        ).dimensions(startX, startY + spacing * 3, buttonWidth, buttonHeight).build();
        this.addDrawableChild(debugToggleButton);
        
        // 自动保存间隔输入框
        autoSaveIntervalField = new TextFieldWidget(textRenderer, startX, startY + spacing * 4 + 5, 100, 20, 
            Text.literal("自动保存间隔"));
        autoSaveIntervalField.setText(String.valueOf(modConfig.getAutoSaveInterval()));
        autoSaveIntervalField.setChangedListener(text -> {
            try {
                int interval = Integer.parseInt(text);
                if (interval >= 0) {
                    modConfig.setAutoSaveInterval(interval);
                }
            } catch (NumberFormatException e) {
                // 忽略无效输入
            }
        });
        this.addDrawableChild(autoSaveIntervalField);
        
        // 自动保存间隔标签 - 在renderCustomContent中渲染
    }
    
    private void updateToggleButtonText(ButtonWidget button, String prefix, boolean enabled) {
        button.setMessage(Text.literal(prefix + ": " + (enabled ? "启用" : "禁用")));
    }
    
    private void saveConfig() {
        try {
            // 保存通用配置
            configManager.updateModConfig(modConfig);
            
            // 显示保存成功消息
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("[Better Experience] 通用配置保存成功！"), false);
            }
            
            LogUtil.logSuccess(LogUtil.MODULE_GUI, "通用配置保存成功");
            
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_GUI, "保存通用配置失败: {}", e.getMessage(), e);
            
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("[Better Experience] 配置保存失败！"), false);
            }
        }
    }
    
    @Override
    public String getScreenName() {
        return "GeneralConfigScreen";
    }
}
