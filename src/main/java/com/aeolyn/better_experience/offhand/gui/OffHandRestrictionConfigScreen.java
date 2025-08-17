package com.aeolyn.better_experience.offhand.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.offhand.gui.AddOffHandItemScreen;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.client.gui.BaseConfigScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 副手限制配置界面
 * 继承BaseConfigScreen，减少重复代码
 */
public class OffHandRestrictionConfigScreen extends BaseConfigScreen {
    
    private OffHandRestrictionConfig config;
    
    // 当前显示模式
    public enum DisplayMode {
        MAIN_MENU,
        WHITELIST
    }
    private DisplayMode currentMode = DisplayMode.MAIN_MENU;
    private List<ButtonWidget> listWidgets;
    
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
                saveData();
                LogUtil.info(LogUtil.MODULE_OFFHAND, "副手限制已{}", config.isEnabled() ? "启用" : "禁用");
            }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());
        
        // 白名单配置按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("白名单 ⚙"),
            button -> {
                currentMode = DisplayMode.WHITELIST;
                refreshScreen();
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());
    }
    
    private void renderMainMenu(DrawContext context) {
        // 绘制说明文字
        Text description = Text.translatable("better_experience.config.offhand_restrictions.description");
        renderCenteredText(context, description.getString(), 40, 0xAAAAAA);
    }
    
    // ==================== 白名单模式 ====================
    
    private void addWhitelistButtons() {
        // 添加返回按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.back"),
            button -> {
                currentMode = DisplayMode.MAIN_MENU;
                refreshScreen();
            }
        ).dimensions(getCenterX() - 100, this.height - 30, 200, 20).build());
        
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
        List<String> currentItems = getCurrentItems();
        setupScrollableList(currentItems);
        updateListWidgets();
    }
    
    private void renderWhitelistMode(DrawContext context) {
        // 绘制列表标题
        renderCenteredText(context, "副手白名单", 50, 0xFFFFFF);
        
        // 渲染物品列表
        renderItemList(context);
    }
    
    private void renderItemList(DrawContext context) {
        List<String> currentItems = getCurrentItems();
        renderScrollableList(context, currentItems, LIST_START_Y, LIST_END_Y);
    }
    
    @Override
    protected void renderListItem(DrawContext context, Object item, int y) {
        if (item instanceof String) {
            renderItemEntry(context, (String) item, y);
        }
    }
    
    private void renderItemEntry(DrawContext context, String itemId, int y) {
        // 渲染物品图标
        renderItemIcon(context, itemId, y);
        
        // 渲染物品名称
        renderItemName(context, itemId, y);
    }
    
    private void renderItemIcon(DrawContext context, String itemId, int y) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            // 正常显示
            context.drawItem(stack, getCenterX() - 100, y + 2);
        }
    }
    
    private void renderItemName(DrawContext context, String itemId, int y) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        String displayName = item != null ? item.getName().getString() : itemId;
        context.drawTextWithShadow(this.textRenderer, Text.literal(displayName), 
            getCenterX() - 70, y + 5, 0xFFFFFF);
    }
    
    private void addScrollButtons() {
        // 向上滚动按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↑"), button -> {
            if (scrollOffset > 0) {
                scrollOffset--;
                updateListWidgets();
            }
        }).dimensions(getCenterX() + 160, LIST_START_Y, 20, 20).build());
        
        // 向下滚动按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↓"), button -> {
            if (scrollOffset < maxScrollOffset) {
                scrollOffset++;
                updateListWidgets();
            }
        }).dimensions(getCenterX() + 160, LIST_END_Y - 20, 20, 20).build());
    }
    
    private void updateListWidgets() {
        // 清除现有的列表控件
        for (ButtonWidget widget : listWidgets) {
            this.remove(widget);
        }
        listWidgets.clear();
        
        // 添加可见的物品控件
        List<String> currentItems = getCurrentItems();
        int visibleItems = getVisibleItemCount();
        
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
        // 删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                removeItem(itemId);
            }
        ).dimensions(getCenterX() + 60, y, 20, 20).build();
        
        this.addDrawableChild(deleteButton);
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
    
    // ==================== 辅助方法 ====================
    
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
    
    private Text getToggleText(boolean enabled, String type) {
        String key = enabled ? "enabled" : "disabled";
        return Text.translatable("better_experience.config.offhand_restrictions." + type + "." + key);
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 获取当前模式，供添加物品界面使用
     */
    public DisplayMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * 刷新物品列表，供添加物品界面使用
     */
    public void refreshItemList() {
        if (currentMode == DisplayMode.WHITELIST) {
            updateListWidgets();
        }
    }
}
