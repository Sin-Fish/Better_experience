package com.aeolyn.better_experience.importexport.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.importexport.core.ConfigImportExportManager;
import com.aeolyn.better_experience.render3d.gui.Render3DConfigScreen;
import com.aeolyn.better_experience.client.gui.ModConfigScreen;
import com.aeolyn.better_experience.client.gui.BaseConfigScreen;
import net.minecraft.client.MinecraftClient;
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

public class ConfigImportExportScreen extends BaseConfigScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-ImportExportScreen");
    
    private TextFieldWidget exportPathField;
    private TextFieldWidget importPathField;
    private ButtonWidget exportButton;
    private ButtonWidget importButton;
    private ButtonWidget validateButton;
    
    private String statusMessage = "";
    private int statusMessageTicks = 0;
    private boolean isStatusError = false;
    
    // 导入验证结果
    private ConfigImportExportManager.ImportValidationResult validationResult = null;
    private boolean showValidationDetails = false;
    
    // 验证状态枚举
    private enum ValidationStatus {
        UNKNOWN,    // 黄色 - 未验证
        VALID,      // 绿色 - 验证通过
        INVALID     // 红色 - 验证失败
    }
    
    private ValidationStatus validationStatus = ValidationStatus.UNKNOWN;
    
    public ConfigImportExportScreen(Screen parentScreen, ConfigManager configManager) {
        super(Text.literal("配置导入导出"), parentScreen, configManager);
    }
    
    @Override
    protected void loadData() {
        // 导入导出界面不需要加载数据
    }
    
    @Override
    protected void renderCustomContent(DrawContext context) {
        // 渲染状态消息
        if (statusMessageTicks > 0) {
            int color = isStatusError ? 0xFF0000 : 0x00FF00;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(statusMessage), 
                this.width / 2, 20, color);
            statusMessageTicks--;
        }
        
        // 渲染验证状态指示器
        if (validationStatus != ValidationStatus.UNKNOWN) {
            String statusText = validationStatus == ValidationStatus.VALID ? "✓ 验证通过" : "✗ 验证失败";
            int color = validationStatus == ValidationStatus.VALID ? 0x00FF00 : 0xFF0000;
            context.drawTextWithShadow(this.textRenderer, Text.literal(statusText), 
                this.width / 2 + 160, 80, color);
        }
        
        // 渲染验证详情
        if (showValidationDetails && validationResult != null) {
            renderValidationDetails(context);
        }
    }
    
    @Override
    protected void onAddClicked() {
        // 导入导出界面不需要添加功能
    }
    
    @Override
    protected void addStandardButtons() {
        super.addStandardButtons(); // 使用基类统一的并排返回/关闭按钮
    }
    
    @Override
    protected void addCustomButtons() {
        int centerX = this.width / 2;
        int startY = 80;
        int fieldWidth = 300;
        int fieldHeight = 20;
        int spacing = 30;
        
        // 导出路径输入框
        exportPathField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, startY, fieldWidth, fieldHeight, Text.literal("导出路径"));
        exportPathField.setPlaceholder(Text.literal("例如: C:\\BetterExperience_Config"));
        exportPathField.setText("C:\\BetterExperience_Config");
        
        // 导出按钮
        exportButton = ButtonWidget.builder(
            Text.literal("导出配置"),
            button -> exportConfigs()
        ).dimensions(centerX - fieldWidth/2, startY + spacing, fieldWidth, fieldHeight).build();
        
        // 分隔线
        int separatorY = startY + spacing * 3;
        
        // 导入路径输入框
        importPathField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, separatorY, fieldWidth, fieldHeight, Text.literal("导入路径"));
        importPathField.setPlaceholder(Text.literal("例如: C:\\BetterExperience_Config"));
        importPathField.setChangedListener(text -> {
            // 当路径改变时，重置验证状态
            validationStatus = ValidationStatus.UNKNOWN;
            showValidationDetails = false;
            validationResult = null;
        });
        
        // 验证按钮（和导入按钮一样大）
        validateButton = ButtonWidget.builder(
            Text.literal("验证导入配置"),
            button -> validateAllConfigs()
        ).dimensions(centerX - fieldWidth/2, separatorY + spacing, fieldWidth, fieldHeight).build();
        
        // 导入按钮
        importButton = ButtonWidget.builder(
            Text.literal("导入配置"),
            button -> importConfigs()
        ).dimensions(centerX - fieldWidth/2, separatorY + spacing * 2, fieldWidth, fieldHeight).build();
        
        // 添加所有控件
        this.addDrawableChild(exportPathField);
        this.addDrawableChild(exportButton);
        this.addDrawableChild(importPathField);
        this.addDrawableChild(validateButton);
        this.addDrawableChild(importButton);
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
                
                // 延迟2秒后返回主界面
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        MinecraftClient.getInstance().execute(() -> {
                            this.close();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                showError("配置导出失败，请检查路径和权限");
            }
        } catch (Exception e) {
            showError("配置导出失败: " + e.getMessage());
            LOGGER.error("配置导出失败: " + e.getMessage(), e);
        }
    }
    
    private void validateAllConfigs() {
        String importPath = importPathField.getText().trim();
        if (importPath.isEmpty()) {
            showError("导入路径不能为空");
            validationStatus = ValidationStatus.INVALID;
            return;
        }
        
        try {
            // 先验证文件结构
            ConfigImportExportManager.ImportValidationResult structureResult = ConfigImportExportManager.validateFileStructure(importPath);
            
            // 再验证配置内容
            ConfigImportExportManager.ImportValidationResult contentResult = ConfigImportExportManager.validateImportConfigs(importPath);
            
            // 合并结果
            validationResult = contentResult; // 使用内容验证结果作为主要结果，因为它包含了结构验证的信息
            
            showValidationDetails = true;
            
            // 只有当两个验证都通过时，状态才为有效
            if (structureResult.isValid() && contentResult.isValid()) {
                showSuccess("配置验证成功！");
                validationStatus = ValidationStatus.VALID;
            } else {
                showError("配置验证失败: " + validationResult.getMessage());
                validationStatus = ValidationStatus.INVALID;
            }
            
            LOGGER.info("配置验证完成: {}", importPath);
        } catch (Exception e) {
            showError("配置验证失败: " + e.getMessage());
            validationStatus = ValidationStatus.INVALID;
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
                message.append("\n成功导入: ").append(result.getTotalImported()).append(" 个3D渲染物品配置");
                if (result.isOffHandConfigImported()) {
                    message.append("\n副手限制配置: 已导入");
                }
                if (result.getTotalFailed() > 0) {
                    message.append("\n失败: ").append(result.getTotalFailed()).append(" 个");
                }
                showSuccess(message.toString());
                
                // 刷新父界面的物品列表
                if (parentScreen != null && parentScreen instanceof Render3DConfigScreen) {
                    ((Render3DConfigScreen) parentScreen).refreshItemList();
                }
                
                LOGGER.info("配置导入完成，成功: {}, 失败: {}", result.getTotalImported(), result.getTotalFailed());
                
                // 延迟2秒后返回主界面
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        MinecraftClient.getInstance().execute(() -> {
                            this.close();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
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
    
    private void renderValidationDetails(DrawContext context) {
        int startY = 300;
        int lineHeight = 12;
        int currentY = startY;
        
        // 验证结果标题
        context.drawTextWithShadow(this.textRenderer, Text.literal("验证结果详情:"), this.width / 2 - 150, currentY, 0xFFFFFF);
        currentY += lineHeight + 5;
        
        // 3D渲染主配置文件状态
        if (validationResult.isMainConfigValid()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✓ 3D渲染主配置文件: 有效 (" + validationResult.getMainConfigItemsCount() + " 个物品)"), 
                this.width / 2 - 150, currentY, 0x55FF55);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✗ 3D渲染主配置文件: 无效"), 
                this.width / 2 - 150, currentY, 0xFF5555);
        }
        currentY += lineHeight;
        
        // 副手限制配置文件状态
        if (validationResult.isOffHandConfigValid()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✓ 副手限制配置文件: 有效 (" + validationResult.getOffHandConfigItemsCount() + " 个物品)"), 
                this.width / 2 - 150, currentY, 0x55FF55);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("✗ 副手限制配置文件: 无效"), 
                this.width / 2 - 150, currentY, 0xFF5555);
        }
        currentY += lineHeight;
        
        // 3D渲染物品配置统计
        context.drawTextWithShadow(this.textRenderer, Text.literal("有效3D渲染物品配置: " + validationResult.getTotalValid()), 
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
        
        // 信息消息
        if (!validationResult.getInfos().isEmpty()) {
            currentY += 5;
            context.drawTextWithShadow(this.textRenderer, Text.literal("信息:"), this.width / 2 - 150, currentY, 0x55FFFF);
            currentY += lineHeight;
            for (String info : validationResult.getInfos()) {
                context.drawTextWithShadow(this.textRenderer, Text.literal("  • " + info), this.width / 2 - 150, currentY, 0x55FFFF);
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
}
