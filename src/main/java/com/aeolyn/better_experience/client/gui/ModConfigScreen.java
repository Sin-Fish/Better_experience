package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.manager.ConfigManager;
import com.aeolyn.better_experience.config.ItemConfig;
import com.aeolyn.better_experience.client.gui.ConfigImportExportScreen;
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

public class ModConfigScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-ConfigScreen");
    
    private final Screen parentScreen;
    private final ConfigManager configManager;
    private final List<ItemConfig> itemConfigs;
    private final List<ClickableWidget> itemWidgets;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 30;
    private static final int LIST_START_Y = 50;
    private static final int LIST_END_Y = 200;
    private static final int LIST_WIDTH = 300;
    
    public ModConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.title"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
        this.itemConfigs = new ArrayList<>();
        this.itemWidgets = new ArrayList<>();
        
        loadItemConfigs();
    }
    
    private void loadItemConfigs() {
        itemConfigs.clear();
        
        for (String itemId : configManager.getAllConfiguredItems()) {
            ItemConfig config = configManager.getItemConfig(itemId);
            if (config != null) {
                itemConfigs.add(config);
            }
        }
        
        LOGGER.info("加载了 {} 个物品配置", itemConfigs.size());
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 计算最大滚动偏移
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, itemConfigs.size() - visibleItems);
        
        // 添加返回按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("better_experience.config.back"), button -> {
            this.client.setScreen(parentScreen);
        }).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
        
        // 添加保存按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("better_experience.config.save"), button -> {
            configManager.saveAllConfigs();
            this.close();
        }).dimensions(this.width / 2 - 100, this.height - 60, 200, 20).build());
        
        // 添加关闭按钮（不保存设置）
        this.addDrawableChild(ButtonWidget.builder(Text.literal("关闭"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 100, this.height - 90, 200, 20).build());
        
        // 添加新建物品按钮（放在列表下方）
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ 新建物品"), button -> {
            this.client.setScreen(new AddItemConfigScreen(this, configManager));
        }).dimensions(this.width / 2 - 100, LIST_END_Y + 10, 200, 20).build());
        
        // 添加导入导出按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("导入导出配置"), button -> {
            this.client.setScreen(new ConfigImportExportScreen(this, configManager));
        }).dimensions(this.width / 2 - 100, LIST_END_Y + 40, 200, 20).build());
        
        // 添加滚动按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↑"), button -> {
            if (scrollOffset > 0) {
                scrollOffset--;
                updateItemWidgets();
            }
        }).dimensions(this.width / 2 + 160, LIST_START_Y, 20, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("↓"), button -> {
            if (scrollOffset < maxScrollOffset) {
                scrollOffset++;
                updateItemWidgets();
            }
        }).dimensions(this.width / 2 + 160, LIST_END_Y - 20, 20, 20).build());
        
        updateItemWidgets();
    }
    
    private void updateItemWidgets() {
        // 清除现有的物品控件
        for (ClickableWidget widget : itemWidgets) {
            this.remove(widget);
        }
        itemWidgets.clear();
        
        // 添加可见的物品控件
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        for (int i = 0; i < visibleItems && i + scrollOffset < itemConfigs.size(); i++) {
            ItemConfig config = itemConfigs.get(i + scrollOffset);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            addConfigEntry(config, y);
        }
    }
    
    private void addConfigEntry(ItemConfig config, int y) {
        // 创建自定义物品图标按钮（正方形）
        ItemIconButton iconButton = new ItemIconButton(
            this.width / 2 - 100, y, 20, 20, // 居中显示
            config.getItemId(), config.isEnabled(),
            button -> {
                config.setEnabled(!config.isEnabled());
                // 更新按钮外观
                ((ItemIconButton) button).setEnabled(config.isEnabled());
            }
        );
        
        // 物品名称按钮
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        String displayName = item.getName().getString();
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal(displayName),
            button -> {
                // 打开详细配置界面
                this.client.setScreen(new ItemDetailConfigScreen(this, configManager, config));
            }
        ).dimensions(this.width / 2 - 70, y, 120, 20).build(); // 居中显示
        
        // 删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                // 显示删除确认对话框
                showDeleteConfirmation(config.getItemId());
            }
        ).dimensions(this.width / 2 + 60, y, 20, 20).build();
        
        this.addDrawableChild(iconButton);
        this.addDrawableChild(nameButton);
        this.addDrawableChild(deleteButton);
        itemWidgets.add(iconButton);
        itemWidgets.add(nameButton);
        itemWidgets.add(deleteButton);
    }
    
    private void showDeleteConfirmation(String itemId) {
        // 创建确认对话框
        this.client.setScreen(new ConfirmDeleteScreen(this, configManager, itemId));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        
        // 绘制列表背景
        context.fill(this.width / 2 - 160, LIST_START_Y - 5, this.width / 2 + 180, LIST_END_Y + 5, 0x44000000);
        
        // 绘制滚动信息
        if (maxScrollOffset > 0) {
            String scrollInfo = String.format("滚动: %d/%d", scrollOffset + 1, maxScrollOffset + 1);
            context.drawTextWithShadow(this.textRenderer, Text.literal(scrollInfo), 
                this.width / 2 + 160, LIST_START_Y + 10, 0xFFFFFF);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= this.width / 2 - 160 && mouseX <= this.width / 2 + 180 &&
            mouseY >= LIST_START_Y && mouseY <= LIST_END_Y) {
            if (verticalAmount > 0 && scrollOffset > 0) {
                scrollOffset--;
                updateItemWidgets();
                return true;
            } else if (verticalAmount < 0 && scrollOffset < maxScrollOffset) {
                scrollOffset++;
                updateItemWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    // 刷新物品列表
    public void refreshItemList() {
        loadItemConfigs();
        updateItemWidgets();
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
            
            // 绘制状态指示器（小圆点）
            if (enabled) {
                context.fill(this.getX() + this.width - 4, this.getY() + 2, this.getX() + this.width - 2, this.getY() + 4, 0xFF00FF00); // 绿色
            } else {
                context.fill(this.getX() + this.width - 4, this.getY() + 2, this.getX() + this.width - 2, this.getY() + 4, 0xFFFF0000); // 红色
            }
        }
    }
}
