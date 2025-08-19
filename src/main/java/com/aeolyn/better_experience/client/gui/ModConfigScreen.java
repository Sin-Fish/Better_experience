package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.importexport.gui.ConfigImportExportScreen;
import com.aeolyn.better_experience.inventory.gui.InventorySortConfigScreen;
import com.aeolyn.better_experience.offhand.gui.OffHandRestrictionConfigScreen;
import com.aeolyn.better_experience.render3d.gui.Render3DConfigScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 模组主配置界面
 * 提供各个模块配置的入口
 */
public class ModConfigScreen extends BaseConfigScreen {
    
    private final List<ClickableWidget> moduleWidgets;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private static final int ITEM_HEIGHT = 30;
    private static final int LIST_START_Y = 60;
    private static final int LIST_END_Y = 180;
    
    public ModConfigScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.translatable("better_experience.config.title"), parentScreen, configManager);
        this.moduleWidgets = new ArrayList<>();
    }
    
    @Override
    protected void loadData() {
        LogUtil.info(LogUtil.MODULE_GUI, "加载模组配置界面");
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
        // 主界面不需要添加功能
    }
    
    @Override
    protected void addStandardButtons() {
        super.addStandardButtons(); // 使用基类统一的并排返回/关闭
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
        maxScrollOffset = Math.max(0, 5 - visibleItems); // 5个模块
        
        updateModuleWidgets();
    }
    
    private void addScrollButtons() {
        // 向上滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↑"), 
            button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    updateModuleWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_START_Y, 20, 20).build());
        
        // 向下滚动按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("↓"), 
            button -> {
                if (scrollOffset < maxScrollOffset) {
                    scrollOffset++;
                    updateModuleWidgets();
                }
            }
        ).dimensions(getCenterX() + 160, LIST_END_Y - 20, 20, 20).build());
    }
    
    private void updateModuleWidgets() {
        // 清除现有的模块控件
        for (ClickableWidget widget : moduleWidgets) {
            this.remove(widget);
        }
        moduleWidgets.clear();
        
        // 模块列表
        List<ModuleItem> moduleItems = new ArrayList<>();
        moduleItems.add(new ModuleItem("通用配置", () -> this.client.setScreen(new GeneralConfigScreen(this, configManager))));
        moduleItems.add(new ModuleItem("3D渲染配置", () -> this.client.setScreen(new Render3DConfigScreen(this, configManager))));
        moduleItems.add(new ModuleItem("副手限制配置", () -> this.client.setScreen(new OffHandRestrictionConfigScreen(this, configManager))));
        moduleItems.add(new ModuleItem("背包增强配置", () -> this.client.setScreen(new InventorySortConfigScreen(this, configManager))));
        moduleItems.add(new ModuleItem("配置导入导出", () -> this.client.setScreen(new ConfigImportExportScreen(this, configManager))));
        
        // 添加可见的模块控件
        int visibleItems = (LIST_END_Y - LIST_START_Y) / ITEM_HEIGHT;
        for (int i = 0; i < visibleItems && i + scrollOffset < moduleItems.size(); i++) {
            ModuleItem item = moduleItems.get(i + scrollOffset);
            int y = LIST_START_Y + i * ITEM_HEIGHT;
            addModuleEntry(item, y);
        }
    }
    
    private void addModuleEntry(ModuleItem item, int y) {
        // 模块按钮
        ButtonWidget moduleButton = ButtonWidget.builder(
            Text.literal(item.name),
            button -> {
                LogUtil.logButtonClick(getScreenName(), "open_" + item.name.replaceAll("\\s+", "_").toLowerCase());
                item.onClick.run();
            }
        ).dimensions(getCenterX() - 120, y, 240, 20).build();
        
        moduleWidgets.add(moduleButton);
        this.addDrawableChild(moduleButton);
    }
    
    // 模块项内部类
    private static class ModuleItem {
        final String name;
        final Runnable onClick;
        
        ModuleItem(String name, Runnable onClick) {
            this.name = name;
            this.onClick = onClick;
        }
    }
    
    @Override
    protected void saveConfig() {
        // 主界面不需要保存配置
    }
    
    @Override
    public String getScreenName() {
        return "ModConfigScreen";
    }
}
