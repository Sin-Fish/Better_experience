package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.ConfigManager;
import com.aeolyn.better_experience.config.ItemConfig;
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
    
    public ModConfigScreen(ConfigManager configManager) {
        super(Text.translatable("better_experience.config.title"));
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
        
        // 添加保存按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("better_experience.config.save"), button -> {
            configManager.saveAllConfigs();
            this.close();
        }).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
        
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
        // 启用/禁用按钮
        ButtonWidget toggleButton = ButtonWidget.builder(
            Text.literal(config.isEnabled() ? "✓" : "✗"),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(Text.literal(config.isEnabled() ? "✓" : "✗"));
            }
        ).dimensions(this.width / 2 - 140, y, 30, 20).build();
        
        // 物品名称按钮
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        String displayName = item.getName().getString();
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal(displayName),
            button -> {
                // 打开详细配置界面
                this.client.setScreen(new ItemDetailConfigScreen(configManager, config));
            }
        ).dimensions(this.width / 2 - 100, y, 200, 20).build();
        
        this.addDrawableChild(toggleButton);
        this.addDrawableChild(nameButton);
        itemWidgets.add(toggleButton);
        itemWidgets.add(nameButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        
        // 绘制列表背景
        context.fill(this.width / 2 - 160, LIST_START_Y - 5, this.width / 2 + 180, LIST_END_Y + 5, 0x44000000);
        
        // 绘制物品图标 - 调整位置和大小，让图标更明显
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        for (int i = 0; i < visibleItems && i + scrollOffset < itemConfigs.size(); i++) {
            ItemConfig config = itemConfigs.get(i + scrollOffset);
            Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
            ItemStack stack = new ItemStack(item);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            // 调整图标位置，让它更居中且更大，避免与按钮重叠
            context.drawItem(stack, this.width / 2 - 180, y + 5);
        }
        
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
}
