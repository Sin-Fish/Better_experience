# Better Experience Mod

一个为Minecraft 1.21.6设计的Fabric mod，提供多种游戏体验优化功能，包括3D手持物品渲染、副手限制系统等。

A Fabric mod designed for Minecraft 1.21.6, providing various game experience optimization features including 3D handheld item rendering, offhand restriction system, and more.

## 🎯 当前状态 / Current Status

### ✅ 已完成功能 / Completed Features
- **项目构建系统** - 完整的Gradle构建配置
- **基础框架** - Fabric mod基础结构
- **Mixin系统** - 物品渲染拦截框架
- **通用3D渲染** - 基于JSON配置的通用3D物品渲染系统
- **副手限制系统** - 可配置的副手物品使用和方块放置限制
- **统一配置界面** - 通过B键或命令打开的统一配置入口
- **配置管理** - 完全由JSON文件管理的配置系统
- **配置导入导出** - 支持配置的导出和导入，便于配置共享
- **日志系统** - 详细的调试日志输出
- **按键绑定** - 支持自定义按键打开配置界面

- **Project Build System** - Complete Gradle build configuration
- **Base Framework** - Fabric mod base structure
- **Mixin System** - Item rendering interception framework
- **Universal 3D Rendering** - JSON-based universal 3D item rendering system
- **Offhand Restriction System** - Configurable offhand item usage and block placement restrictions
- **Unified Configuration Interface** - Unified configuration entry via B key or command
- **Configuration Management** - Configuration system fully managed by JSON files
- **Configuration Import/Export** - Support for configuration export and import for easy sharing
- **Logging System** - Detailed debug log output
- **Key Bindings** - Support for custom keys to open configuration interface

### 🔧 技术实现 / Technical Implementation
- **Minecraft版本**: 1.21.0-1.21.10 (完全支持)
- **Fabric Loader**: 0.16.14
- **Fabric API**: 0.128.1+
- **Java版本**: 17+
- **Gradle版本**: 8.6
- **Mod版本**: 1.2.4

- **Minecraft Version**: 1.21.0-1.21.10 (Fully Supported)
- **Fabric Loader**: 0.16.14
- **Fabric API**: 0.128.1+
- **Java Version**: 17+
- **Gradle Version**: 8.6
- **Mod Version**: 1.2.4

### ⚠️ 版本兼容性说明 / Version Compatibility Notice
- **完全支持**: Minecraft 1.21.0-1.21.10 (已充分测试)
- **推荐版本**: Minecraft 1.21.10 (最新稳定版)
- **不支持**: Minecraft 1.20.x 及以下版本

- **Fully Supported**: Minecraft 1.21.0-1.21.10 (fully tested)
- **Recommended Version**: Minecraft 1.21.10 (latest stable)
- **Not Supported**: Minecraft 1.20.x and below

## 🏗️ 项目结构 / Project Structure

```
BetterExperience/
├── build.gradle                    # 构建配置 / Build configuration
├── gradle.properties              # 项目属性 / Project properties
├── README.md                      # 项目文档 / Project documentation
├── src/main/java/com/aeolyn/better_experience/
│   ├── BetterExperienceMod.java   # 主mod类 / Main mod class
│   ├── BetterExperienceClientMod.java  # 客户端mod类 / Client mod class
│   ├── client/
│   │   ├── command/
│   │   │   └── ConfigCommand.java     # 配置命令 / Configuration command
│   │   ├── gui/
│   │   │   ├── BaseConfigScreen.java  # 基础配置界面 / Base configuration screen
│   │   │   ├── ModConfigScreen.java   # 主配置界面 / Main configuration screen
│   │   │   ├── Render3DConfigScreen.java  # 3D渲染配置界面 / 3D rendering config screen
│   │   │   └── OffHandRestrictionConfigScreen.java  # 副手限制配置界面 / Offhand restriction config screen
│   │   └── KeyBindings.java       # 按键绑定 / Key bindings
│   ├── common/
│   │   ├── config/
│   │   │   ├── manager/
│   │   │   │   ├── ConfigManager.java     # 配置管理器门面 / Config manager facade
│   │   │   │   └── ConfigManagerImpl.java # 配置管理器实现 / Config manager implementation
│   │   │   ├── cache/
│   │   │   │   ├── ConfigCache.java       # 配置缓存 / Configuration cache
│   │   │   │   └── MemoryConfigCache.java # 内存配置缓存 / Memory config cache
│   │   │   ├── validator/
│   │   │   │   ├── ConfigValidator.java   # 配置验证器 / Configuration validator
│   │   │   │   └── ValidationResult.java  # 验证结果 / Validation result
│   │   │   └── exception/
│   │   │       ├── ConfigException.java   # 配置异常 / Configuration exception
│   │   │       └── ConfigLoadException.java # 配置加载异常 / Config load exception
│   │   └── util/
│   │       └── LogUtil.java       # 统一日志工具 / Unified logging utility
│   ├── render3d/
│   │   ├── core/
│   │   │   └── ItemRenderer3D.java    # 3D渲染器 / 3D renderer
│   │   ├── config/
│   │   │   ├── ItemsConfig.java       # 物品配置类 / Items configuration class
│   │   │   └── ItemConfig.java        # 单个物品配置类 / Single item config class
│   │   ├── gui/
│   │   │   ├── Render3DConfigScreen.java  # 3D渲染配置界面 / 3D rendering config screen
│   │   │   ├── AddItemConfigScreen.java   # 添加物品配置界面 / Add item config screen
│   │   │   └── ItemDetailConfigScreen.java # 物品详情配置界面 / Item detail config screen
│   │   ├── loader/
│   │   │   └── Render3DConfigLoader.java  # 3D渲染配置加载器 / 3D rendering config loader
│   │   └── saver/
│   │       └── Render3DConfigSaver.java   # 3D渲染配置保存器 / 3D rendering config saver
│   ├── offhand/
│   │   ├── core/
│   │   │   └── OffHandRestrictionController.java  # 副手限制控制器 / Offhand restriction controller
│   │   ├── config/
│   │   │   └── OffHandRestrictionConfig.java      # 副手限制配置 / Offhand restriction config
│   │   ├── gui/
│   │   │   ├── OffHandRestrictionConfigScreen.java # 副手限制配置界面 / Offhand restriction config screen
│   │   │   └── AddOffHandItemScreen.java           # 添加副手物品界面 / Add offhand item screen
│   │   ├── loader/
│   │   │   └── OffHandConfigLoader.java           # 副手配置加载器 / Offhand config loader
│   │   └── saver/
│   │       └── OffHandConfigSaver.java            # 副手配置保存器 / Offhand config saver
│   ├── importexport/
│   │   ├── core/
│   │   │   └── ConfigImportExportManager.java     # 配置导入导出管理器 / Config import/export manager
│   │   └── gui/
│   │       └── ConfigImportExportScreen.java      # 配置导入导出界面 / Config import/export screen
│   └── mixin/
│       ├── render3d/
│       │   └── GenericItemRendererMixin.java      # 通用物品渲染mixin / Generic item renderer mixin
│       └── offhand/
│           └── OffHandRestrictionMixin.java       # 副手限制mixin / Offhand restriction mixin
└── src/main/resources/
    ├── fabric.mod.json            # mod元数据 / Mod metadata
    ├── better_experience.mixins.json  # mixin配置 / Mixin configuration
    └── assets/better_experience/
        ├── lang/                  # 语言文件 / Language files
        │   ├── en_us.json        # 英文 / English
        │   └── zh_cn.json        # 中文 / Chinese
        ├── render3d/
        │   ├── items.json        # 主配置文件 / Main configuration file
        │   └── item_configs/     # 物品配置文件夹 (35个物品) / Item config folder (35 items)
        │       ├── minecraft_lantern.json
        │       ├── minecraft_torch.json
        │       ├── minecraft_soul_torch.json
        │       ├── minecraft_redstone_torch.json
        │       ├── minecraft_campfire.json
        │       └── ... (共35个物品配置) / ... (35 item configurations total)
        └── offhand/
            └── offhand_restrictions.json  # 副手限制配置文件 / Offhand restrictions config file
```

## 🚀 开发环境 / Development Environment

### 构建项目 / Building the Project
```bash
# 克隆项目 / Clone the project
git clone https://github.com/Tanfreefish/better_experience.git
cd better_experience

# 验证版本兼容性（不启动Minecraft）
./gradlew checkVersionCompatibility

# 构建项目 / Build the project
./gradlew build

# 运行开发环境 / Run development environment
./gradlew runClient
```

### 测试 / Testing

#### 快速测试（推荐）
```bash
# 运行所有测试
./gradlew runTests

# 这个命令会：
# 1. 检查项目配置和文件完整性
# 2. 分析当前版本兼容性
# 3. 生成测试报告
```

#### 单独测试
```bash
# 快速检查项目配置
./gradlew quickCheck

# 兼容性分析
./gradlew compatibilityAnalysis

# 生成测试报告
./gradlew generateReport
```

#### 实际游戏测试
```bash
# 启动Minecraft进行实际测试
./gradlew runClient

# 测试步骤：
# 1. 检查启动日志
# 2. 测试3D渲染功能
# 3. 测试副手限制功能
# 4. 测试配置界面
```

#### 测试报告
测试完成后会生成：
- `test-report.md` - 测试报告模板

### 构建输出 / Build Output
构建成功后，jar文件位于：
Build successful, jar file located at:
```
build/libs/better_experience-1.2.4.jar
```

## 📋 功能特性 / Features

### 3D手持物品渲染 / 3D Handheld Item Rendering
- **通用渲染系统**: 支持任何物品的3D渲染
- **JSON配置**: 所有参数通过JSON文件管理
- **灵活设置**: 支持缩放、旋转、平移等变换
- **多视角支持**: 第一人称和第三人称独立配置
- **35种物品支持**: 包括火把、灯笼、箭矢、矿车、植物等
- **方块渲染**: 支持将物品渲染为对应的方块模型
- **实体渲染**: 支持将物品渲染为对应的实体模型

- **Universal Rendering System**: Supports 3D rendering of any item
- **JSON Configuration**: All parameters managed through JSON files
- **Flexible Settings**: Supports scaling, rotation, translation and other transformations
- **Multi-view Support**: Independent configuration for first-person and third-person views
- **35 Item Support**: Including torches, lanterns, arrows, minecarts, plants, etc.
- **Block Rendering**: Supports rendering items as corresponding block models
- **Entity Rendering**: Supports rendering items as corresponding entity models

### 副手限制系统 / Offhand Restriction System
- **双重限制**: 可分别控制副手物品使用和方块放置
- **白名单机制**: 只有明确允许的物品才能在副手使用
- **静默拦截**: 被阻止的操作无提示、无声音
- **实时生效**: 配置修改后立即生效，无需重启游戏
- **22种物品支持**: 包括火把、灯笼、盾牌、图腾等

- **Dual Restrictions**: Can separately control offhand item usage and block placement
- **Whitelist Mechanism**: Only explicitly allowed items can be used in offhand
- **Silent Interception**: Blocked operations have no prompts or sounds
- **Real-time Effect**: Configuration changes take effect immediately without restarting the game
- **22 Item Support**: Including torches, lanterns, shields, totems, etc.

### 统一配置界面 / Unified Configuration Interface
- **一键访问**: 按B键或使用命令`/betterexperience config`打开
- **模块化设计**: 3D渲染和副手限制独立配置
- **直观界面**: 齿轮图标表示配置选项，物品图标显示真实外观
- **便捷导航**: 完整的返回和保存功能
- **配置验证**: 实时验证配置有效性

- **One-click Access**: Press B key or use command `/betterexperience config` to open
- **Modular Design**: Independent configuration for 3D rendering and offhand restrictions
- **Intuitive Interface**: Gear icons for configuration options, item icons show real appearance
- **Convenient Navigation**: Complete return and save functionality
- **Configuration Validation**: Real-time validation of configuration validity

### 配置系统 / Configuration System
- **items.json**: 主配置文件，列出启用的物品
- **item_configs/**: 各个物品的具体渲染配置
- **offhand_restrictions.json**: 副手限制配置文件
- **热重载**: 支持运行时重新加载配置
- **默认值**: 提供合理的默认配置
- **导入导出**: 支持配置的导出和导入，便于在不同实例间共享配置
- **缓存机制**: 内存缓存提高配置访问性能

- **items.json**: Main configuration file listing enabled items
- **item_configs/**: Specific rendering configuration for each item
- **offhand_restrictions.json**: Offhand restrictions configuration file
- **Hot Reload**: Supports runtime configuration reloading
- **Default Values**: Provides reasonable default configurations
- **Import/Export**: Supports configuration export and import for easy sharing between instances
- **Cache Mechanism**: Memory cache improves configuration access performance

### 日志系统 / Logging System
- **模块化日志**: 按功能模块分类的日志输出
- **性能监控**: 详细的性能指标记录
- **调试支持**: 可配置的调试日志开关
- **错误追踪**: 完整的异常堆栈信息

- **Modular Logging**: Log output categorized by functional modules
- **Performance Monitoring**: Detailed performance metrics recording
- **Debug Support**: Configurable debug log switch
- **Error Tracking**: Complete exception stack information

## 🔧 配置说明 / Configuration Guide

### 主配置文件 (items.json) / Main Configuration File (items.json)
```json
{
  "enabled_items": [
    "minecraft:lantern",
    "minecraft:soul_lantern",
    "minecraft:torch",
    "minecraft:soul_torch",
    "minecraft:redstone_torch",
    "minecraft:campfire",
    "minecraft:arrow",
    "minecraft:spectral_arrow",
    "minecraft:tipped_arrow",
    "minecraft:minecart",
    "minecraft:bamboo",
    "minecraft:oak_sapling",
    "minecraft:jungle_sapling",
    "minecraft:spruce_sapling",
    "minecraft:dark_oak_sapling",
    "minecraft:birch_sapling",
    "minecraft:cherry_sapling",
    "minecraft:mangrove_propagule",
    "minecraft:poppy",
    "minecraft:dandelion",
    "minecraft:chain",
    "minecraft:wildflowers",
    "minecraft:wind_charge",
    "minecraft:flower_pot",
    "minecraft:item_frame",
    "minecraft:oak_sign",
    "minecraft:lever",
    "minecraft:repeater",
    "minecraft:comparator",
    "minecraft:turtle_egg",
    "minecraft:sniffer_egg",
    "minecraft:cornflower"
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

### 物品配置文件 (item_configs/minecraft_lantern.json) / Item Configuration File (item_configs/minecraft_lantern.json)
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

### 副手限制配置文件 (offhand_restrictions.json) / Offhand Restrictions Configuration File (offhand_restrictions.json)
```json
{
  "enabled": false,
  "allowed_items": [
    "minecraft:torch",
    "minecraft:soul_torch",
    "minecraft:lantern",
    "minecraft:soul_lantern",
    "minecraft:shield",
    "minecraft:totem_of_undying",
    "minecraft:campfire",
    "minecraft:arrow",
    "minecraft:spectral_arrow",
    "minecraft:tipped_arrow",
    "minecraft:minecart",
    "minecraft:bamboo",
    "minecraft:firework_rocket",
    "minecraft:wind_charge",
    "minecraft:oak_sign",
    "minecraft:lever",
    "minecraft:repeater",
    "minecraft:comparator",
    "minecraft:redstone_torch",
    "minecraft:chain",
    "minecraft:flower_pot",
    "minecraft:item_frame"
  ],
  "disable_block_placement": {
    "enabled": false
  },
  "disable_item_usage": {
    "enabled": false
  }
}
```

## 🔄 配置导入导出 / Configuration Import/Export

### 使用方法 / Usage

1. **导出配置 / Export Configuration**
   - 打开配置界面（`/betterexperience config`）
   - 点击"导入导出配置"按钮
   - 输入导出路径（如：`./config_export`）
   - 点击"导出配置"按钮

   - Open configuration interface (`/betterexperience config`)
   - Click "Import/Export Configuration" button
   - Enter export path (e.g., `./config_export`)
   - Click "Export Configuration" button

2. **导入配置 / Import Configuration**
   - 在导入界面输入配置目录路径
   - 点击"验证导入配置"检查配置有效性
   - 点击"导入配置"应用配置

   - Enter configuration directory path in import interface
   - Click "Validate Import Configuration" to check configuration validity
   - Click "Import Configuration" to apply configuration

### 导出格式 / Export Format

导出的配置格式与项目资源完全一致：
Exported configuration format is completely consistent with project resources:
```
config_export/
├── items.json              # 主配置文件 / Main configuration file
├── item_configs/           # 物品配置目录 / Item configuration directory
│   ├── minecraft_lantern.json
│   ├── minecraft_torch.json
│   └── ...
└── offhand_restrictions.json  # 副手限制配置 / Offhand restrictions configuration
```

### 功能特性 / Features

- ✅ **完整导出**: 导出所有配置文件和目录结构
- ✅ **格式兼容**: 与项目资源格式完全一致
- ✅ **验证功能**: 导入前验证配置有效性
- ✅ **错误处理**: 详细的错误报告和日志记录
- ✅ **配置共享**: 支持在不同Minecraft实例间共享配置

- ✅ **Complete Export**: Export all configuration files and directory structure
- ✅ **Format Compatibility**: Completely consistent with project resource format
- ✅ **Validation Function**: Validate configuration validity before import
- ✅ **Error Handling**: Detailed error reports and log records
- ✅ **Configuration Sharing**: Support sharing configurations between different Minecraft instances

## 🎮 使用指南 / User Guide

### 快速开始 / Quick Start
1. 安装Fabric Loader 0.16.14+
2. 安装Fabric API 0.128.1+
3. 下载并安装Better Experience mod
4. 启动游戏，按B键打开配置界面
5. 根据需要调整3D渲染和副手限制设置

1. Install Fabric Loader 0.16.14+
2. Install Fabric API 0.128.1+
3. Download and install Better Experience mod
4. Start the game, press B key to open configuration interface
5. Adjust 3D rendering and offhand restriction settings as needed

### 按键绑定 / Key Bindings
- **B键**: 打开主配置界面
- **命令**: `/betterexperience config` 打开配置界面

- **B Key**: Open main configuration interface
- **Command**: `/betterexperience config` to open configuration interface

### 支持的物品类型 / Supported Item Types
- **光源类**: 火把、灵魂火把、红石火把、灯笼、灵魂灯笼、营火
- **箭矢类**: 箭、光灵箭、药箭
- **运输类**: 矿车
- **植物类**: 竹子、各种树苗、花朵
- **装饰类**: 链条、花盆、物品展示框、告示牌
- **红石类**: 拉杆、中继器、比较器
- **特殊类**: 海龟蛋、嗅探兽蛋、风弹

- **Light Sources**: Torches, Soul Torches, Redstone Torches, Lanterns, Soul Lanterns, Campfires
- **Arrows**: Arrows, Spectral Arrows, Tipped Arrows
- **Transportation**: Minecarts
- **Plants**: Bamboo, Various Saplings, Flowers
- **Decoration**: Chains, Flower Pots, Item Frames, Signs
- **Redstone**: Levers, Repeaters, Comparators
- **Special**: Turtle Eggs, Sniffer Eggs, Wind Charges

## 📝 许可证 / License

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 作者 / Author

**Aeolyn** - [GitHub](https://github.com/Tanfreefish)

## 🤝 贡献 / Contributing

欢迎提交 Issue 和 Pull Request！

Issues and Pull Requests are welcome!

## 📞 联系方式 / Contact

- GitHub: [@Aeolyn](https://github.com/Tanfreefish)
- 项目主页: [Better Experience](https://github.com/Tanfreefish/better_experience)

- GitHub: [@Aeolyn](https://github.com/Tanfreefish)
- Project Homepage: [Better Experience](https://github.com/Tanfreefish/better_experience)

## 🔄 更新日志 / Changelog

### v1.3.4
-背包增强模块逻辑简化
-

### v1.3.3
-背包增强模块重构
-GUI界面重构
-增加通用配置

### v1.3.0
-增加背包增强模块，支持R键排序（对背包与容器都适用），智能转移
-简易的GUI更新

### v1.2.4
- 更新到Minecraft 1.21.6
- 更新Fabric Loader到0.16.14
- 更新Fabric API到0.128.1+1.21.6
- 优化配置管理系统
- 增强日志系统功能
- 改进GUI界面用户体验

- Updated to Minecraft 1.21.6
- Updated Fabric Loader to 0.16.14
- Updated Fabric API to 0.128.1+1.21.6
- Optimized configuration management system
- Enhanced logging system functionality
- Improved GUI interface user experience

### v1.2.0
- 新增副手限制系统
- 新增配置导入导出功能
- 新增统一配置界面
- 支持35种物品的3D渲染
- 完善错误处理和日志系统

- Added offhand restriction system
- Added configuration import/export functionality
- Added unified configuration interface
- Support for 3D rendering of 35 items
- Improved error handling and logging system

### v1.1.2
- 重构项目代码，使用统一的config管理3d物品的渲染对象
- 增加自定义渲染功能，使玩家可以在游戏中自行决定渲染对象


### v1.1.0
- 增加3d物品渲染系统

