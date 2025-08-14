# Better Experience Mod

一个为Minecraft 1.21.6设计的Fabric mod，提供多种游戏体验优化功能，包括3D手持物品渲染等。

## 🎯 当前状态

### ✅ 已完成功能
- **项目构建系统** - 完整的Gradle构建配置
- **基础框架** - Fabric mod基础结构
- **Mixin系统** - 物品渲染拦截框架
- **通用3D渲染** - 基于JSON配置的通用3D物品渲染系统
- **配置管理** - 完全由JSON文件管理的配置系统
- **日志系统** - 详细的调试日志输出

### 🔧 技术实现
- **Minecraft版本**: 1.21.6
- **Fabric Loader**: 0.16.13
- **Fabric API**: 0.128.1+1.21.6
- **Java版本**: 17+
- **Gradle版本**: 8.6

## 🏗️ 项目结构

```
BetterExperience/
├── build.gradle                    # 构建配置
├── gradle.properties              # 项目属性
├── README.md                      # 项目文档
├── src/main/java/com/aeolyn/better_experience/
│   ├── BetterExperienceMod.java   # 主mod类
│   ├── config/
│   │   ├── ConfigManager.java     # 配置管理器
│   │   ├── ItemsConfig.java       # 物品配置类
│   │   └── ItemConfig.java        # 单个物品配置类
│   ├── core/
│   │   └── ItemRenderer3D.java    # 3D渲染器
│   └── mixin/
│       └── GenericItemRendererMixin.java  # 通用物品渲染mixin
└── src/main/resources/
    ├── fabric.mod.json            # mod元数据
    ├── better_experience.mixins.json  # mixin配置
    └── assets/better_experience/
        ├── config/
        │   ├── items.json         # 主配置文件
        │   └── item_configs/      # 物品配置文件夹
        │       ├── minecraft_lantern.json
        │       ├── minecraft_torch.json
        │       └── ...
        └── lang/                  # 语言文件
```

## 🚀 开发环境

### 构建项目
```bash
# 克隆项目
git clone https://github.com/Aeolyn/better_experience.git
cd better_experience

# 构建项目
./gradlew build

# 运行开发环境
./gradlew runClient
```

### 构建输出
构建成功后，jar文件位于：
```
build/libs/better_experience-1.0.0.jar
```

## 📋 功能特性

### 3D手持物品渲染
- **通用渲染系统**: 支持任何物品的3D渲染
- **JSON配置**: 所有参数通过JSON文件管理
- **灵活设置**: 支持缩放、旋转、平移等变换
- **多视角支持**: 第一人称和第三人称独立配置

### 配置系统
- **items.json**: 主配置文件，列出启用的物品
- **item_configs/**: 各个物品的具体渲染配置
- **热重载**: 支持运行时重新加载配置
- **默认值**: 提供合理的默认配置

## 🔧 配置说明

### 主配置文件 (items.json)
```json
{
  "enabled_items": [
    "minecraft:lantern",
    "minecraft:torch",
    "minecraft:soul_torch"
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

### 物品配置文件 (item_configs/minecraft_lantern.json)
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

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

**Aeolyn** - [GitHub](https://github.com/Tanfreefish)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- GitHub: [@Aeolyn](https://github.com/Tanfreefish)
- 项目主页: [Better Experience](https://github.com/Aeolyn/better_experience)
