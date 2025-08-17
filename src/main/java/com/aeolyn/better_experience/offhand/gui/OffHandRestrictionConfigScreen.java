package com.aeolyn.better_experience.offhand.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.offhand.gui.AddOffHandItemScreen;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.client.gui.BaseConfigScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 副手限制配置界面
 * 继承BaseConfigScreen，减少重复代码
 */
public class OffHandRestrictionConfigScreen extends BaseConfigScreen {
    
    private OffHandRestrictionConfig config;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 25; 
    private static final int LIST_START_Y = 70;
    private static final int LIST_END_Y = 200;
    
    // 当前显示模式
    public enum DisplayMode {
        MAIN_MENU,
        WHITELIST
    }
    private DisplayMode currentMode = DisplayMode.MAIN_MENU;
    private List<ClickableWidget> listWidgets;
    
    public OffHandRestrictionConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.offhand_restrictions.title"), parentScreen, configManager);
        this.listWidgets = new ArrayList<>();
    }
    
    // ==================== 抽象方法实现 ====================
    
    @Override
    protected void loadData() {
        // 重新加载配置以确保获取最新数据
        this.config = configManager.getOffHandRestrictionConfig();
    }
    
    @Override
    protected void saveData() {
        configManager.updateOffHandRestrictionConfig(config);
        configManager.saveOffHandRestrictionConfig();
        setInfo("副手限制配置已保存");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        if (currentMode == DisplayMode.MAIN_MENU) {
            renderMainMenu(context);
        } else {
            renderWhitelistMode(context);
        }
    }
    
    @Override
    protected void onAddClicked() {
        this.client.setScreen(new AddOffHandItemScreen(this, configManager));
    }
    
    @Override
    protected void onSaveClicked() {
        saveData();
        showInfoDialog("成功", "副手限制配置已保存");
    }
    
    // ==================== 自定义按钮 ====================
    
    @Override
    protected void addCustomButtons() {
        if (currentMode == DisplayMode.MAIN_MENU) {
            addMainMenuButtons();
        } else {
            addWhitelistButtons();
        }
    }
    
    @Override
    protected void setupScrollableList() {
        if (currentMode == DisplayMode.WHITELIST) {
            setupWhitelistScroll();
        }
    }
    
    // ==================== 重写标准按钮 ====================
    
    @Override
    protected void addStandardButtons() {
        // 使用BaseConfigScreen的默认实现，包含保存、返回和关闭按钮
        super.addStandardButtons();
    }
    
    // ==================== 主菜单模式 ====================
    
    private void addMainMenuButtons() {
        int centerX = getCenterX();
        int startY = getCenterY() - 60;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        
        // 副手限制开关
        this.addDrawableChild(ButtonWidget.builder(
            getToggleText(config.isEnabled(), "offhand_restriction"),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(getToggleText(config.isEnabled(), "offhand_restriction"));
                // 立即保存配置
                configManager.updateOffHandRestrictionConfig(config);
                configManager.saveOffHandRestrictionConfig();
                LogUtil.info(LogUtil.MODULE_OFFHAND, "副手限制{}", config.isEnabled() ? "启用" : "禁用");
            }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());
        
        // 白名单配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("白名单配置"),
            button -> {
                currentMode = DisplayMode.WHITELIST;
                refreshScreen();
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());
    }
    
    private void addWhitelistButtons() {
        // 添加新建物品按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("+ 添加物品"),
            button -> {
                this.client.setScreen(new AddOffHandItemScreen(this, configManager));
            }
        ).dimensions(getCenterX() - 100, LIST_END_Y + 10, 200, 20).build());
        
        // 添加滚动按钮
        addScrollButtons();
    }
    
    private void setupWhitelistScroll() {
        // 计算最大滚动偏移量
        List<String> currentItems = getCurrentItems();
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, currentItems.size() - visibleItems);
        
        updateListWidgets();
    }
    
    private void addScrollButtons() {
        // 向上滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↑"),
            button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    updateListWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_START_Y, 20, 20).build());
        
        // 向下滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↓"),
            button -> {
                if (scrollOffset < maxScrollOffset) {
                    scrollOffset++;
                    updateListWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_END_Y - 20, 20, 20).build());
    }
    
    private void refreshScreen() {
        // 清除所有控件并重新初始化
        this.clearAndInit();
    }
    
    private List<String> getCurrentItems() {
        // 使用统一的白名单
        List<String> items = config.getAllowedItems();
        LogUtil.debug(LogUtil.MODULE_OFFHAND, "当前白名单物品数量: {}, 物品: {}", items.size(), items);
        return items;
    }
    
    private void updateListWidgets() {
        // 清除现有的列表控件
        for (ClickableWidget widget : listWidgets) {
            this.remove(widget);
        }
        listWidgets.clear();
        
        // 添加可见的物品控件
        List<String> currentItems = getCurrentItems();
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        
        // 计算最大滚动偏移量
        maxScrollOffset = Math.max(0, currentItems.size() - visibleItems);
        
        // 确保滚动偏移量在有效范围内
        if (scrollOffset > maxScrollOffset) {
            scrollOffset = maxScrollOffset;
        }
        
        for (int i = 0; i < visibleItems && i + scrollOffset < currentItems.size(); i++) {
            String itemId = currentItems.get(i + scrollOffset);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            addListItem(itemId, y);
        }
    }
    
    private void addListItem(String itemId, int y) {
        // 物品图标按钮
        ItemIconButton iconButton = new ItemIconButton(
            getCenterX() - 100, y, 20, 20,
            itemId, true, // 白名单中的物品都是启用的
            button -> {
                // 点击物品图标可以显示物品信息或进行其他操作
                Item item = Registries.ITEM.get(Identifier.of(itemId));
                String displayName = item != null ? item.getName().getString() : itemId;
                showInfoDialog("物品信息", "物品ID: " + itemId + "\n显示名称: " + displayName);
                LogUtil.logGuiAction("click_offhand_item", getScreenName(), 
                    Map.of("itemId", itemId, "action", "info"));
            }
        );
        
        // 获取物品的实际显示名称
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        String displayName = item != null ? item.getName().getString() : itemId;
        
        // 物品名称按钮
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal(displayName),
            button -> {
                // 可以在这里添加点击物品的详细配置
            }
        ).dimensions(getCenterX() - 70, y, 120, 20).build();
        
        // 删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                removeItem(itemId);
            }
        ).dimensions(getCenterX() + 60, y, 20, 20).build();
        
        this.addDrawableChild(iconButton);
        this.addDrawableChild(nameButton);
        this.addDrawableChild(deleteButton);
        listWidgets.add(iconButton);
        listWidgets.add(nameButton);
        listWidgets.add(deleteButton);
    }
    
    private void removeItem(String itemId) {
        // 使用配置类中的方法移除物品
        config.removeAllowedItem(itemId);
        
        // 保存配置
        try {
            saveData();
            LogUtil.info(LogUtil.MODULE_OFFHAND, "移除物品: {}", itemId);
            
            // 更新界面
            updateListWidgets();
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_OFFHAND, "保存副手限制配置失败: {}", e.getMessage(), e);
        }
    }
    
    // ==================== 渲染方法 ====================
    
    private void renderMainMenu(DrawContext context) {
        renderCenteredText(context, "副手限制配置", 40, 0xFFFFFF);
        renderCenteredText(context, "管理副手物品使用限制", 60, 0xCCCCCC);
    }
    
    private void renderWhitelistMode(DrawContext context) {
        // 渲染列表背景
        context.fill(getCenterX() - 160, LIST_START_Y - 5, getCenterX() + 180, LIST_END_Y + 5, 0x44000000);
        
        // 渲染滚动信息
        if (maxScrollOffset > 0) {
            String scrollInfo = String.format("滚动: %d/%d", scrollOffset + 1, maxScrollOffset + 1);
            context.drawTextWithShadow(this.textRenderer, Text.literal(scrollInfo),
                getCenterX() + 160, LIST_START_Y + 10, 0xFFFFFF);
        }
        
        renderCenteredText(context, "白名单配置", 40, 0xFFFFFF);
        renderCenteredText(context, "管理允许在副手使用的物品", 60, 0xCCCCCC);
    }
    
    // ==================== 鼠标滚动处理 ====================
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentMode == DisplayMode.WHITELIST &&
            mouseX >= getCenterX() - 160 && mouseX <= getCenterX() + 180 &&
            mouseY >= LIST_START_Y && mouseY <= LIST_END_Y) {
            if (verticalAmount > 0 && scrollOffset > 0) {
                scrollOffset--;
                updateListWidgets();
                return true;
            } else if (verticalAmount < 0 && scrollOffset < maxScrollOffset) {
                scrollOffset++;
                updateListWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    // ==================== 辅助方法 ====================
    
    private Text getToggleText(boolean enabled, String key) {
        if ("offhand_restriction".equals(key)) {
            return Text.literal(enabled ? "✓ 副手限制" : "✗ 副手限制");
        }
        return Text.literal(enabled ? "✓ " + key : "✗ " + key);
    }
    
    /**
     * 获取界面名称
     */
    @Override
    protected String getScreenName() {
        return "OffHandRestrictionConfigScreen";
    }
    
    // ==================== 自定义物品图标按钮类 ====================
    
    private static class ItemIconButton extends ButtonWidget {
        private final String itemId;
        private boolean enabled;
        
        public ItemIconButton(int x, int y, int width, int height, String itemId, boolean enabled, PressAction onPress) {
            super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.itemId = itemId;
            this.enabled = enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // 绘制按钮背景
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x44000000);
            
            // 绘制物品图标
            Item item = Registries.ITEM.get(Identifier.of(itemId));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                // 根据启用状态设置不同的渲染参数
                if (enabled) {
                    // 正常显示
                    context.drawItem(stack, this.getX() + 2, this.getY() + 2);
                } else {
                    // 变暗显示
                    context.drawItem(stack, this.getX() + 2, this.getY() + 2, 0x88888888);
                }
            }
            
            // 绘制边框
            if (this.isHovered()) {
                context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x44FFFFFF);
            }
            
            // 绘制状态指示器（小圆点）
            if (enabled) {
                context.fill(this.getX() + this.width - 4, this.getY() + 2, this.getX() + this.width - 2, this.getY() + 4, 0xFF00FF00); // 绿色
            } else {
                context.fill(this.getX() + this.width - 4, this.getY() + 2, this.getX() + this.width - 2, this.getY() + 4, 0xFFFF0000); // 红色
            }
        }
    }
}
