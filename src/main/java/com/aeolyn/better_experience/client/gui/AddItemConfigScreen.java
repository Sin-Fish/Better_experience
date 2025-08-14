package com.aeolyn.better_experience.client.gui;

import com.aeolyn.better_experience.config.ConfigManager;
import com.aeolyn.better_experience.config.ItemConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddItemConfigScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-AddItemScreen");
    
    private final ModConfigScreen parentScreen;
    private final ConfigManager configManager;
    
    private TextFieldWidget itemIdField;
    private TextFieldWidget renderIdField;
    private ButtonWidget renderTypeButton;
    private boolean isEntityRender = true; // true = 实体渲染, false = 方块渲染
    
    public AddItemConfigScreen(ModConfigScreen parentScreen, ConfigManager configManager) {
        super(Text.literal("新建物品配置"));
        this.parentScreen = parentScreen;
        this.configManager = configManager;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 80;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 30;
        
        // 物品ID输入框
        itemIdField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, startY, fieldWidth, fieldHeight, Text.literal("物品ID"));
        itemIdField.setPlaceholder(Text.literal("例如: minecraft:diamond_sword"));
        itemIdField.setChangedListener(text -> {
            // 实时验证物品ID
            validateItemId(text);
        });
        
        // 渲染ID输入框
        renderIdField = new TextFieldWidget(this.textRenderer, centerX - fieldWidth/2, startY + spacing, fieldWidth, fieldHeight, Text.literal("渲染ID"));
        renderIdField.setPlaceholder(Text.literal("实体ID或方块ID"));
        renderIdField.setChangedListener(text -> {
            // 根据渲染类型设置不同的提示
            updateRenderIdPlaceholder();
        });
        
        // 渲染方式切换按钮
        renderTypeButton = ButtonWidget.builder(
            Text.literal("渲染方式: 实体"),
            button -> {
                isEntityRender = !isEntityRender;
                button.setMessage(Text.literal("渲染方式: " + (isEntityRender ? "实体" : "方块")));
                updateRenderIdPlaceholder();
            }
        ).dimensions(centerX - fieldWidth/2, startY + spacing * 2, fieldWidth, fieldHeight).build();
        
        // 创建按钮
        ButtonWidget createButton = ButtonWidget.builder(
            Text.literal("创建配置"),
            button -> createItemConfig()
        ).dimensions(centerX - fieldWidth/2, startY + spacing * 3, fieldWidth, fieldHeight).build();
        
        // 返回按钮
        ButtonWidget backButton = ButtonWidget.builder(
            Text.literal("返回"),
            button -> this.close()
        ).dimensions(centerX - fieldWidth/2, startY + spacing * 4, fieldWidth, fieldHeight).build();
        
        // 添加所有控件
        this.addDrawableChild(itemIdField);
        this.addDrawableChild(renderIdField);
        this.addDrawableChild(renderTypeButton);
        this.addDrawableChild(createButton);
        this.addDrawableChild(backButton);
        
        // 初始化渲染ID提示
        updateRenderIdPlaceholder();
    }
    
    private void validateItemId(String itemId) {
        if (itemId != null && !itemId.isEmpty()) {
            try {
                Item item = Registries.ITEM.get(Identifier.of(itemId));
                if (item != null) {
                    // 物品存在，可以显示预览
                    LOGGER.debug("物品ID有效: {}", itemId);
                } else {
                    LOGGER.debug("物品ID无效: {}", itemId);
                }
            } catch (Exception e) {
                LOGGER.debug("物品ID格式错误: {}", itemId);
            }
        }
    }
    
    private void updateRenderIdPlaceholder() {
        if (isEntityRender) {
            renderIdField.setPlaceholder(Text.literal("例如: minecraft:arrow"));
        } else {
            renderIdField.setPlaceholder(Text.literal("例如: minecraft:stone"));
        }
    }
    
    private void createItemConfig() {
        String itemId = itemIdField.getText().trim();
        String renderId = renderIdField.getText().trim();
        
        // 验证输入
        if (itemId.isEmpty()) {
            LOGGER.warn("物品ID不能为空");
            return;
        }
        
        if (renderId.isEmpty()) {
            LOGGER.warn("渲染ID不能为空");
            return;
        }
        
        // 验证物品ID
        try {
            Item item = Registries.ITEM.get(Identifier.of(itemId));
            if (item == null) {
                LOGGER.warn("物品不存在: {}", itemId);
                return;
            }
        } catch (Exception e) {
            LOGGER.warn("物品ID格式错误: {}", itemId);
            return;
        }
        
        // 验证渲染ID
        try {
            if (isEntityRender) {
                // 验证实体类型
                if (Registries.ENTITY_TYPE.get(Identifier.of(renderId)) == null) {
                    LOGGER.warn("实体类型不存在: {}", renderId);
                    return;
                }
            } else {
                // 验证方块
                if (Registries.BLOCK.get(Identifier.of(renderId)) == null) {
                    LOGGER.warn("方块不存在: {}", renderId);
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("渲染ID格式错误: {}", renderId);
            return;
        }
        
        // 创建新的物品配置
        ItemConfig newConfig = new ItemConfig();
        newConfig.setItemId(itemId);
        newConfig.setEnabled(true);
        
        if (isEntityRender) {
            newConfig.setRenderAsEntity(true);
            newConfig.setRenderAsBlock(false);
            newConfig.setEntityType(renderId);
        } else {
            newConfig.setRenderAsEntity(false);
            newConfig.setRenderAsBlock(true);
            newConfig.setBlockId(renderId);
        }
        
        // 添加到配置管理器
        configManager.addItemConfig(itemId, newConfig);
        
        LOGGER.info("成功创建物品配置: {}", itemId);
        
        // 返回主界面并刷新列表
        this.close();
        parentScreen.refreshItemList();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        context.drawTextWithShadow(this.textRenderer, Text.literal("物品ID: 要渲染的物品的ID"), this.width / 2 - 100, 60, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("渲染ID: 用于渲染的实体或方块ID"), this.width / 2 - 100, 90, 0xCCCCCC);
        
        // 绘制物品预览（如果物品ID有效）
        String itemId = itemIdField.getText().trim();
        if (!itemId.isEmpty()) {
            try {
                Item item = Registries.ITEM.get(Identifier.of(itemId));
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    context.drawItem(stack, this.width / 2 + 120, 70);
                }
            } catch (Exception ignored) {}
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parentScreen);
    }
}
