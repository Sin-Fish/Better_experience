package com.aeolyn.better_experience.inventory.gui;

import com.aeolyn.better_experience.client.gui.BaseConfigScreen;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * 背包整理配置界面
 * 继承BaseConfigScreen，保持架构统一性
 */
public class InventorySortConfigScreen extends BaseConfigScreen {
    
    private InventorySortConfig config;
    
    // 输入框
    private TextFieldWidget sortKeyField;
    private TextFieldWidget depositKeyField;
    private TextFieldWidget withdrawKeyField;
    private TextFieldWidget sortContainerKeyField;
    
    // 按钮
    private ButtonWidget sortModeButton;
    private ButtonWidget autoSortButton;
    private ButtonWidget showButtonsButton;
    private ButtonWidget showContainerButtonsButton;
    
    public InventorySortConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.literal("背包整理配置"), parentScreen, configManager);
    }
    
    // ==================== 抽象方法实现 ====================
    
    @Override
    protected void loadData() {
        // 暂时使用默认配置，后续会从ConfigManager获取
        config = new InventorySortConfig();
        LogUtil.info(LogUtil.MODULE_GUI, "加载背包整理配置");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        int centerX = getCenterX();
        int startY = 50;
        
        // 渲染标题
        context.drawCenteredTextWithShadow(this.textRenderer, "背包整理设置", centerX, startY, 0xFFFFFF);
        
        // 渲染说明
        context.drawTextWithShadow(this.textRenderer, "快捷键设置（留空表示不设置）:", centerX - 150, startY + 30, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, "排序模式: 按名称、按数量、按分类、按类型", centerX - 150, startY + 120, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, "其他设置: 自动整理、显示按钮等", centerX - 150, startY + 180, 0xCCCCCC);
    }
    
    @Override
    protected void onAddClicked() {
        // 保存配置
        saveConfig();
        LogUtil.info(LogUtil.MODULE_GUI, "保存背包整理配置");
    }
    
    // ==================== 自定义按钮 ====================
    
    @Override
    protected void addCustomButtons() {
        int centerX = getCenterX();
        int startY = 60;
        int buttonWidth = 120;
        int buttonHeight = 20;
        int spacing = 25;
        
        // 快捷键输入框
        sortKeyField = new TextFieldWidget(this.textRenderer, centerX - 150, startY, 60, buttonHeight, Text.literal("排序键"));
        sortKeyField.setText(config.getSortKey());
        sortKeyField.setChangedListener(text -> config.setSortKey(text));
        this.addDrawableChild(sortKeyField);
        
        depositKeyField = new TextFieldWidget(this.textRenderer, centerX - 80, startY, 60, buttonHeight, Text.literal("存入键"));
        depositKeyField.setText(config.getDepositKey());
        depositKeyField.setChangedListener(text -> config.setDepositKey(text));
        this.addDrawableChild(depositKeyField);
        
        withdrawKeyField = new TextFieldWidget(this.textRenderer, centerX - 10, startY, 60, buttonHeight, Text.literal("拿取键"));
        withdrawKeyField.setText(config.getWithdrawKey());
        withdrawKeyField.setChangedListener(text -> config.setWithdrawKey(text));
        this.addDrawableChild(withdrawKeyField);
        
        sortContainerKeyField = new TextFieldWidget(this.textRenderer, centerX + 60, startY, 60, buttonHeight, Text.literal("整理容器键"));
        sortContainerKeyField.setText(config.getSortContainerKey());
        sortContainerKeyField.setChangedListener(text -> config.setSortContainerKey(text));
        this.addDrawableChild(sortContainerKeyField);
        
        // 排序模式按钮
        sortModeButton = ButtonWidget.builder(
            Text.literal("排序模式: " + config.getDefaultSortMode().getDisplayName()),
            button -> cycleSortMode()
        ).dimensions(centerX - 150, startY + spacing, buttonWidth, buttonHeight).build();
        this.addDrawableChild(sortModeButton);
        
        // 自动整理按钮
        autoSortButton = ButtonWidget.builder(
            Text.literal("自动整理: " + (config.isAutoSortOnOpen() ? "开启" : "关闭")),
            button -> toggleAutoSort()
        ).dimensions(centerX - 20, startY + spacing, buttonWidth, buttonHeight).build();
        this.addDrawableChild(autoSortButton);
        
        // 显示按钮设置
        showButtonsButton = ButtonWidget.builder(
            Text.literal("显示按钮: " + (config.isShowSortButtons() ? "开启" : "关闭")),
            button -> toggleShowButtons()
        ).dimensions(centerX - 150, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addDrawableChild(showButtonsButton);
        
        showContainerButtonsButton = ButtonWidget.builder(
            Text.literal("容器按钮: " + (config.isShowContainerButtons() ? "开启" : "关闭")),
            button -> toggleShowContainerButtons()
        ).dimensions(centerX - 20, startY + spacing * 2, buttonWidth, buttonHeight).build();
        this.addDrawableChild(showContainerButtonsButton);
        
        // 测试按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("测试排序"),
            button -> testSort()
        ).dimensions(centerX - 150, startY + spacing * 3, buttonWidth, buttonHeight).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("重置配置"),
            button -> resetConfig()
        ).dimensions(centerX - 20, startY + spacing * 3, buttonWidth, buttonHeight).build());
    }
    
    @Override
    protected void setupScrollableList() {
        // 不需要滚动列表
    }
    
    // ==================== 私有方法 ====================
    
    private void cycleSortMode() {
        InventorySortConfig.SortMode[] modes = InventorySortConfig.SortMode.values();
        int currentIndex = config.getDefaultSortMode().ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        config.setDefaultSortMode(modes[nextIndex]);
        sortModeButton.setMessage(Text.literal("排序模式: " + config.getDefaultSortMode().getDisplayName()));
    }
    
    private void toggleAutoSort() {
        config.setAutoSortOnOpen(!config.isAutoSortOnOpen());
        autoSortButton.setMessage(Text.literal("自动整理: " + (config.isAutoSortOnOpen() ? "开启" : "关闭")));
    }
    
    private void toggleShowButtons() {
        config.setShowSortButtons(!config.isShowSortButtons());
        showButtonsButton.setMessage(Text.literal("显示按钮: " + (config.isShowSortButtons() ? "开启" : "关闭")));
    }
    
    private void toggleShowContainerButtons() {
        config.setShowContainerButtons(!config.isShowContainerButtons());
        showContainerButtonsButton.setMessage(Text.literal("容器按钮: " + (config.isShowContainerButtons() ? "开启" : "关闭")));
    }
    
    private void testSort() {
        // 测试排序功能
        LogUtil.info(LogUtil.MODULE_GUI, "测试排序功能");
        // 这里可以添加实际的测试逻辑
    }
    
    private void resetConfig() {
        config = new InventorySortConfig();
        updateUI();
        LogUtil.info(LogUtil.MODULE_GUI, "重置背包整理配置");
    }
    
    private void updateUI() {
        sortKeyField.setText(config.getSortKey());
        depositKeyField.setText(config.getDepositKey());
        withdrawKeyField.setText(config.getWithdrawKey());
        sortContainerKeyField.setText(config.getSortContainerKey());
        sortModeButton.setMessage(Text.literal("排序模式: " + config.getDefaultSortMode().getDisplayName()));
        autoSortButton.setMessage(Text.literal("自动整理: " + (config.isAutoSortOnOpen() ? "开启" : "关闭")));
        showButtonsButton.setMessage(Text.literal("显示按钮: " + (config.isShowSortButtons() ? "开启" : "关闭")));
        showContainerButtonsButton.setMessage(Text.literal("容器按钮: " + (config.isShowContainerButtons() ? "开启" : "关闭")));
    }
    
    private void saveConfig() {
        // 保存配置到ConfigManager
        LogUtil.info(LogUtil.MODULE_GUI, "保存背包整理配置");
        // 这里会调用ConfigManager的保存方法
    }
    
    @Override
    public String getScreenName() {
        return "InventorySortConfig";
    }
}
