package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.ModConfig;
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
 * 通用配置界面
 * 用于管理模组的开关和通用设置
 */
public class GeneralConfigScreen extends BaseConfigScreen {
    
    private final List<ClickableWidget> configWidgets;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 30;
    private static final int LIST_START_Y = 60;
    private static final int LIST_END_Y = 180;
    
    private ModConfig modConfig;
    
    public GeneralConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.general.title"), parentScreen, configManager);
        this.configWidgets = new ArrayList<>();
    }
    
    @Override
    protected void loadData() {
        modConfig = configManager.getModConfig();
        LogUtil.info(LogUtil.MODULE_GUI, "加载通用配置");
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
        // 通用配置界面不需要添加功能
    }
    
    @Override
    protected void addStandardButtons() {
        super.addStandardButtons(); // 使用基类的并排按钮布局
    }
    
    @Override
    protected void addCustomButtons() {
        // 添加滚动按钮
        addScrollButtons();
        
        // 设置滚动列表
        setupScrollableList();
    }
    
    // ==================== 滚动列表方法 ====================
    
    @Override
    protected void setupScrollableList() {
        // 计算最大滚动偏移量
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, 4 - visibleItems); // 4个配置项
        
        updateConfigWidgets();
    }
    
    private void addScrollButtons() {
        // 向上滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↑"), 
            button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    updateConfigWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_START_Y, 20, 20).build());
        
        // 向下滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↓"), 
            button -> {
                if (scrollOffset < maxScrollOffset) {
                    scrollOffset++;
                    updateConfigWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_END_Y - 20, 20, 20).build());
    }
    
    private void updateConfigWidgets() {
        // 清除现有的配置控件
        for (ClickableWidget widget : configWidgets) {
            this.remove(widget);
        }
        configWidgets.clear();
        
        // 配置项列表
        List<ConfigItem> configItems = new ArrayList<>();
        configItems.add(new ConfigItem("3D渲染模块", modConfig.isRender3dEnabled(), 
            enabled -> modConfig.setRender3dEnabled(enabled)));
        configItems.add(new ConfigItem("副手限制模块", modConfig.isOffhandRestrictionEnabled(), 
            enabled -> modConfig.setOffhandRestrictionEnabled(enabled)));
        configItems.add(new ConfigItem("背包排序模块", modConfig.isInventorySortEnabled(), 
            enabled -> modConfig.setInventorySortEnabled(enabled)));
        configItems.add(new ConfigItem("调试模式", modConfig.isDebugMode(), 
            enabled -> modConfig.setDebugMode(enabled)));
        
        // 添加可见的配置控件
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        for (int i = 0; i < visibleItems && i + scrollOffset < configItems.size(); i++) {
            ConfigItem item = configItems.get(i + scrollOffset);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            addConfigEntry(item, y);
        }
    }
    
    private void addConfigEntry(ConfigItem item, int y) {
        // 配置名称按钮
        ButtonWidget nameButton = ButtonWidget.builder(
            Text.literal(item.name + ": " + (item.enabled ? "启用" : "禁用")),
            button -> {
                item.enabled = !item.enabled;
                item.onToggle.accept(item.enabled);
                button.setMessage(Text.literal(item.name + ": " + (item.enabled ? "启用" : "禁用")));
                LogUtil.logButtonClick(getScreenName(), "toggle_" + item.name.replaceAll("\\s+", "_").toLowerCase());
            }
        ).dimensions(getCenterX() - 120, y, 240, 20).build();
        
        configWidgets.add(nameButton);
        this.addDrawableChild(nameButton);
    }
    
    // 配置项内部类
    private static class ConfigItem {
        final String name;
        boolean enabled;
        final java.util.function.Consumer<Boolean> onToggle;
        
        ConfigItem(String name, boolean enabled, java.util.function.Consumer<Boolean> onToggle) {
            this.name = name;
            this.enabled = enabled;
            this.onToggle = onToggle;
        }
    }
    
    @Override
    protected void saveConfig() {
        try {
            // 保存通用配置
            configManager.updateModConfig(modConfig);
            
            LogUtil.logSuccess(LogUtil.MODULE_GUI, "通用配置保存成功");
            
        } catch (Exception e) {
            LogUtil.error(LogUtil.MODULE_GUI, "保存通用配置失败: {}", e.getMessage(), e);
            
            if (client != null && client.player != null) {
                client.player.sendMessage(Text.literal("[Better Experience] 配置保存失败！"), false);
            }
        }
    }
    
    @Override
    public String getScreenName() {
        return "GeneralConfigScreen";
    }
}
