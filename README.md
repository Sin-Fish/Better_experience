# Handheld3D - 通用3D物品渲染模组

这是一个通用的Minecraft Fabric模组，可以为手持物品提供3D渲染效果。

## 功能特性

- 🎯 **通用渲染系统**: 支持任何物品的3D渲染，不再局限于特定物品
- ⚙️ **JSON配置**: 所有配置都通过JSON文件管理，无需修改代码
- 🔧 **灵活设置**: 每个物品都可以独立配置渲染参数
- 🎮 **多视角支持**: 支持第一人称和第三人称的不同渲染设置

## 配置说明

### 主配置文件 (`assets/handheld3d/config/items.json`)

```json
{
  "enabled_items": [
    "minecraft:lantern",
    "minecraft:soul_lantern",
    "minecraft:torch",
    "minecraft:soul_torch",
    "minecraft:redstone_torch"
  ],
  "settings": {
    "enable_debug_logs": true,
    "default_scale": 1.0,
    "default_rotation_x": 0.0,
    "default_rotation_y": 0.0,
    "default_rotation_z": 0.0,
    "default_translate_x": 0.0,
    "default_translate_y": 0.0,
    "default_translate_z": 0.0
  }
}
```

### 物品配置文件 (`assets/handheld3d/config/item_configs/`)

每个物品都有独立的配置文件，例如 `minecraft_lantern.json`:

```json
{
  "item_id": "minecraft:lantern",
  "enabled": true,
  "render_as_block": true,
  "block_id": "minecraft:lantern",
  "first_person": {
    "scale": 1.2,
    "rotation_x": 0.0,
    "rotation_y": 0.0,
    "rotation_z": 0.0,
    "translate_x": 0.0,
    "translate_y": -0.2,
    "translate_z": 0.0
  },
  "third_person": {
    "scale": 1.0,
    "rotation_x": 90.0,
    "rotation_y": 0.0,
    "rotation_z": 0.0,
    "translate_x": 0.0,
    "translate_y": 0.7,
    "translate_z": 0.0
  }
}
```

## 配置参数说明

### 主配置参数
- `enabled_items`: 启用3D渲染的物品ID列表
- `enable_debug_logs`: 是否启用调试日志
- `default_*`: 默认渲染参数（当物品没有特定配置时使用）

### 物品配置参数
- `item_id`: 物品的完整ID（如 "minecraft:lantern"）
- `enabled`: 是否启用该物品的3D渲染
- `render_as_block`: 是否渲染为方块（而不是物品）
- `block_id`: 对应的方块ID（当render_as_block为true时使用）

### 渲染设置参数
- `scale`: 缩放比例
- `rotation_x/y/z`: 绕X/Y/Z轴的旋转角度（度）
- `translate_x/y/z`: 在X/Y/Z轴上的平移距离

## 添加新物品

1. 在 `items.json` 的 `enabled_items` 数组中添加物品ID
2. 在 `item_configs/` 文件夹中创建对应的配置文件
3. 配置文件命名格式：`物品ID.replace(":", "_") + ".json"`

例如，添加 `minecraft:campfire`:
- 在 `enabled_items` 中添加 `"minecraft:campfire"`
- 创建文件 `minecraft_campfire.json`

## 构建和安装

1. 克隆项目
2. 运行 `./gradlew build`
3. 将生成的jar文件放入mods文件夹

## 兼容性

- Minecraft: 1.21.x
- Fabric Loader: 0.15.0+
- Fabric API: 0.91.0+

## 许可证

本项目采用MIT许可证。
