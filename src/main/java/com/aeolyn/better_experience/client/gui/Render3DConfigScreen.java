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
 * 3D渲染配置界面
 * 管理物品的3D渲染设置
 */
public class Render3DConfigScreen extends BaseConfigScreen {
    
    private final List<ItemConfig> itemConfigs;
    private final List<ClickableWidget> itemWidgets;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 30;
    private static final int LIST_START_Y = 50;
    private static final int LIST_END_Y = 200;
    
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
        // 渲染列表背景
        context.fill(getCenterX() - 160, LIST_START_Y - 5, getCenterX() + 180, LIST_END_Y + 5, 0x44000000);
        
        // 渲染滚动信息
        if (maxScrollOffset > 0) {
            String scrollInfo = String.format("滚动: %d/%d", scrollOffset + 1, maxScrollOffset + 1);
            context.drawTextWithShadow(this.textRenderer, Text.literal(scrollInfo),
                getCenterX() + 160, LIST_START_Y + 10, 0xFFFFFF);
        }
    }
    
    @Override
    protected void onAddClicked() {
        this.client.setScreen(new AddItemConfigScreen(this, configManager));
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
            button -> this.client.setScreen(new ConfigImportExportScreen(this, configManager))
        ).dimensions(getCenterX() - 100, LIST_END_Y + 40, 200, 20).build());
        
        // 添加滚动按钮
        addScrollButtons();
    }
    
    @Override
    protected void setupScrollableList() {
        // 计算最大滚动偏移量
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, itemConfigs.size() - visibleItems);
        
        updateItemWidgets();
    }
    
    // ==================== 重写标准按钮 ====================
    
    @Override
    protected void addStandardButtons() {
        // 使用BaseConfigScreen的默认实现，包含保存、返回和关闭按钮
        super.addStandardButtons();
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
            getCenterX() - 100, y, 20, 20, // 居中显示
            config.getItemId(), config.isEnabled(),
            button -> {
                config.setEnabled(!config.isEnabled());
                // 更新按钮外观
                ((ItemIconButton) button).setEnabled(config.isEnabled());
                LogUtil.logGuiAction("toggle_3d_item", getScreenName(), 
                    Map.of("itemId", config.getItemId(), "enabled", config.isEnabled()));
            }
        );
        
        // 物品名称按钮
        Item item = Registries.ITEM.get(Identifier.of(config.getItemId()));
        String displayName = item != null ? item.getName().getString() : config.getItemId();
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal(displayName),
            button -> {
                // 打开详细配置界面
                this.client.setScreen(new ItemDetailConfigScreen(this, configManager, config));
            }
        ).dimensions(getCenterX() - 70, y, 120, 20).build(); // 居中显示
        
        // 删除按钮
        ButtonWidget deleteButton = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                showDeleteConfirmation(config.getItemId());
            }
        ).dimensions(getCenterX() + 60, y, 20, 20).build();
        
        this.addDrawableChild(iconButton);
        this.addDrawableChild(nameButton);
        this.addDrawableChild(deleteButton);
        itemWidgets.add(iconButton);
        itemWidgets.add(nameButton);
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
    
    // ==================== 鼠标滚动处理 ====================
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= getCenterX() - 160 && mouseX <= getCenterX() + 180 &&
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
