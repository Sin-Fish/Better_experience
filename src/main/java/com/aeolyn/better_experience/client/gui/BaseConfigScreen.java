package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一配置界面基类
 * 提供标准的界面布局和交互模式
 */
public abstract class BaseConfigScreen extends Screen {
    
    protected final Screen parentScreen;
    protected final ConfigManager configManager;
    
    // 通用滚动列表支持
    protected int scrollOffset = 0;
    protected int maxScrollOffset = 0;
    protected static final int ITEM_HEIGHT = 30;
    protected static final int LIST_START_Y = 50;
    protected static final int LIST_END_Y = 200;
    protected static final int LIST_WIDTH = 300;
    
    // 通用按钮
   
    protected ButtonWidget backButton;
    protected ButtonWidget closeButton;
    protected ButtonWidget addButton;
    protected ButtonWidget refreshButton;
    
    // 状态管理
    protected boolean isLoading = false;
    protected String errorMessage = null;
    protected String infoMessage = null;
    
    // 确认对话框
    protected String confirmTitle = null;
    protected String confirmMessage = null;
    protected Runnable confirmCallback = null;
    
    public BaseConfigScreen(Text title, Screen parentScreen, ConfigManager configManager) {
        super(title);
        this.parentScreen = parentScreen;
        this.configManager = configManager;
    }
    
    // ==================== 抽象方法 ====================
    
    /**
     * 加载数据
     */
    protected abstract void loadData();
    
    /**
     * 渲染自定义内容
     */
    protected abstract void renderCustomContent(DrawContext context);
    
    /**
     * 添加按钮点击处理
     */
    protected abstract void onAddClicked();
        
    // ==================== 初始化方法 ====================
    
    @Override
    protected void init() {
        super.init();
        
        // 记录界面打开
        LogUtil.logScreenOpen(getScreenName());
        
        // 加载数据
        loadData();
        
        // 添加标准按钮
        addStandardButtons();
        
        // 添加自定义按钮
        addCustomButtons();
        
        // 设置滚动列表
        setupScrollableList();
    }
    
    /**
     * 添加标准按钮
     */
    protected void addStandardButtons() {
        int centerX = getCenterX();
        int smallWidth = 100;
        int buttonHeight = getButtonHeight();
        int y = this.height - 26;

        // 返回按钮（左侧）
        backButton = ButtonWidget.builder(
            Text.translatable("better_experience.config.back"),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "back");
                saveConfig();
                this.client.setScreen(parentScreen);
            }
        ).dimensions(centerX - smallWidth - 5, y, smallWidth, buttonHeight).build();
        this.addDrawableChild(backButton);

        // 关闭按钮（右侧）
        closeButton = ButtonWidget.builder(
            Text.translatable("better_experience.config.close"),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "close");
                saveConfig();
                this.close();
            }
        ).dimensions(centerX + 5, y, smallWidth, buttonHeight).build();
        this.addDrawableChild(closeButton);
    }
    
    /**
     * 添加自定义按钮（子类可以重写）
     */
    protected void addCustomButtons() {
        // 默认实现为空，子类可以重写添加自定义按钮
    }
    
    /**
     * 设置滚动列表（子类可以重写）
     */
    protected void setupScrollableList() {
        // 默认实现为空，子类可以重写设置滚动列表
    }
    
    /**
     * 保存配置（子类可以重写）
     */
    protected void saveConfig() {
        // 默认实现为空，子类可以重写实现具体的保存逻辑
    }
    
    // ==================== 渲染方法 ====================
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景
        renderStandardBackground(context);
        
        // 绘制标题
        renderStandardTitle(context);
        
        // 绘制状态信息
        renderStatusInfo(context);
        
        // 绘制自定义内容
        renderCustomContent(context);
        
        // 绘制确认对话框
        renderConfirmDialog(context);
        
        // 绘制标准按钮
        renderStandardButtons(context);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    /**
     * 渲染标准背景
     */
    protected void renderStandardBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }
    
    /**
     * 渲染标准标题
     */
    protected void renderStandardTitle(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
    
    /**
     * 渲染状态信息
     */
    protected void renderStatusInfo(DrawContext context) {
        int y = 40;
        
        // 显示加载状态
        if (isLoading) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("加载中..."), this.width / 2, y, 0xFFFF00);
            y += 15;
        }
        
        // 显示错误信息
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("错误: " + errorMessage), this.width / 2, y, 0xFF0000);
            y += 15;
        }
        
        // 显示信息消息
        if (infoMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(infoMessage), this.width / 2, y, 0x00FF00);
        }
    }
    
    /**
     * 渲染标准按钮
     */
    protected void renderStandardButtons(DrawContext context) {
        // 按钮的渲染由Minecraft自动处理
    }
    
    /**
     * 渲染确认对话框
     */
    protected void renderConfirmDialog(DrawContext context) {
        if (confirmTitle != null && confirmMessage != null) {
            // 绘制半透明背景
            context.fill(0, 0, this.width, this.height, 0x80000000);
            
            // 绘制对话框背景
            int dialogWidth = 300;
            int dialogHeight = 100;
            int dialogX = (this.width - dialogWidth) / 2;
            int dialogY = (this.height - dialogHeight) / 2;
            
            context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xFF202020);
            context.drawBorder(dialogX, dialogY, dialogWidth, dialogHeight, 0xFFFFFFFF);
            
            // 绘制标题
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(confirmTitle), this.width / 2, dialogY + 10, 0xFFFFFF);
            
            // 绘制消息
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(confirmMessage), this.width / 2, dialogY + 30, 0xCCCCCC);
        }
    }
    
    // ==================== 滚动列表方法 ====================
    
    /**
     * 设置滚动列表
     */
    protected void setupScrollableList(List<?> items) {
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, items.size() - visibleItems);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);
    }
    
    /**
     * 渲染滚动列表
     */
    protected void renderScrollableList(DrawContext context, List<?> items, int startY, int endY) {
        int visibleItems = (endY - startY) / ITEM_HEIGHT;
        
        for (int i = 0; i < visibleItems && i + scrollOffset < items.size(); i++) {
            Object item = items.get(i + scrollOffset);
            int y = startY + i * ITEM_HEIGHT;
            renderListItem(context, item, y);
        }
        
        // 绘制滚动条
        if (maxScrollOffset > 0) {
            renderScrollBar(context, startY, endY);
        }
    }
    
    /**
     * 渲染列表项（子类需要实现）
     */
    protected void renderListItem(DrawContext context, Object item, int y) {
        // 默认实现，子类可以重写
        context.drawTextWithShadow(this.textRenderer, Text.literal(item.toString()), LIST_START_Y, y + 5, 0xFFFFFF);
    }
    
    /**
     * 渲染滚动条
     */
    protected void renderScrollBar(DrawContext context, int startY, int endY) {
        int scrollBarWidth = 6;
        int scrollBarX = this.width / 2 + LIST_WIDTH / 2 + 5;
        
        // 滚动条背景
        context.fill(scrollBarX, startY, scrollBarX + scrollBarWidth, endY, 0x80000000);
        
        // 滚动条滑块
        int scrollBarHeight = endY - startY;
        int sliderHeight = Math.max(20, scrollBarHeight / (maxScrollOffset + 1));
        int sliderY = startY + (scrollOffset * (scrollBarHeight - sliderHeight) / maxScrollOffset);
        
        context.fill(scrollBarX, sliderY, scrollBarX + scrollBarWidth, sliderY + sliderHeight, 0xFF808080);
    }
    
    /**
     * 处理滚动输入
     */
    protected void handleScrollInput(double mouseX, double mouseY, double amount) {
        if (isMouseInScrollArea((int) mouseX, (int) mouseY)) {
            scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset - (int) amount));
        }
    }
    
    /**
     * 检查鼠标是否在滚动区域
     */
    protected boolean isMouseInScrollArea(int mouseX, int mouseY) {
        return mouseX >= this.width / 2 - LIST_WIDTH / 2 && 
               mouseX <= this.width / 2 + LIST_WIDTH / 2 &&
               mouseY >= LIST_START_Y && mouseY <= LIST_END_Y;
    }
    
    // ==================== 用户交互方法 ====================
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        handleScrollInput(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC键关闭确认对话框
        if (keyCode == 256 && confirmTitle != null) { // ESC键
            clearConfirmDialog();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    // ==================== 对话框方法 ====================
    
    /**
     * 显示确认对话框
     */
    protected void showConfirmDialog(String title, String message, Runnable onConfirm) {
        this.confirmTitle = title;
        this.confirmMessage = message;
        this.confirmCallback = onConfirm;
    }
    
    /**
     * 显示错误对话框
     */
    protected void showErrorDialog(String title, String message) {
        showConfirmDialog(title, message, null);
    }
    
    /**
     * 显示信息对话框
     */
    protected void showInfoDialog(String title, String message) {
        showConfirmDialog(title, message, null);
    }
    
    /**
     * 清除确认对话框
     */
    protected void clearConfirmDialog() {
        this.confirmTitle = null;
        this.confirmMessage = null;
        this.confirmCallback = null;
    }
    
    /**
     * 确认对话框回调
     */
    protected void onConfirmDialog() {
        if (confirmCallback != null) {
            confirmCallback.run();
        }
        clearConfirmDialog();
    }
    
    // ==================== 状态管理方法 ====================
    
    /**
     * 设置加载状态
     */
    protected void setLoading(boolean loading) {
        this.isLoading = loading;
    }
    
    /**
     * 设置错误信息
     */
    protected void setError(String error) {
        this.errorMessage = error;
        if (error != null) {
            LogUtil.error(LogUtil.MODULE_GUI, "界面错误: {} | 界面: {}", error, getScreenName());
        }
    }
    
    /**
     * 清除错误信息
     */
    protected void clearError() {
        this.errorMessage = null;
    }
    
    /**
     * 设置信息消息
     */
    protected void setInfo(String info) {
        this.infoMessage = info;
    }
    
    /**
     * 清除信息消息
     */
    protected void clearInfo() {
        this.infoMessage = null;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取界面名称
     */
    protected String getScreenName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取中心X坐标
     */
    protected int getCenterX() {
        return this.width / 2;
    }
    
    /**
     * 获取中心Y坐标
     */
    protected int getCenterY() {
        return this.height / 2;
    }
    
    /**
     * 获取按钮宽度
     */
    protected int getButtonWidth() {
        return 200;
    }
    
    /**
     * 获取按钮高度
     */
    protected int getButtonHeight() {
        return 20;
    }
    
    /**
     * 获取标准间距
     */
    protected int getStandardSpacing() {
        return 30;
    }
    
    /**
     * 获取可见项目数量
     */
    protected int getVisibleItemCount() {
        return (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
    }
    
    // ==================== 文本渲染方法 ====================
    
    /**
     * 渲染居中文本
     */
    protected void renderCenteredText(DrawContext context, Text text, int y) {
        context.drawCenteredTextWithShadow(this.textRenderer, text, this.width / 2, y, 0xFFFFFF);
    }
    
    /**
     * 渲染居中文本
     */
    protected void renderCenteredText(DrawContext context, String text, int y, int color) {
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(text), this.width / 2, y, color);
    }
    
    /**
     * 渲染换行文本
     */
    protected void renderWrappedText(DrawContext context, String text, int x, int y, int maxWidth) {
        List<net.minecraft.text.OrderedText> lines = this.textRenderer.wrapLines(Text.literal(text), maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, lines.get(i), x, y + i * 12, 0xFFFFFF);
        }
    }
    
    // ==================== 生命周期方法 ====================
    
    @Override
    public void close() {
        LogUtil.logScreenClose(getScreenName());
        super.close();
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // ==================== 事件处理方法 ====================
    
    /**
     * 界面打开事件
     */
    protected void onScreenOpen() {
        LogUtil.logScreenOpen(getScreenName());
    }
    
    /**
     * 界面关闭事件
     */
    protected void onScreenClose() {
        LogUtil.logScreenClose(getScreenName());
    }
    
    /**
     * 数据变化事件
     */
    protected void onDataChanged() {
        LogUtil.logGuiAction("data_changed", getScreenName(), "数据已更新");
    }
}
