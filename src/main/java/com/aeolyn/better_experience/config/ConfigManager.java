package com.aeolyn.better_experience.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetterExperience-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 线程安全的读写锁
    private static final ReadWriteLock configLock = new ReentrantReadWriteLock();
    
    // 配置文件路径
    private static final String CONFIG_DIR = "config/better_experience";
    private static final String ITEMS_CONFIG_FILE = "items.json";
    private static final String ITEM_CONFIGS_DIR = "item_configs";
    
    private static volatile ConfigManager instance;
    private static volatile boolean initialized = false;
    
    private ItemsConfig itemsConfig;
    private Map<String, ItemConfig> itemConfigs;
    
    private ConfigManager() {
        this.itemConfigs = new HashMap<>();
        this.itemsConfig = new ItemsConfig();
    }
    
    /**
     * 线程安全的单例模式实现
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化配置管理器
     */
    public static void initialize() {
        if (initialized) return;
        
        synchronized (ConfigManager.class) {
            if (initialized) return;
            
            try {
                ConfigManager manager = getInstance();
                manager.loadItemsConfig();
                manager.loadItemConfigs();
                initialized = true;
                LOGGER.info("配置管理器初始化完成");
            } catch (Exception e) {
                LOGGER.error("配置管理器初始化失败: " + e.getMessage(), e);
                throw new RuntimeException("配置管理器初始化失败", e);
            }
        }
    }
    
    /**
     * 加载主配置文件
     */
    private void loadItemsConfig() {
        configLock.writeLock().lock();
        try {
            Path configPath = getConfigPath(ITEMS_CONFIG_FILE);
            
            // 如果配置文件不存在，从资源文件复制默认配置
            if (!Files.exists(configPath)) {
                createDefaultItemsConfig(configPath);
            }
            
            // 读取配置文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                itemsConfig = GSON.fromJson(reader, ItemsConfig.class);
                if (itemsConfig == null) {
                    itemsConfig = new ItemsConfig();
                }
                LOGGER.info("主配置文件加载成功: {}", configPath);
            }
        } catch (Exception e) {
            LOGGER.error("加载主配置文件失败: " + e.getMessage(), e);
            itemsConfig = new ItemsConfig();
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 创建默认主配置文件
     */
    private void createDefaultItemsConfig(Path configPath) throws IOException {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 从资源文件读取默认配置
            try (InputStream resourceStream = getClass().getClassLoader()
                    .getResourceAsStream("assets/better_experience/config/items.json")) {
                
                if (resourceStream != null) {
                    // 复制默认配置到配置文件
                    Files.copy(resourceStream, configPath);
                    LOGGER.info("创建默认主配置文件: {}", configPath);
                } else {
                    // 创建空的默认配置
                    itemsConfig = new ItemsConfig();
                    saveItemsConfig();
                }
            }
        } catch (Exception e) {
            LOGGER.error("创建默认主配置文件失败: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 加载所有物品配置
     */
    private void loadItemConfigs() {
        configLock.writeLock().lock();
        try {
            itemConfigs.clear();
            
            for (String itemId : itemsConfig.getEnabledItems()) {
                try {
                    loadItemConfig(itemId);
                } catch (Exception e) {
                    LOGGER.error("加载物品配置失败 " + itemId + ": " + e.getMessage(), e);
                }
            }
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 加载单个物品配置
     */
    private void loadItemConfig(String itemId) throws IOException {
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(ITEM_CONFIGS_DIR, fileName);
        
        // 如果配置文件不存在，从资源文件复制默认配置
        if (!Files.exists(configPath)) {
            createDefaultItemConfig(itemId, configPath);
        }
        
        // 读取配置文件
        try (Reader reader = Files.newBufferedReader(configPath)) {
            ItemConfig config = GSON.fromJson(reader, ItemConfig.class);
            if (config != null) {
                itemConfigs.put(itemId, config);
                LOGGER.info("物品配置加载成功: {}", itemId);
            } else {
                LOGGER.warn("物品配置文件为空: {}", itemId);
            }
        }
    }
    
    /**
     * 创建默认物品配置文件
     */
    private void createDefaultItemConfig(String itemId, Path configPath) throws IOException {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 从资源文件读取默认配置
            String resourcePath = "assets/better_experience/config/item_configs/" + itemId.replace(":", "_") + ".json";
            try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                
                if (resourceStream != null) {
                    // 复制默认配置到配置文件
                    Files.copy(resourceStream, configPath);
                    LOGGER.info("创建默认物品配置文件: {}", configPath);
                } else {
                    // 创建空的默认配置
                    ItemConfig defaultConfig = createDefaultItemConfig(itemId);
                    saveItemConfig(itemId, defaultConfig);
                }
            }
        } catch (Exception e) {
            LOGGER.error("创建默认物品配置文件失败 " + itemId + ": " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建默认物品配置
     */
    private ItemConfig createDefaultItemConfig(String itemId) {
        ItemConfig config = new ItemConfig();
        config.setItemId(itemId);
        config.setEnabled(true);
        config.setRenderAsBlock(true);
        config.setBlockId(itemId);
        
        // 设置默认渲染参数
        ItemConfig.RenderSettings firstPerson = new ItemConfig.RenderSettings();
        firstPerson.setScale(1.0f);
        firstPerson.setRotationX(0.0f);
        firstPerson.setRotationY(0.0f);
        firstPerson.setRotationZ(0.0f);
        firstPerson.setTranslateX(0.0f);
        firstPerson.setTranslateY(0.0f);
        firstPerson.setTranslateZ(0.0f);
        config.setFirstPerson(firstPerson);
        
        ItemConfig.RenderSettings thirdPerson = new ItemConfig.RenderSettings();
        thirdPerson.setScale(1.0f);
        thirdPerson.setRotationX(0.0f);
        thirdPerson.setRotationY(0.0f);
        thirdPerson.setRotationZ(0.0f);
        thirdPerson.setTranslateX(0.0f);
        thirdPerson.setTranslateY(0.0f);
        thirdPerson.setTranslateZ(0.0f);
        config.setThirdPerson(thirdPerson);
        
        return config;
    }
    
    /**
     * 保存主配置文件
     */
    private void saveItemsConfig() throws IOException {
        Path configPath = getConfigPath(ITEMS_CONFIG_FILE);
        Files.createDirectories(configPath.getParent());
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(itemsConfig, writer);
            LOGGER.info("主配置文件保存成功: {}", configPath);
        }
    }
    
    /**
     * 保存物品配置文件
     */
    private void saveItemConfig(String itemId, ItemConfig config) throws IOException {
        String fileName = itemId.replace(":", "_") + ".json";
        Path configPath = getConfigPath(ITEM_CONFIGS_DIR, fileName);
        Files.createDirectories(configPath.getParent());
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
            LOGGER.info("物品配置文件保存成功: {}", configPath);
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private Path getConfigPath(String... parts) {
        return Paths.get(CONFIG_DIR, parts);
    }
    
    /**
     * 检查物品是否启用
     */
    public boolean isItemEnabled(String itemId) {
        configLock.readLock().lock();
        try {
            return itemsConfig != null && itemsConfig.getEnabledItems().contains(itemId);
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 获取物品配置
     */
    public ItemConfig getItemConfig(String itemId) {
        configLock.readLock().lock();
        try {
            return itemConfigs.get(itemId);
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 检查调试模式是否启用
     */
    public boolean isDebugEnabled() {
        configLock.readLock().lock();
        try {
            return itemsConfig != null && itemsConfig.getSettings().isEnableDebugLogs();
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 获取默认设置
     */
    public ItemsConfig.Settings getDefaultSettings() {
        configLock.readLock().lock();
        try {
            return itemsConfig != null ? itemsConfig.getSettings() : null;
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        synchronized (ConfigManager.class) {
            initialized = false;
            loadItemsConfig();
            loadItemConfigs();
            initialized = true;
            LOGGER.info("配置重新加载完成");
        }
    }
    
    /**
     * 获取所有已配置的物品
     */
    public java.util.Set<String> getAllConfiguredItems() {
        configLock.readLock().lock();
        try {
            return new java.util.HashSet<>(itemConfigs.keySet());
        } finally {
            configLock.readLock().unlock();
        }
    }
    
    /**
     * 保存物品配置到文件
     */
    public void saveConfig(String itemId) {
        configLock.writeLock().lock();
        try {
            ItemConfig config = itemConfigs.get(itemId);
            if (config != null) {
                saveItemConfig(itemId, config);
                LOGGER.info("物品配置保存成功: {}", itemId);
            } else {
                LOGGER.warn("物品配置不存在，无法保存: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("保存物品配置失败 " + itemId + ": " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 保存所有配置
     */
    public void saveAllConfigs() {
        configLock.writeLock().lock();
        try {
            // 保存主配置文件
            saveItemsConfig();
            
            // 保存所有物品配置文件
            for (String itemId : itemConfigs.keySet()) {
                try {
                    saveItemConfig(itemId, itemConfigs.get(itemId));
                } catch (Exception e) {
                    LOGGER.error("保存物品配置失败 " + itemId + ": " + e.getMessage(), e);
                }
            }
            LOGGER.info("所有配置保存完成");
        } catch (Exception e) {
            LOGGER.error("保存所有配置失败: " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 添加新的物品配置
     */
    public void addItemConfig(String itemId, ItemConfig config) {
        configLock.writeLock().lock();
        try {
            // 添加到内存中的配置
            itemConfigs.put(itemId, config);
            
            // 添加到启用的物品列表
            if (!itemsConfig.getEnabledItems().contains(itemId)) {
                itemsConfig.getEnabledItems().add(itemId);
            }
            
            // 保存到文件
            saveItemConfig(itemId, config);
            saveItemsConfig();
            
            LOGGER.info("成功添加物品配置: {}", itemId);
        } catch (Exception e) {
            LOGGER.error("添加物品配置失败 " + itemId + ": " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 删除物品配置
     */
    public void removeItemConfig(String itemId) {
        configLock.writeLock().lock();
        try {
            // 从内存中的配置移除
            itemConfigs.remove(itemId);
            
            // 从启用的物品列表移除
            itemsConfig.getEnabledItems().remove(itemId);
            
            // 删除配置文件
            String fileName = itemId.replace(":", "_") + ".json";
            Path configPath = getConfigPath(ITEM_CONFIGS_DIR, fileName);
            if (Files.exists(configPath)) {
                Files.delete(configPath);
            }
            
            // 保存主配置文件
            saveItemsConfig();
            
            LOGGER.info("成功删除物品配置: {}", itemId);
        } catch (Exception e) {
            LOGGER.error("删除物品配置失败 " + itemId + ": " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 更新物品配置
     */
    public void updateItemConfig(String itemId, ItemConfig config) {
        configLock.writeLock().lock();
        try {
            itemConfigs.put(itemId, config);
            saveItemConfig(itemId, config);
            LOGGER.info("物品配置更新成功: {}", itemId);
        } catch (Exception e) {
            LOGGER.error("更新物品配置失败 " + itemId + ": " + e.getMessage(), e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    
    /**
     * 测试配置持久化功能
     */
    public void testConfigPersistence() {
        LOGGER.info("开始测试配置持久化功能...");
        
        try {
            // 创建一个测试配置
            ItemConfig testConfig = createDefaultItemConfig("minecraft:test_item");
            testConfig.getFirstPerson().setScale(2.5f);
            testConfig.getFirstPerson().setRotationX(45.0f);
            
            // 保存测试配置
            addItemConfig("minecraft:test_item", testConfig);
            
            // 重新加载配置
            reload();
            
            // 验证配置是否正确加载
            ItemConfig loadedConfig = getItemConfig("minecraft:test_item");
            if (loadedConfig != null && 
                loadedConfig.getFirstPerson().getScale() == 2.5f &&
                loadedConfig.getFirstPerson().getRotationX() == 45.0f) {
                LOGGER.info("配置持久化测试成功！");
            } else {
                LOGGER.error("配置持久化测试失败！");
            }
            
            // 清理测试配置
            removeItemConfig("minecraft:test_item");
            
        } catch (Exception e) {
            LOGGER.error("配置持久化测试失败: " + e.getMessage(), e);
        }
    }
}
