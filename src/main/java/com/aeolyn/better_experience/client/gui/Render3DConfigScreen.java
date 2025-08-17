package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.render3d.gui.AddItemConfigScreen;
import com.aeolyn.better_experience.render3d.gui.ItemDetailConfigScreen;
import com.aeolyn.better_experience.client.gui.ConfigImportExportScreen;
import com.aeolyn.better_experience.common.util.LogUtil;
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
import java.util.Map;

/**
 * 3D渲染配置界面
 * 管理物品的3D渲染设置
 */
public class Render3DConfigScreen extends BaseConfigScreen {
    
    private final List<ItemConfig> itemConfigs;
    private final List<ButtonWidget> itemWidgets;
    
    public Render3DConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.render3d.title"), parentScreen, configManager);
        this.itemConfigs = new ArrayList<>();
        this.itemWidgets = new ArrayList<>();
    }
    
    // ==================== 抽象方法实现 ====================
    
    @Override
    protected void loadData() {
        itemConfigs.clear();
        
        for (String itemId : configManager.getAllConfiguredItems()) {
            ItemConfig config = configManager.getItemConfig(itemId);
            if (config != null) {
                itemConfigs.add(config);
            }
        }
        
        LogUtil.info(LogUtil.MODULE_GUI, "加载了 {} 个3D渲染物品配置", itemConfigs.size());
    }
    
    @Override
    protected void saveData() {
        configManager.saveAllConfigs();
        setInfo("3D渲染配置已保存");
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        // 渲染物品列表
        renderItemConfigs(context);
        
        // 渲染说明文字
        renderDescription(context);
    }
    
    @Override
    protected void onAddClicked() {
        // 暂时使用原来的ModConfigScreen作为父界面
        this.client.setScreen(new AddItemConfigScreen((ModConfigScreen) this.parentScreen, configManager));
    }
    
    @Override
    protected void onSaveClicked() {
        saveData();
        showInfoDialog("成功", "3D渲染配置已保存");
    }
    
    // ==================== 自定义按钮 ====================
    
    @Override
    protected void addCustomButtons() {
        // 添加新建物品按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("+ 新建物品"), 
            button -> onAddClicked()
        ).dimensions(getCenterX() - 100, LIST_END_Y + 10, 200, 20).build());
        
        // 添加导入导出按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("导入导出配置"), 
            button -> this.client.setScreen(new ConfigImportExportScreen((ModConfigScreen) this.parentScreen, configManager))
        ).dimensions(getCenterX() - 100, LIST_END_Y + 40, 200, 20).build());
        
        // 添加滚动按钮
        addScrollButtons();
    }
    
    @Override
    protected void setupScrollableList() {
        setupScrollableList(itemConfigs);
        updateItemWidgets();
    }
    
    // ==================== 渲染方法 ====================
    
    private void renderItemConfigs(DrawContext context) {
        renderScrollableList(context, itemConfigs, LIST_START_Y, LIST_END_Y);
    }
    
    @Override
    protected void renderListItem(DrawContext context, Object item, int y) {
        if (item instanceof ItemConfig) {
            renderItemConfigEntry(context, (ItemConfig) item, y);
        }
    }
    
    private void renderItemConfigEntry(DrawContext context, ItemConfig config, int y) {
        // 渲染物品图标
        renderItemIcon(context, config, y);
        
        // 渲染物品名称
        renderItemName(context, config, y);
        
        // 渲染启用状态
        renderEnabledStatus(context, config, y);
    }
    
    private void renderItemIcon(DrawContext context, ItemConfig config, int y) {
        // 绘制物品图标
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            // 根据启用状态设置不同的渲染参数
            if (config.isEnabled()) {
                // 正常显示
                context.drawItem(stack, getCenterX() - 100, y + 2);
            } else {
                // 变暗显示
                context.drawItem(stack, getCenterX() - 100, y + 2, 0x88888888);
            }
        }
        
        // 绘制状态指示器（小圆点）
        if (config.isEnabled()) {
            context.fill(getCenterX() - 80, y + 2, getCenterX() - 78, y + 4, 0xFF00FF00); // 绿色
        } else {
            context.fill(getCenterX() - 80, y + 2, getCenterX() - 78, y + 4, 0xFFFF0000); // 红色
        }
    }
    
    private void renderItemName(DrawContext context, ItemConfig config, int y) {
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        String displayName = item != null ? item.getName().getString() : config.getItemId();
        context.drawTextWithShadow(this.textRenderer, Text.literal(displayName), 
            getCenterX() - 70, y + 5, 0xFFFFFF);
    }
    
    private void renderEnabledStatus(DrawContext context, ItemConfig config, int y) {
        String status = config.isEnabled() ? "启用" : "禁用";
        int color = config.isEnabled() ? 0xFF00FF00 : 0xFFFF0000;
        context.drawTextWithShadow(this.textRenderer, Text.literal(status), 
            getCenterX() + 50, y + 5, color);
    }
    
    private void renderDescription(DrawContext context) {
        renderCenteredText(context, "3D渲染配置 - 管理物品的3D渲染设置", 250, 0xFFFFFF);
        renderCenteredText(context, "点击物品名称进入详细配置", 270, 0xCCCCCC);
    }
    
    // ==================== 滚动按钮 ====================
    
    private void addScrollButtons() {
        // 向上滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↑"), 
            button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    updateItemWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_START_Y, 20, 20).build());
        
        // 向下滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↓"), 
            button -> {
                if (scrollOffset < maxScrollOffset) {
                    scrollOffset++;
                    updateItemWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_END_Y - 20, 20, 20).build());
    }
    
    private void updateItemWidgets() {
        // 清除现有的物品控件
        for (ButtonWidget widget : itemWidgets) {
            this.remove(widget);
        }
        itemWidgets.clear();
        
        // 添加可见的物品控件
        int visibleItems = getVisibleItemCount();
        for (int i = 0; i < visibleItems && i + scrollOffset < itemConfigs.size(); i++) {
            ItemConfig config = itemConfigs.get(i + scrollOffset);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            addConfigEntry(config, y);
        }
    }
    
    private void addConfigEntry(ItemConfig config, int y) {
        // 创建物品名称按钮
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal("配置"),
            button -> {
                this.client.setScreen(new ItemDetailConfigScreen((ModConfigScreen) this.parentScreen, configManager, config));
            }
        ).dimensions(getCenterX() + 70, y, 80, 20).build();
        
        this.addDrawableChild(nameButton);
        itemWidgets.add(nameButton);
        
        // 创建启用/禁用切换按钮
        ButtonWidget toggleButton = ButtonWidget.builder(
            Text.literal(config.isEnabled() ? "禁用" : "启用"),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(Text.literal(config.isEnabled() ? "禁用" : "启用"));
                LogUtil.logGuiAction("toggle_3d_item", getScreenName(), 
                    Map.of("itemId", config.getItemId(), "enabled", config.isEnabled()));
            }
        ).dimensions(getCenterX() + 160, y, 60, 20).build();
        
        this.addDrawableChild(toggleButton);
        itemWidgets.add(toggleButton);
        
        // 创建删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                showDeleteConfirmation(config.getItemId());
            }
        ).dimensions(getCenterX() + 230, y, 20, 20).build();
        
        this.addDrawableChild(deleteButton);
        itemWidgets.add(deleteButton);
    }
    
    private void showDeleteConfirmation(String itemId) {
        showConfirmDialog("确认删除", "确定要删除3D渲染配置 " + itemId + " 吗？", () -> {
            configManager.removeItemConfig(itemId);
            loadData();
            updateItemWidgets();
            setInfo("3D渲染配置已删除");
        });
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 刷新物品列表
     */
    public void refreshItemList() {
        loadData();
        updateItemWidgets();
    }
    
    /**
     * 获取界面名称
     */
    @Override
    protected String getScreenName() {
        return "Render3DConfigScreen";
    }
}
