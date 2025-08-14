package com.aeolyn.better_experience.core;

import com.aeolyn.better_experience.config.ConfigManager;
import com.aeolyn.better_experience.config.ItemConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用3D物品渲染器
 * 负责根据配置渲染物品的3D模型
 */
public class ItemRenderer3D {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Renderer");
    private final ConfigManager configManager;
    
    public ItemRenderer3D(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * 渲染物品的3D模型
     */
    public void render3DItem(Item item, ItemDisplayContext displayContext, 
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                           int light, int overlay) {
        
        try {
            System.out.println("🎯 开始3D渲染物品: " + Registries.ITEM.getId(item));
            
            // 检查物品是否启用3D渲染
            if (!configManager.isItemEnabled(Registries.ITEM.getId(item).toString())) {
                System.out.println("❌ 物品未启用3D渲染，回退到原版渲染");
                return;
            }
            
            // 获取渲染配置
            ItemConfig config = configManager.getItemConfig(Registries.ITEM.getId(item).toString());
            if (config == null || !config.isEnabled()) {
                System.out.println("❌ 渲染配置无效，回退到原版渲染");
                return;
            }
            
            // 判断是否为手持模式
            if (!isHandheldMode(displayContext)) {
                System.out.println("❌ 不是手持模式，回退到原版渲染");
                return;
            }
            
            // 获取对应的BlockState
            BlockState blockState = getBlockStateForItem(item, config);
            if (blockState == null) {
                System.out.println("❌ 无法获取方块状态，回退到原版渲染");
                LOGGER.warn("无法为物品 {} 找到对应的方块状态", Registries.ITEM.getId(item));
                return;
            }
            
            // 应用矩阵变换
            applyMatrixTransform(matrices, displayContext, config);
            
            // 渲染3D模型
            renderBlockModel(blockState, matrices, vertexConsumers, light, overlay);
            
            System.out.println("✅ 3D渲染完成: " + Registries.ITEM.getId(item));
            
        } catch (Exception e) {
            System.out.println("❌ 3D渲染发生错误: " + e.getMessage());
            LOGGER.error("渲染3D物品时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查是否为手持模式
     */
    private boolean isHandheldMode(ItemDisplayContext displayContext) {
        String contextName = displayContext.name();
        return contextName.contains("FIRST_PERSON_LEFT_HAND") ||
               contextName.contains("FIRST_PERSON_RIGHT_HAND") ||
               contextName.contains("THIRD_PERSON_LEFT_HAND") ||
               contextName.contains("THIRD_PERSON_RIGHT_HAND");
    }
    
    /**
     * 根据物品获取对应的方块状态
     */
    private BlockState getBlockStateForItem(Item item, ItemConfig config) {
        System.out.println("=== 获取方块状态 ===");
        System.out.println("物品: " + Registries.ITEM.getId(item));
        
        if (!config.isRenderAsBlock()) {
            System.out.println("❌ 物品配置为不渲染为方块");
            return null;
        }
        
        String blockId = config.getBlockId();
        System.out.println("配置中的blockId: '" + blockId + "'");
        System.out.println("配置是否启用: " + config.isEnabled());
        
        if (blockId == null || blockId.isEmpty()) {
            System.out.println("❌ blockId为空或空字符串，回退到原版渲染");
            System.out.println("==================");
            LOGGER.warn("物品配置中blockId为空或空字符串: {}", Registries.ITEM.getId(item));
            return null;
        }
        
        System.out.println("✅ 使用方块: " + blockId);
        LOGGER.info("渲染物品 {} 使用方块: {}", Registries.ITEM.getId(item), blockId);
        
        try {
            // 通过方块ID获取方块状态
            Identifier identifier = Identifier.of(blockId);
            Block block = Registries.BLOCK.get(identifier);
            BlockState state = block.getDefaultState();
            System.out.println("✅ 成功获取方块状态: " + state);
            System.out.println("==================");
            return state;
        } catch (Exception e) {
            System.out.println("❌ 无法找到方块: " + blockId);
            System.out.println("错误: " + e.getMessage());
            System.out.println("==================");
            LOGGER.warn("无法找到方块: {}", blockId);
            return null;
        }
    }
    
    /**
     * 应用矩阵变换
     */
    private void applyMatrixTransform(MatrixStack matrices, ItemDisplayContext displayContext, ItemConfig config) {
        // 判断是否为第一人称
        boolean isFirstPerson = displayContext.name().contains("FIRST_PERSON");
        ItemConfig.RenderSettings viewConfig = isFirstPerson ? config.getFirstPerson() : config.getThirdPerson();
        
        // 应用缩放
        float scale = viewConfig.getScale();
        matrices.scale(scale, scale, scale);
        
        // 应用平移
        matrices.translate(viewConfig.getTranslateX(), viewConfig.getTranslateY(), viewConfig.getTranslateZ());
        
        // 应用旋转
        if (viewConfig.getRotationX() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewConfig.getRotationX()));
        }
        if (viewConfig.getRotationY() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewConfig.getRotationY()));
        }
        if (viewConfig.getRotationZ() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewConfig.getRotationZ()));
        }
    }
    
    /**
     * 渲染方块模型
     */
    private void renderBlockModel(BlockState blockState, MatrixStack matrices, 
                                VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }
        
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        blockRenderManager.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, overlay);
    }
    
    /**
     * 检查物品是否应该被3D渲染
     */
    public boolean shouldRender3D(Item item, ItemDisplayContext displayContext) {
        System.out.println("🔍 检查是否应该3D渲染: " + Registries.ITEM.getId(item));
        
        // 检查是否为手持模式
        if (!isHandheldMode(displayContext)) {
            System.out.println("❌ 不是手持模式");
            return false;
        }
        
        // 检查物品是否启用3D渲染
        if (!configManager.isItemEnabled(Registries.ITEM.getId(item).toString())) {
            System.out.println("❌ 物品未启用3D渲染");
            return false;
        }
        
        // 检查渲染配置
        ItemConfig config = configManager.getItemConfig(Registries.ITEM.getId(item).toString());
        if (config == null || !config.isEnabled()) {
            System.out.println("❌ 渲染配置无效");
            return false;
        }
        
        System.out.println("✅ 应该进行3D渲染");
        return true;
    }
}
