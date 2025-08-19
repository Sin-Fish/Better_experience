package com.aeolyn.better_experience.inventory.gui;

import com.aeolyn.better_experience.client.gui.BaseConfigScreen;
import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * 背包整理配置界面
 * 继承BaseConfigScreen，保持架构统一性
 */
public class InventorySortConfigScreen extends BaseConfigScreen {
    
    private InventorySortConfig config;
    
    // 按钮
    private ButtonWidget sortModeButton;
    private ButtonWidget smartTransferLogicButton;
    
    public InventorySortConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.literal("背包增强配置"), parentScreen, configManager);
    }
    
    // ==================== 抽象方法实现 ====================
    
    @Override
    protected void loadData() {
        // 从ConfigManager获取配置
        try {
            config = configManager.getConfig(InventorySortConfig.class);
            if (config == null) {
                config = new InventorySortConfig();
            }
        } catch (Exception e) {
            LogUtil.warn(LogUtil.MODULE_GUI, "加载配置失败，使用默认配置: " + e.getMessage());
            config = new InventorySortConfig();
        }
        LogUtil.info(LogUtil.MODULE_GUI, "加载背包整理配置");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        int centerX = getCenterX();
        int startY = 50;
        
        // 渲染标题
        context.drawCenteredTextWithShadow(this.textRenderer, "背包增强设置", centerX, startY, 0xFFFFFF);
        
        // 渲染说明
        context.drawTextWithShadow(this.textRenderer, "排序模式: 按名称、按数量、按类型", centerX - 150, startY + 80, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, "智能转移逻辑: 根据鼠标位置或空位数量", centerX - 150, startY + 110, 0xCCCCCC);
    }
    
    @Override
    protected void onAddClicked() {
        // 保存配置
        saveConfig();
        LogUtil.info(LogUtil.MODULE_GUI, "保存背包增强配置");
    }
    
    // ==================== 自定义按钮 ====================
    
    @Override
    protected void addCustomButtons() {
        int centerX = getCenterX();
        int startY = 100;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        
        // 排序模式按钮（居中）
        sortModeButton = ButtonWidget.builder(
            Text.literal("排序模式: " + config.getDefaultSortMode().getDisplayName()),
            button -> cycleSortMode()
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(sortModeButton);
        
        // 智能转移逻辑按钮（居中）
        smartTransferLogicButton = ButtonWidget.builder(
            Text.literal("智能转移: " + config.getSmartTransferLogic().getDisplayName()),
            button -> cycleSmartTransferLogic()
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build();
        this.addDrawableChild(smartTransferLogicButton);
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
    
    private void cycleSmartTransferLogic() {
        InventorySortConfig.SmartTransferLogic[] logics = InventorySortConfig.SmartTransferLogic.values();
        int currentIndex = config.getSmartTransferLogic().ordinal();
        int nextIndex = (currentIndex + 1) % logics.length;
        config.setSmartTransferLogic(logics[nextIndex]);
        smartTransferLogicButton.setMessage(Text.literal("智能转移: " + config.getSmartTransferLogic().getDisplayName()));
    }
    
    @Override
    protected void saveConfig() {
        // 保存配置到ConfigManager
        try {
            configManager.saveConfig(config);
            LogUtil.logSuccess(LogUtil.MODULE_GUI, "背包整理配置保存成功");
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_GUI, "保存背包整理配置失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String getScreenName() {
        return "InventorySortConfig";
    }
}
