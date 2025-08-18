package com.aeolyn.better_experience.render3d.gui;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import com.aeolyn.better_experience.client.gui.ModConfigScreen;
import com.aeolyn.better_experience.render3d.gui.Render3DConfigScreen;
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
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.projectile.ArrowEntity;

public class AddItemConfigScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-AddItemScreen");
    
    private final Screen parentScreen;
    private final ConfigManager configManager;
    
    private TextFieldWidget itemIdField;
    private TextFieldWidget renderIdField;
    private ButtonWidget renderTypeButton;
    private boolean isEntityRender = true; // true = 实体渲染, false = 方块渲染
    private String errorMessage = "";
    private int errorMessageTicks = 0;
    
    public AddItemConfigScreen(Screen parentScreen, ConfigManager configManager) {
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
        itemIdField.setPlaceholder(Text.literal("格式: namespace:item_name"));
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
            // 实时验证渲染ID
            validateRenderId(text);
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
            // 首先检查格式
            if (!itemId.contains(":")) {
                LOGGER.debug("物品ID格式错误，缺少命名空间: {}", itemId);
                return;
            }
            
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
    
    private void validateRenderId(String renderId) {
        if (renderId != null && !renderId.isEmpty()) {
            // 首先检查格式
            if (!renderId.contains(":")) {
                LOGGER.debug("渲染ID格式错误，缺少命名空间: {}", renderId);
                return;
            }
            
            try {
                if (isEntityRender) {
                    // 验证实体类型
                    if (Registries.ENTITY_TYPE.get(Identifier.of(renderId)) != null) {
                        LOGGER.debug("实体ID有效: {}", renderId);
                    } else {
                        LOGGER.debug("实体ID无效: {}", renderId);
                    }
                } else {
                    // 验证方块
                    if (Registries.BLOCK.get(Identifier.of(renderId)) != null) {
                        LOGGER.debug("方块ID有效: {}", renderId);
                    } else {
                        LOGGER.debug("方块ID无效: {}", renderId);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("渲染ID格式错误: {}", renderId);
            }
        }
    }
    
    private void updateRenderIdPlaceholder() {
        if (isEntityRender) {
            renderIdField.setPlaceholder(Text.literal("格式: namespace:entity_name"));
        } else {
            renderIdField.setPlaceholder(Text.literal("格式: namespace:block_name"));
        }
    }
    
    private void showError(String message) {
        errorMessage = message;
        errorMessageTicks = 120; // 显示6秒 (20 ticks/秒)
        LOGGER.warn("配置创建错误: {}", message);
    }
    
    private void createItemConfig() {
        String itemId = itemIdField.getText().trim();
        String renderId = renderIdField.getText().trim();
        
        // 验证输入
        if (itemId.isEmpty()) {
            showError("物品ID不能为空");
            return;
        }
        
        if (renderId.isEmpty()) {
            showError("渲染ID不能为空");
            return;
        }
        
        // 验证物品ID格式
        if (!itemId.contains(":")) {
            showError("物品ID格式错误，必须包含命名空间 (例如: minecraft:diamond_sword)");
            return;
        }
        
        // 验证物品ID
        try {
            Item item = Registries.ITEM.get(Identifier.of(itemId));
            if (item == null) {
                showError("物品不存在: " + itemId);
                return;
            }
        } catch (Exception e) {
            showError("物品ID格式错误: " + itemId);
            return;
        }
        
        // 验证渲染ID格式
        if (!renderId.contains(":")) {
            showError("渲染ID格式错误，必须包含命名空间 (例如: minecraft:arrow)");
            return;
        }
        
        // 验证渲染ID
        try {
            if (isEntityRender) {
                // 验证实体类型
                if (Registries.ENTITY_TYPE.get(Identifier.of(renderId)) == null) {
                    showError("实体类型不存在: " + renderId);
                    return;
                }
            } else {
                // 验证方块
                if (Registries.BLOCK.get(Identifier.of(renderId)) == null) {
                    showError("方块不存在: " + renderId);
                    return;
                }
            }
        } catch (Exception e) {
            showError("渲染ID格式错误: " + renderId);
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
        boolean success = configManager.addItemConfig(itemId, newConfig);
        
        if (success) {
            LOGGER.info("成功创建物品配置: {}", itemId);
            // 返回主界面并刷新列表
            this.close();
            if (parentScreen instanceof Render3DConfigScreen) {
                ((Render3DConfigScreen) parentScreen).refreshItemList();
            } else if (parentScreen instanceof ModConfigScreen) {
                ((ModConfigScreen) parentScreen).refreshItemList();
            }
        } else {
            showError("创建配置失败，请检查输入是否正确");
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.fill(0, 0, this.width, this.height, 0x88000000);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // 绘制说明文字
        context.drawTextWithShadow(this.textRenderer, Text.literal("物品ID: 要渲染的物品的ID (格式: minecraft:item_name)"), this.width / 2 - 100, 60, 0xCCCCCC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("渲染ID: 用于渲染的实体或方块ID (格式: minecraft:entity_name)"), this.width / 2 - 100, 90, 0xCCCCCC);
        
        // 显示错误消息
        if (errorMessageTicks > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(errorMessage), this.width / 2, 120, 0xFF5555);
            errorMessageTicks--;
        }
        
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
        
        // 绘制渲染ID预览（如果渲染ID有效）
        String renderId = renderIdField.getText().trim();
        if (!renderId.isEmpty()) {
            try {
                if (isEntityRender) {
                    // 实体预览
                    EntityType<?> entityType = Registries.ENTITY_TYPE.get(Identifier.of(renderId));
                    if (entityType != null) {
                        // 显示实体名称
                        context.drawTextWithShadow(this.textRenderer, Text.literal("实体: " + renderId), this.width / 2 + 120, 100, 0x00FF00);
                        
                        // 渲染实体预览
                        try {
                            MinecraftClient client = MinecraftClient.getInstance();
                            World world = client.world;
                            if (world != null) {
                                // 创建实体实例
                                Entity entity = entityType.create(world, net.minecraft.entity.SpawnReason.NATURAL);
                                if (entity != null) {
                                    // 设置实体位置
                                    entity.setPos(0, 0, 0);
                                    
                                    // 准备渲染
                                    VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
                                    
                                    // 创建新的矩阵栈
                                    MatrixStack matrices = new MatrixStack();
                                    
                                    // 调整渲染位置和缩放
                                    matrices.translate(this.width / 2 + 120, 110, 0);
                                    matrices.scale(0.5f, 0.5f, 0.5f);
                                    
                                    // 特殊处理箭的旋转
                                    if (entity instanceof ArrowEntity) {
                                        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
                                    }
                                    
                                    // 渲染实体
                                    EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
                                    dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, matrices, vertexConsumers, 15728880);
                                    
                                    // 清理实体
                                    entity.discard();
                                    
                                    // 添加调试信息
                                    LOGGER.debug("成功渲染实体: {}", renderId);
                                } else {
                                    LOGGER.debug("无法创建实体: {}", renderId);
                                }
                            } else {
                                LOGGER.debug("世界为空，无法渲染实体");
                            }
                        } catch (Exception e) {
                            LOGGER.debug("实体渲染失败: {} - {}", renderId, e.getMessage());
                            // 如果实体渲染失败，只显示状态指示器
                            context.drawTextWithShadow(this.textRenderer, Text.literal("✓"), this.width / 2 + 120, 110, 0x00FF00);
                        }
                    }
                    // 无效的实体ID不显示任何内容
                } else {
                    // 方块预览
                    Block block = Registries.BLOCK.get(Identifier.of(renderId));
                    if (block != null) {
                        ItemStack blockStack = new ItemStack(block.asItem());
                        context.drawItem(blockStack, this.width / 2 + 120, 100);
                    }
                    // 无效的方块ID不显示任何内容
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
