package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.offhand.gui.OffHandRestrictionConfigScreen;
import com.aeolyn.better_experience.render3d.gui.Render3DConfigScreen;
import com.aeolyn.better_experience.importexport.gui.ConfigImportExportScreen;
import com.aeolyn.better_experience.inventory.gui.InventorySortConfigScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * 统一配置入口界面
 * 提供各个功能模块的配置入口
 */
public class ModConfigScreen extends BaseConfigScreen {
    
    public ModConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.title"), parentScreen, configManager);
    }
    
    // ==================== 抽象方法实现 ====================
    
    @Override
    protected void loadData() {
        // 入口界面不需要加载数据
        LogUtil.info(LogUtil.MODULE_GUI, "打开配置入口界面");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        // 渲染说明文字
        renderDescription(context);
    }
    
    @Override
    protected void onAddClicked() {
        // 入口界面不需要添加功能
    }
    
    // ==================== 自定义按钮 ====================
    
    @Override
    protected void addStandardButtons() {
        // 主界面只显示关闭按钮，不显示返回按钮
        int centerX = getCenterX();
        int buttonWidth = getButtonWidth();
        int buttonHeight = getButtonHeight();
        
        // 关闭按钮
        closeButton = ButtonWidget.builder(
            Text.translatable("better_experience.config.close"),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "close");
                this.close();
            }
        ).dimensions(centerX - buttonWidth / 2, this.height - 30, buttonWidth, buttonHeight).build();
        this.addDrawableChild(closeButton);
    }
    
    @Override
    protected void addCustomButtons() {
        int centerX = getCenterX();
        int startY = 100;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        
        // 3D渲染配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("3D渲染配置"),
            button -> {
                this.client.setScreen(new Render3DConfigScreen(this, configManager));
                LogUtil.logGuiAction("open_3d_config", getScreenName(), "打开3D渲染配置界面");
            }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());
        
        // 副手限制配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("副手限制配置"),
            button -> {
                this.client.setScreen(new OffHandRestrictionConfigScreen(this, configManager));
                LogUtil.logGuiAction("open_offhand_config", getScreenName(), "打开副手限制配置界面");
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());
        
        // 背包整理配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("背包整理配置"),
            button -> {
                this.client.setScreen(new InventorySortConfigScreen(this, configManager));
                LogUtil.logGuiAction("open_inventory_config", getScreenName(), "打开背包整理配置界面");
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());
        
        // 导入导出配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("导入导出配置"),
            button -> {
                this.client.setScreen(new ConfigImportExportScreen(this, configManager));
                LogUtil.logGuiAction("open_config_export", getScreenName(), "打开配置导出对话框");
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());
    }
    
    @Override
    protected void setupScrollableList() {
        // 入口界面不需要滚动列表
    }
    
    // ==================== 渲染方法 ====================
    
    private void renderDescription(DrawContext context) {
        renderCenteredText(context, "Better Experience Mod 配置中心", 40, 0xFFFFFF);
        renderCenteredText(context, "选择要配置的功能模块", 60, 0xCCCCCC);
        renderCenteredText(context, "3D渲染配置 - 管理物品的3D渲染效果", 80, 0xAAAAAA);
        renderCenteredText(context, "副手限制配置 - 管理副手物品使用限制", 100, 0xAAAAAA);
        renderCenteredText(context, "导入导出配置 - 备份和恢复配置", 120, 0xAAAAAA);
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 获取界面名称
     */
    @Override
    protected String getScreenName() {
        return "ModConfigScreen";
    }
    
    /**
     * 刷新物品列表（为了兼容性）
     */
    public void refreshItemList() {
        // 入口界面不需要刷新物品列表
        LogUtil.info(LogUtil.MODULE_GUI, "入口界面刷新物品列表（无操作）");
    }
}
