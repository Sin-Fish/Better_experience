package com.aeolyn.better_experience.render3d.core;

import com.aeolyn.better_experience.common.config.manager.ConfigManager;
import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.render3d.config.ItemConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ArrowEntity;
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
     * 初始化3D渲染器
     */
    public static void initialize() {
        LogUtil.info("Render3D", "初始化3D物品渲染器");
        // 这里可以添加任何需要的初始化逻辑
        // 比如注册事件监听器、初始化缓存等
    }
    
    /**
     * 渲染物品的3D模型
     */
    public void render3DItem(Item item, ItemDisplayContext displayContext, 
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                           int light, int overlay) {
        
        try {
            // 首先检查3D渲染模块是否启用
            if (!configManager.isRender3dEnabled()) {
                LogUtil.debug("Render3D", "3D渲染模块已禁用，跳过渲染");
                return;
            }
            
            LogUtil.debug("Render3D", "开始3D渲染物品: {}", Registries.ITEM.getId(item));
            
            // 检查物品是否启用3D渲染
            if (!configManager.isItemEnabled(Registries.ITEM.getId(item).toString())) {
                LogUtil.debug("Render3D", "物品未启用3D渲染，回退到原版渲染");
                return;
            }
            
            // 获取渲染配置
            ItemConfig config = configManager.getItemConfig(Registries.ITEM.getId(item).toString());
            if (config == null || !config.isEnabled()) {
                LogUtil.debug("Render3D", "渲染配置无效，回退到原版渲染");
                return;
            }
            
            // 判断是否为手持模式
            if (!isHandheldMode(displayContext)) {
                LogUtil.debug("Render3D", "不是手持模式，回退到原版渲染");
                return;
            }
            
            // 根据配置选择渲染方式
            if (config.isRenderAsEntity()) {
                // 渲染实体模型
                Entity entity = getEntityForItem(item, config);
                if (entity != null) {
                    // 应用实体专用矩阵变换
                    applyEntityMatrixTransform(matrices, displayContext, config);
                    renderEntityModel(entity, matrices, vertexConsumers, light, overlay);
                } else {
                    LogUtil.debug("Render3D", "无法获取实体，回退到原版渲染");
                    LogUtil.warn("Render3D", "无法为物品 {} 找到对应的实体", Registries.ITEM.getId(item));
                    return;
                }
            } else if (config.isRenderAsBlock()) {
                // 渲染方块模型
                BlockState blockState = getBlockStateForItem(item, config);
                if (blockState == null) {
                    LogUtil.debug("Render3D", "无法获取方块状态，回退到原版渲染");
                    LogUtil.warn("Render3D", "无法为物品 {} 找到对应的方块状态", Registries.ITEM.getId(item));
                    return;
                }
                // 应用方块专用矩阵变换
                applyBlockMatrixTransform(matrices, displayContext, config);
                renderBlockModel(blockState, matrices, vertexConsumers, light, overlay);
            } else {
                LogUtil.debug("Render3D", "未指定渲染方式，回退到原版渲染");
                return;
            }
            
            LogUtil.debug("Render3D", "3D渲染完成: {}", Registries.ITEM.getId(item));
            
        } catch (Exception e) {
            LogUtil.error("Render3D", "渲染3D物品时发生错误: {}", e.getMessage(), e);
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
     * 根据物品获取对应的实体
     */
    private Entity getEntityForItem(Item item, ItemConfig config) {
        LogUtil.debug("Render3D", "获取实体，物品: {}", Registries.ITEM.getId(item));
        
        if (!config.isRenderAsEntity()) {
            LogUtil.debug("Render3D", "物品配置为不渲染为实体");
            return null;
        }
        
        String entityTypeId = config.getEntityType();
        LogUtil.debug("Render3D", "配置中的entityType: '{}', 配置是否启用: {}", entityTypeId, config.isEnabled());
        
        if (entityTypeId == null || entityTypeId.isEmpty()) {
            LogUtil.debug("Render3D", "entityType为空或空字符串，回退到原版渲染");
            LogUtil.warn("Render3D", "物品配置中entityType为空或空字符串: {}", Registries.ITEM.getId(item));
            return null;
        }
        
        LogUtil.debug("Render3D", "使用实体类型: {}", entityTypeId);
        LogUtil.info("Render3D", "渲染物品 {} 使用实体: {}", Registries.ITEM.getId(item), entityTypeId);
        
        try {
            // 通过实体类型ID获取实体类型
            Identifier identifier = Identifier.of(entityTypeId);
            EntityType<?> entityType = Registries.ENTITY_TYPE.get(identifier);
            
            if (entityType == null) {
                LogUtil.debug("Render3D", "无法找到实体类型: {}", entityTypeId);
                return null;
            }
            
            // 创建实体实例
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) {
                LogUtil.debug("Render3D", "世界未加载，无法创建实体");
                return null;
            }
            
            // 使用简单的create方法创建实体
            Entity entity = entityType.create(client.world, SpawnReason.NATURAL);
            if (entity == null) {
                LogUtil.debug("Render3D", "无法创建实体实例: {}", entityTypeId);
                return null;
            }
            
            // 设置实体的基本属性
            entity.setPosition(0, 0, 0);
            entity.setVelocity(0, 0, 0);
            
            LogUtil.debug("Render3D", "成功创建实体: {}", entity);
            return entity;
        } catch (Exception e) {
            LogUtil.debug("Render3D", "无法找到实体类型: {}, 错误: {}", entityTypeId, e.getMessage());
            LogUtil.warn("Render3D", "无法找到实体类型: {}", entityTypeId);
            return null;
        }
    }
    
    /**
     * 根据物品获取对应的方块状态
     */
    private BlockState getBlockStateForItem(Item item, ItemConfig config) {
        LogUtil.debug("Render3D", "获取方块状态，物品: {}", Registries.ITEM.getId(item));
        
        if (!config.isRenderAsBlock()) {
            LogUtil.debug("Render3D", "物品配置为不渲染为方块");
            return null;
        }
        
        String blockId = config.getBlockId();
        LogUtil.debug("Render3D", "配置中的blockId: '{}', 配置是否启用: {}", blockId, config.isEnabled());
        
        if (blockId == null || blockId.isEmpty()) {
            LogUtil.debug("Render3D", "blockId为空或空字符串，回退到原版渲染");
            LogUtil.warn("Render3D", "物品配置中blockId为空或空字符串: {}", Registries.ITEM.getId(item));
            return null;
        }
        
        LogUtil.debug("Render3D", "使用方块: {}", blockId);
        LogUtil.info("Render3D", "渲染物品 {} 使用方块: {}", Registries.ITEM.getId(item), blockId);
        
        try {
            // 通过方块ID获取方块状态
            Identifier identifier = Identifier.of(blockId);
            Block block = Registries.BLOCK.get(identifier);
            BlockState state = block.getDefaultState();
            LogUtil.debug("Render3D", "成功获取方块状态: {}", state);
            return state;
        } catch (Exception e) {
            LogUtil.debug("Render3D", "无法找到方块: {}, 错误: {}", blockId, e.getMessage());
            LogUtil.warn("Render3D", "无法找到方块: {}", blockId);
            return null;
        }
    }
    
    /**
     * 应用实体专用矩阵变换
     */
    private void applyEntityMatrixTransform(MatrixStack matrices, ItemDisplayContext displayContext, ItemConfig config) {
        // 判断是否为第一人称
        boolean isFirstPerson = displayContext.name().contains("FIRST_PERSON");
        ItemConfig.RenderSettings viewConfig = isFirstPerson ? config.getFirstPerson() : config.getThirdPerson();
        
        // 判断是否为副手
        boolean isOffhand = displayContext.name().contains("OFFHAND");
        
        // 应用缩放
        float scale = viewConfig.getScale();
        matrices.scale(scale, scale, scale);
        
        // 应用平移
        matrices.translate(viewConfig.getTranslateX(), viewConfig.getTranslateY(), viewConfig.getTranslateZ());
        
        // 应用旋转 - 注意旋转顺序很重要
        if (viewConfig.getRotationX() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewConfig.getRotationX()));
        }
        if (viewConfig.getRotationY() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewConfig.getRotationY()));
        }
        if (viewConfig.getRotationZ() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewConfig.getRotationZ()));
        }
        
        // 副手处理 - 实体使用更温和的镜像和位置补偿
        if (isOffhand) {
            // 对X轴进行镜像变换
            matrices.scale(-1.0f, 1.0f, 1.0f);
            
            // 副手位置补偿 - 将实体拉回屏幕内
            matrices.translate(-0.3f, 0.0f, 0.0f);
        }
    }
    
    /**
     * 应用方块专用矩阵变换
     */
    private void applyBlockMatrixTransform(MatrixStack matrices, ItemDisplayContext displayContext, ItemConfig config) {
        // 判断是否为第一人称
        boolean isFirstPerson = displayContext.name().contains("FIRST_PERSON");
        ItemConfig.RenderSettings viewConfig = isFirstPerson ? config.getFirstPerson() : config.getThirdPerson();
        
        // 判断是否为副手
        boolean isOffhand = displayContext.name().contains("OFFHAND");
        
        // 应用缩放
        float scale = viewConfig.getScale();
        matrices.scale(scale, scale, scale);
        
        // 应用平移
        matrices.translate(viewConfig.getTranslateX(), viewConfig.getTranslateY(), viewConfig.getTranslateZ());
        
        // 应用旋转 - 注意旋转顺序很重要
        if (viewConfig.getRotationX() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewConfig.getRotationX()));
        }
        if (viewConfig.getRotationY() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewConfig.getRotationY()));
        }
        if (viewConfig.getRotationZ() != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewConfig.getRotationZ()));
        }
        
        // 副手镜像处理 - 方块使用标准镜像
        if (isOffhand) {
            matrices.scale(-1.0f, 1.0f, 1.0f);
        }
    }
    
    /**
     * 渲染实体模型
     */
    private void renderEntityModel(Entity entity, MatrixStack matrices, 
                                 VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }
        
        // 特殊处理箭的旋转 - 将水平箭转为垂直
        if (entity instanceof ArrowEntity) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        }
        
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, matrices, vertexConsumers, light);
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
        LogUtil.debug("Render3D", "检查是否应该3D渲染: {}", Registries.ITEM.getId(item));
        
        // 检查是否为手持模式
        if (!isHandheldMode(displayContext)) {
            LogUtil.debug("Render3D", "不是手持模式");
            return false;
        }
        
        // 检查物品是否启用3D渲染
        if (!configManager.isItemEnabled(Registries.ITEM.getId(item).toString())) {
            LogUtil.debug("Render3D", "物品未启用3D渲染");
            return false;
        }
        
        // 检查渲染配置
        ItemConfig config = configManager.getItemConfig(Registries.ITEM.getId(item).toString());
        if (config == null || !config.isEnabled()) {
            LogUtil.debug("Render3D", "渲染配置无效");
            return false;
        }
        
        LogUtil.debug("Render3D", "应该进行3D渲染");
        return true;
    }
}
