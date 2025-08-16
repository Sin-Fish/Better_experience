package com.aeolyn.better_experience.offhand.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.offhand.config.OffHandRestrictionConfig;
import com.aeolyn.better_experience.offhand.gui.AddOffHandItemScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OffHandRestrictionConfigScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-OffHandConfig");
    
    private final Screen parentScreen;
    private final ConfigManager configManager;
    private OffHandRestrictionConfig config;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 25;
    private static final int LIST_START_Y = 80;
    private static final int LIST_END_Y = 200;
    private static final int LIST_WIDTH = 300;
    
    // 当前显示模式
    public enum DisplayMode {
        MAIN_MENU,
        WHITELIST
    }
    private DisplayMode currentMode = DisplayMode.MAIN_MENU;
    private List<ClickableWidget> listWidgets;
    
    public OffHandRestrictionConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.offhand_restrictions.title"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
        // 重新加载配置以确保获取最新数据
        this.config = configManager.getOffHandRestrictionConfig();
        this.listWidgets = new ArrayList<>();
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (currentMode == DisplayMode.MAIN_MENU) {
            initMainMenu();
        } else {
            initWhitelistMode();
        }
    }
    
    private void initMainMenu() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 30;
        int gearButtonSize = 20;
        
        // 副手限制开关
        this.addDrawableChild(ButtonWidget.builder(
            getToggleText(config.isEnabled(), "offhand_restriction"),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(getToggleText(config.isEnabled(), "offhand_restriction"));
                // 立即保存配置
                configManager.updateOffHandRestrictionConfig(config);
                configManager.saveOffHandRestrictionConfig();
                LOGGER.info("副手限制已{}", config.isEnabled() ? "启用" : "禁用");
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
        
        // 返回按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.back"),
            button -> {
                this.client.setScreen(parentScreen);
            }
        ).dimensions(centerX - buttonWidth / 2, this.height - 30, buttonWidth, buttonHeight).build());
    }
    
    private void initWhitelistMode() {
        // 计算最大滚动偏移
        List<String> currentItems = getCurrentItems();
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, currentItems.size() - visibleItems);
        
        // 添加返回按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("better_experience.config.back"),
            button -> {
                currentMode = DisplayMode.MAIN_MENU;
                refreshScreen();
            }
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
        
        // 添加新建物品按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("+ 添加物品"),
            button -> {
                this.client.setScreen(new AddOffHandItemScreen(this, configManager));
            }
        ).dimensions(this.width / 2 - 100, LIST_END_Y + 10, 200, 20).build());
        
        // 添加滚动按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↑"), button -> {
            if (scrollOffset > 0) {
                scrollOffset--;
                updateListWidgets();
            }
        }).dimensions(this.width / 2 + 160, LIST_START_Y, 20, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↓"), button -> {
            if (scrollOffset < maxScrollOffset) {
                scrollOffset++;
                updateListWidgets();
            }
        }).dimensions(this.width / 2 + 160, LIST_END_Y - 20, 20, 20).build());
        
        updateListWidgets();
    }
    
    private void refreshScreen() {
        // 清除所有控件并重新初始化
        this.clearAndInit();
    }
    
    private List<String> getCurrentItems() {
        // 使用统一的白名单
        List<String> items = config.getAllowedItems();
        LOGGER.debug("当前白名单物品数量: {}, 物品: {}", items.size(), items);
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
            this.width / 2 - 100, y, 20, 20,
            itemId, true, // 白名单中的物品都是启用的
            button -> {
                // 可以在这里添加点击物品的详细配置
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
        ).dimensions(this.width / 2 - 70, y, 120, 20).build();
        
        // 删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                removeItem(itemId);
            }
        ).dimensions(this.width / 2 + 60, y, 20, 20).build();
        
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
            configManager.updateOffHandRestrictionConfig(config);
            configManager.saveOffHandRestrictionConfig();
            LOGGER.info("移除物品: {}", itemId);
            
            // 更新界面
            updateListWidgets();
        } catch (Exception e) {
            LOGGER.error("保存副手限制配置失败: " + e.getMessage(), e);
            // 可以在这里添加错误提示，但由于这是删除操作，通常不会失败
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        if (currentMode == DisplayMode.MAIN_MENU) {
            // 绘制说明文字
            Text description = Text.translatable("better_experience.config.offhand_restrictions.description");
            context.drawCenteredTextWithShadow(this.textRenderer, description, this.width / 2, 40, 0xAAAAAA);
        } else {
            // 绘制列表标题
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("副手白名单"), this.width / 2, 50, 0xFFFFFF);
            
            // 绘制列表背景
            context.fill(this.width / 2 - 160, LIST_START_Y - 5, this.width / 2 + 180, LIST_END_Y + 5, 0x44000000);
            
            // 绘制滚动信息
            if (maxScrollOffset > 0) {
                String scrollInfo = String.format("滚动: %d/%d", scrollOffset + 1, maxScrollOffset + 1);
                context.drawTextWithShadow(this.textRenderer, Text.literal(scrollInfo), 
                    this.width / 2 + 160, LIST_START_Y + 10, 0xFFFFFF);
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentMode != DisplayMode.MAIN_MENU && 
            mouseX >= this.width / 2 - 160 && mouseX <= this.width / 2 + 180 &&
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
    
    private Text getToggleText(boolean enabled, String type) {
        String key = enabled ? "enabled" : "disabled";
        return Text.translatable("better_experience.config.offhand_restrictions." + type + "." + key);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // 获取当前模式，供添加物品界面使用
    public DisplayMode getCurrentMode() {
        return currentMode;
    }
    
    // 刷新物品列表，供添加物品界面使用
    public void refreshItemList() {
        if (currentMode == DisplayMode.WHITELIST) {
            updateListWidgets();
        }
    }
    
    // 自定义物品图标按钮类
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
        }
    }
}
