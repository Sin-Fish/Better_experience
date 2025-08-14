package com.example.handheld3d.core;

import com.example.handheld3d.config.ConfigManager;
import com.example.handheld3d.config.ItemConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Item3DRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Handheld3D-Renderer");
    
    public static void render3DItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                   int light, int overlay, ItemDisplayContext displayContext, 
                                   String itemId) {
        try {
            ItemConfig config = ConfigManager.getItemConfig(itemId);
            if (config == null || !config.isEnabled()) {
                if (ConfigManager.isDebugEnabled()) {
                    LOGGER.warn("物品配置未找到或未启用: " + itemId);
                }
                return;
            }
            
            if (!config.isRenderAsBlock()) {
                if (ConfigManager.isDebugEnabled()) {
                    LOGGER.warn("物品未配置为方块渲染: " + itemId);
                }
                return;
            }
            
            // 获取方块状态
            BlockState blockState = getBlockState(config.getBlockId());
            if (blockState == null) {
                LOGGER.error("无法获取方块状态: " + config.getBlockId());
                return;
            }
            
            // 应用渲染设置
            applyRenderSettings(matrices, displayContext, config);
            
            // 渲染3D方块
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getBlockRenderManager() != null) {
                BlockRenderManager blockRenderManager = client.getBlockRenderManager();
                blockRenderManager.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, overlay);
                
                if (ConfigManager.isDebugEnabled()) {
                    LOGGER.info("3D物品渲染完成: " + itemId);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("3D渲染错误 " + itemId + ": " + e.getMessage(), e);
        }
    }
    
    private static BlockState getBlockState(String blockId) {
        try {
            Identifier identifier = Identifier.of(blockId);
            Block block = Registries.BLOCK.get(identifier);
            return block.getDefaultState();
        } catch (Exception e) {
            LOGGER.error("获取方块状态失败 " + blockId + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    private static void applyRenderSettings(MatrixStack matrices, ItemDisplayContext displayContext, 
                                          ItemConfig config) {
        ItemConfig.RenderSettings settings = getRenderSettingsForContext(displayContext, config);
        if (settings == null) return;
        
        // 应用缩放
        matrices.scale(settings.getScale(), settings.getScale(), settings.getScale());
        
        // 应用旋转
        if (settings.getRotationX() != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(settings.getRotationX()));
        }
        if (settings.getRotationY() != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(settings.getRotationY()));
        }
        if (settings.getRotationZ() != 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(settings.getRotationZ()));
        }
        
        // 应用平移
        matrices.translate(settings.getTranslateX(), settings.getTranslateY(), settings.getTranslateZ());
    }
    
    private static ItemConfig.RenderSettings getRenderSettingsForContext(ItemDisplayContext displayContext, 
                                                                        ItemConfig config) {
        String contextName = displayContext.name();
        
        if (contextName.contains("FIRST_PERSON")) {
            return config.getFirstPerson();
        } else if (contextName.contains("THIRD_PERSON")) {
            return config.getThirdPerson();
        }
        
        // 如果没有找到特定设置，返回第一人称设置作为默认值
        return config.getFirstPerson();
    }
}
