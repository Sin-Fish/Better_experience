# Better Experience Mod

一个为Minecraft 1.21.6设计的Fabric mod，提供多种游戏体验优化功能，包括3D手持物品渲染等。

## 🎯 当前状态

### ✅ 已完成功能
- **项目构建系统** - 完整的Gradle构建配置
- **基础框架** - Fabric mod基础结构
- **Mixin系统** - 物品渲染拦截框架
- **通用3D渲染** - 基于JSON配置的通用3D物品渲染系统
- **副手限制系统** - 可配置的副手物品使用和方块放置限制
- **统一配置界面** - 通过B键或命令打开的统一配置入口
- **配置管理** - 完全由JSON文件管理的配置系统
- **配置导入导出** - 支持配置的导出和导入，便于配置共享
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
git clone https://github.com/Tanfreefish/better_experience.git
cd better_experience

# 构建项目
./gradlew build

# 运行开发环境
./gradlew runClient
```

### 构建输出
构建成功后，jar文件位于：
```
build/libs/better_experience-1.2.0.jar
```

## 📋 功能特性

### 3D手持物品渲染
- **通用渲染系统**: 支持任何物品的3D渲染
- **JSON配置**: 所有参数通过JSON文件管理
- **灵活设置**: 支持缩放、旋转、平移等变换
- **多视角支持**: 第一人称和第三人称独立配置

### 副手限制系统
- **双重限制**: 可分别控制副手物品使用和方块放置
- **白名单机制**: 只有明确允许的物品才能在副手使用
- **静默拦截**: 被阻止的操作无提示、无声音
- **实时生效**: 配置修改后立即生效，无需重启游戏

### 统一配置界面
- **一键访问**: 按B键或使用命令`/betterexperience config`打开
- **模块化设计**: 3D渲染和副手限制独立配置
- **直观界面**: 齿轮图标表示配置选项，物品图标显示真实外观
- **便捷导航**: 完整的返回和保存功能

### 配置系统
- **items.json**: 主配置文件，列出启用的物品
- **item_configs/**: 各个物品的具体渲染配置
- **offhand_restrictions.json**: 副手限制配置文件
- **热重载**: 支持运行时重新加载配置
- **默认值**: 提供合理的默认配置
- **导入导出**: 支持配置的导出和导入，便于在不同实例间共享配置

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

## 🔄 配置导入导出

### 使用方法

1. **导出配置**
   - 打开配置界面（`/betterexperience config`）
   - 点击"导入导出配置"按钮
   - 输入导出路径（如：`./config_export`）
   - 点击"导出配置"按钮

2. **导入配置**
   - 在导入界面输入配置目录路径
   - 点击"验证导入配置"检查配置有效性
   - 点击"导入配置"应用配置

### 导出格式

导出的配置格式与项目资源完全一致：
```
config_export/
├── items.json              # 主配置文件
└── item_configs/           # 物品配置目录
    ├── minecraft_lantern.json
    ├── minecraft_torch.json
    └── ...
```

### 功能特性

- ✅ **完整导出**: 导出所有配置文件和目录结构
- ✅ **格式兼容**: 与项目资源格式完全一致
- ✅ **验证功能**: 导入前验证配置有效性
- ✅ **错误处理**: 详细的错误报告和日志记录
- ✅ **配置共享**: 支持在不同Minecraft实例间共享配置

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

**Aeolyn** - [GitHub](https://github.com/Tanfreefish)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- GitHub: [@Aeolyn](https://github.com/Tanfreefish)
- 项目主页: [Better Experience](https://github.com/Tanfreefish/better_experience)
