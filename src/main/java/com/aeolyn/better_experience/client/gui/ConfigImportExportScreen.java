package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.manager.ConfigManager;
import com.aeolyn.better_experience.config.manager.ConfigImportExportManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigImportExportScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-ImportExportScreen");
    
    private final ModConfigScreen parentScreen;
    private final ConfigManager configManager;
    
    private TextFieldWidget exportPathField;
    private TextFieldWidget importPathField;
    private ButtonWidget exportButton;
    private ButtonWidget importButton;
    private ButtonWidget validateButton;
    
    private String statusMessage = "";
    private int statusMessageTicks = 0;
    private boolean isStatusError = false;
    
    // 导入验证结果
    private ConfigImportExportManager.ValidationResult validationResult = null;
    private boolean showValidationDetails = false;
    
    public ConfigImportExportScreen(ModConfigScreen parentScreen, ConfigManager configManager) {
        super(Text.literal("配置导入导出"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 80;
        int fieldWidth = 300;
        int fieldHeight = 20;
        int spacing = 30;
        
        // 导出路径输入框
        exportPathField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, startY, fieldWidth, fieldHeight, Text.literal("导出路径"));
        exportPathField.setPlaceholder(Text.literal("例如: ./config_export"));
        exportPathField.setText("./config_export");
        
        // 导出按钮
        exportButton = ButtonWidget.builder(
            Text.literal("导出配置"),
            button -> exportConfigs()
        ).dimensions(centerX - fieldWidth/2, startY + spacing, fieldWidth, fieldHeight).build();
        
        // 分隔线
        int separatorY = startY + spacing * 3;
        
        // 导入路径输入框
        importPathField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, separatorY, fieldWidth, fieldHeight, Text.literal("导入路径"));
        importPathField.setPlaceholder(Text.literal("例如: ./config_import"));
        
        // 验证按钮
        validateButton = ButtonWidget.builder(
            Text.literal("验证导入配置"),
            button -> validateImportConfigs()
        ).dimensions(centerX - fieldWidth/2, separatorY + spacing, fieldWidth, fieldHeight).build();
        
        // 导入按钮
        importButton = ButtonWidget.builder(
            Text.literal("导入配置"),
            button -> importConfigs()
        ).dimensions(centerX - fieldWidth/2, separatorY + spacing * 2, fieldWidth, fieldHeight).build();
        
        // 返回按钮
        ButtonWidget backButton = ButtonWidget.builder(
            Text.literal("返回"),
            button -> this.close()
        ).dimensions(centerX - fieldWidth/2, this.height - 40, fieldWidth, fieldHeight).build();
        
        // 添加所有控件
        this.addDrawableChild(exportPathField);
        this.addDrawableChild(exportButton);
        this.addDrawableChild(importPathField);
        this.addDrawableChild(validateButton);
        this.addDrawableChild(importButton);
        this.addDrawableChild(backButton);
    }
    
    private void exportConfigs() {
        String exportPath = exportPathField.getText().trim();
        if (exportPath.isEmpty()) {
            showError("导出路径不能为空");
            return;
        }
        
        try {
            boolean success = ConfigImportExportManager.exportConfigs(configManager, exportPath);
            if (success) {
                showSuccess("配置导出成功！路径: " + exportPath);
                LOGGER.info("配置导出成功: {}", exportPath);
            } else {
                showError("配置导出失败，请检查路径和权限");
            }
        } catch (Exception e) {
            showError("配置导出失败: " + e.getMessage());
            LOGGER.error("配置导出失败: " + e.getMessage(), e);
        }
    }
    
    private void validateImportConfigs() {
        String importPath = importPathField.getText().trim();
        if (importPath.isEmpty()) {
            showError("导入路径不能为空");
            return;
        }
        
        try {
            validationResult = ConfigImportExportManager.validateImportConfigs(importPath);
            showValidationDetails = true;
            
            if (validationResult.isValid()) {
                showSuccess("配置验证成功！");
            } else {
                showError("配置验证失败: " + validationResult.getMessage());
            }
            
            LOGGER.info("配置验证完成: {}", importPath);
        } catch (Exception e) {
            showError("配置验证失败: " + e.getMessage());
            LOGGER.error("配置验证失败: " + e.getMessage(), e);
        }
    }
    
    private void importConfigs() {
        String importPath = importPathField.getText().trim();
        if (importPath.isEmpty()) {
            showError("导入路径不能为空");
            return;
        }
        
        try {
            ConfigImportExportManager.ImportResult result = ConfigImportExportManager.importConfigs(configManager, importPath);
            
            if (result.isSuccess()) {
                StringBuilder message = new StringBuilder("配置导入成功！");
                message.append("\n成功导入: ").append(result.getTotalImported()).append(" 个物品配置");
                if (result.getTotalFailed() > 0) {
                    message.append("\n失败: ").append(result.getTotalFailed()).append(" 个");
                }
                showSuccess(message.toString());
                
                // 刷新父界面的物品列表
                if (parentScreen != null) {
                    parentScreen.refreshItemList();
                }
                
                LOGGER.info("配置导入完成，成功: {}, 失败: {}", result.getTotalImported(), result.getTotalFailed());
            } else {
                showError("配置导入失败: " + result.getMessage());
            }
        } catch (Exception e) {
            showError("配置导入失败: " + e.getMessage());
            LOGGER.error("配置导入失败: " + e.getMessage(), e);
        }
    }
    
    private void showSuccess(String message) {
        statusMessage = message;
        statusMessageTicks = 120; // 显示6秒
        isStatusError = false;
    }
    
    private void showError(String message) {
        statusMessage = message;
        statusMessageTicks = 120; // 显示6秒
        isStatusError = true;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        context.drawTextWithShadow(this.textRenderer, Text.literal("导出配置: 将当前所有配置导出到指定目录"), this.width / 2 - 150, 60, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("导入配置: 从指定目录导入配置（会覆盖现有配置）"), this.width / 2 - 150, 200, 0xCCCCCC);
        
        // 显示状态消息
        if (statusMessageTicks > 0) {
            int color = isStatusError ? 0xFF5555 : 0x55FF55;
            String[] lines = statusMessage.split("\n");
            int y = 250;
            for (String line : lines) {
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(line), this.width / 2, y, color);
                y += 12;
            }
            statusMessageTicks--;
        }
        
        // 显示验证结果详情
        if (showValidationDetails && validationResult != null) {
            renderValidationDetails(context);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderValidationDetails(DrawContext context) {
        int startY = 300;
        int lineHeight = 12;
        int currentY = startY;
        
        // 验证结果标题
        context.drawTextWithShadow(this.textRenderer, Text.literal("验证结果详情:"), this.width / 2 - 150, currentY, 0xFFFFFF);
        currentY += lineHeight + 5;
        
        // 主配置文件状态
        if (validationResult.isMainConfigValid()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✓ 主配置文件: 有效 (" + validationResult.getMainConfigItemsCount() + " 个物品)"), 
                this.width / 2 - 150, currentY, 0x55FF55);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✗ 主配置文件: 无效"), 
                this.width / 2 - 150, currentY, 0xFF5555);
        }
        currentY += lineHeight;
        
        // 物品配置统计
        context.drawTextWithShadow(this.textRenderer, Text.literal("有效物品配置: " + validationResult.getTotalValid()), 
            this.width / 2 - 150, currentY, 0x55FF55);
        currentY += lineHeight;
        
        if (validationResult.getTotalInvalid() > 0) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("无效物品配置: " + validationResult.getTotalInvalid()), 
                this.width / 2 - 150, currentY, 0xFF5555);
            currentY += lineHeight;
        }
        
        // 错误信息
        if (!validationResult.getErrors().isEmpty()) {
            currentY += 5;
            context.drawTextWithShadow(this.textRenderer, Text.literal("错误:"), this.width / 2 - 150, currentY, 0xFF5555);
            currentY += lineHeight;
            for (String error : validationResult.getErrors()) {
                context.drawTextWithShadow(this.textRenderer, Text.literal("  • " + error), this.width / 2 - 150, currentY, 0xFF5555);
                currentY += lineHeight;
            }
        }
        
        // 警告信息
        if (!validationResult.getWarnings().isEmpty()) {
            currentY += 5;
            context.drawTextWithShadow(this.textRenderer, Text.literal("警告:"), this.width / 2 - 150, currentY, 0xFFFF55);
            currentY += lineHeight;
            for (String warning : validationResult.getWarnings()) {
                context.drawTextWithShadow(this.textRenderer, Text.literal("  • " + warning), this.width / 2 - 150, currentY, 0xFFFF55);
                currentY += lineHeight;
            }
        }
        
        // 如果内容太多，添加滚动提示
        if (currentY > this.height - 100) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("(内容较多，请查看日志获取完整信息)"), 
                this.width / 2 - 150, this.height - 80, 0xCCCCCC);
        }
    }
    
    @Override
    public void close() {
        this.client.setScreen(parentScreen);
    }
}
