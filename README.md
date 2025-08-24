# Better Experience Mod/更好的体验模组

## English

### Overview
Better Experience is a Minecraft optimization mod focused on enhancing vanilla gameplay experience by providing convenient features that are missing in the original game. The mod's philosophy is to offer convenient and reasonable functionality without adding new items. Currently, we have implemented the following features:

#### 🎨 3D Handheld Item Rendering
- **Universal Rendering**: Support for replacing 2D textures of handheld items with their 3D models
- **Flexible Customization**: Scale, rotate, translate, and customize item appearance (you can even replace TNT with a Creeper!)
- **Multi-view Support**: Independent configuration for first-person and third-person views
- **Entity & Block Rendering**: Choose between entity models or block states for rendering

#### 🛡️ Offhand Restriction System
- **Offhand Disable**: Disable offhand item usage to prevent accidental activation
- **Whitelist**: Add items to whitelist to unlock offhand item restrictions
- **Smart Management**: Intuitive GUI interface for managing whitelist items

#### 🎛️ Inventory Sorting System
- **R Key Sorting**: Quickly sort containers where the mouse is hovering
- **Shift+R Smart Transfer**: Transfer items from one container to another
- **Multiple Strategies**: Sort by name, quantity, or type
- **Intelligent Logic**: Smart transfer rules and merge modes

### 🚀 Future Plans

#### 🔧 Quality of Life Improvements
- **Quick Chest Access**: Open shulker boxes and ender chests directly from inventory
- **Auto-replacement System**: Automatically replace broken tools or depleted blocks
- **Click/Hold Functions**: Advanced clicking and long-press functionality
- **Sittable Blocks**: Sit on specific blocks (like stairs, slabs) for enhanced immersion

## 🛠️ Tech Stack

### Core Framework
- **Minecraft**: 1.21.0-1.21.9
- **Fabric Loader**: >=0.16.14
- **Fabric API**: >=0.128.1
- **Java**: >=17

### Development Tools
- **Build Tool**: Gradle 8.0+
- **Fabric Loom**: 1.6-SNAPSHOT
- **Recommended IDE**: IntelliJ IDEA / Eclipse

### Main Dependencies
- **Gson**: 2.10.1 (JSON parsing)
- **LWJGL**: 3.3.2 (Graphics rendering)
- **SLF4J**: Logging framework

### Architecture Design
- **Modular Architecture**: Each feature is an independent module, supporting individual enable/disable
- **Event-driven**: Based on Minecraft event system
- **Mixin Injection**: Modify vanilla code to implement features
- **Configuration-driven**: Unified configuration management system

## 📋 System Requirements

### Minimum Requirements
- **Operating System**: Windows 10+, macOS 10.15+, Linux
- **Java Version**: OpenJDK 17 or Oracle JDK 17
- **Memory**: 4GB RAM (8GB+ recommended)
- **Storage**: 100MB available space

### Recommended Configuration
- **Operating System**: Windows 11, macOS 12+, Ubuntu 20.04+
- **Java Version**: OpenJDK 17 LTS
- **Memory**: 8GB+ RAM
- **Graphics**: OpenGL 3.2+ support

## 🚀 Installation Guide

### For Players
1. **Download Fabric Loader**
   - Visit [Fabric Official Website](https://fabricmc.net/use/)
   - Download Fabric Loader for your Minecraft version

2. **Install Fabric API**
   - Download [Fabric API](https://modrinth.com/mod/fabric-api)
   - Place in `mods` folder

3. **Install Better Experience Mod**
   - Download the latest version of Better Experience Mod
   - Place in `mods` folder

4. **Launch Game**
   - Select Fabric profile to launch game
   - Press `B key` to open configuration interface

### For Developers
```bash
# Clone project
git clone https://github.com/Sin-Fish/better_experience.git
cd better_experience

# Build project
./gradlew build

# Run development environment
./gradlew runClient
```

## 🔧 Project Setup

### Environment Preparation
```bash
# Ensure Java 17 is installed
java -version

# Ensure Gradle is installed (or use project's gradlew)
./gradlew --version
```

### Development Environment Configuration
1. **Import Project to IDE**
   ```bash
   # Generate IDE configuration files
   ./gradlew idea    # IntelliJ IDEA
   ./gradlew eclipse # Eclipse
   ```

2. **Configure Runtime Environment**
   - Set JVM arguments: `-Xmx2G -XX:+UseG1GC`
   - Set working directory: Project root directory
   - Set main class: `com.aeolyn.better_experience.BetterExperienceMod`

3. **Run Tests**
   ```bash
   # Run unit tests
   ./gradlew test
   
   # Run client
   ./gradlew runClient
   
   # Build release version
   ./gradlew build
   ```

### Multi-version Build
```bash
# Build specific version
./gradlew build -Pminecraft_version=1.21.6

# Build all supported versions
./gradlew buildAllVersions
```

## 🎮 Usage Guide

### Key Bindings
- **B Key**: Open configuration interface
- **R Key**: One-click inventory/container sorting
- **Shift+R**: Smart item transfer

### Configuration
1. **3D Rendering Configuration**
   - Enable/disable 3D rendering feature
   - Customize item rendering objects
   - Set rendering parameters

2. **Offhand Restriction Configuration**
   - Enable/disable offhand restrictions
   - Configure whitelist items
   - Set restriction rules

3. **Inventory Sorting Configuration**
   - Choose sorting strategy (name/quantity/type)
   - Configure smart transfer rules
   - Set merge mode

### Configuration File Locations
- **Main Config**: `config/better_experience.json`
- **3D Rendering Config**: `config/better_experience/render3d/`
- **Offhand Config**: `config/better_experience/offhand/`
- **Inventory Config**: `config/better_experience/inventory/`


## 🤝 Contributing

### Development Process
1. Fork the project
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Create Pull Request

### Code Standards
- Use Java 17 syntax
- Follow Google Java Style Guide
- Add appropriate comments and documentation
- Write unit tests

### Commit Standards
```
feat: new feature
fix: bug fix
docs: documentation update
style: code formatting
refactor: code refactoring
test: testing related
chore: build process or auxiliary tool changes
```

---

## 中文

### 概述
Better Experience/更好的体验是一个专注于优化Minecraft原版游戏体验的优化拓展类Mod，主要提供各类原版缺失的方便功能，mod的主旨是在不加入新物品的前提下让玩家享受到方便且合理的功能，目前我们为游戏加入了以下几个功能:

#### 🎨 3D手持物品渲染
- **通用渲染**: 支持将手持物品的2D贴图替换为3D模型
- **灵活自定义**: 缩放、旋转、平移和自定义物品外观（甚至可以将TNT替换成苦力怕！）
- **多视角支持**: 第一人称和第三人称视角的独立配置
- **实体与方块渲染**: 选择实体模型或方块状态进行渲染

#### 🛡️ 副手限制系统
- **副手禁用**: 禁用副手物品使用，防止误操作
- **白名单**: 添加物品到白名单以解除副手限制
- **智能管理**: 直观的GUI界面管理白名单物品

#### 🎛️ 背包整理系统
- **R键整理**: 快速整理鼠标停留区域的容器
- **Shift+R智能转移**: 将物品从一个容器转移到另一个容器
- **多种策略**: 按名称、数量或类型排序
- **智能逻辑**: 智能转移规则和合并模式

### 🚀 未来计划

#### 🔧 生活质量改进
- **快速箱子访问**: 直接从背包打开潜影盒和末影箱
- **自动替换系统**: 自动替换损坏的工具或耗尽的方块
- **点击/长按功能**: 高级点击和长按功能
- **可坐方块**: 坐在特定方块（如台阶、楼梯）上，增强沉浸感

## 🛠️ 技术栈

### 核心框架
- **Minecraft**: 1.21.0-1.21.9
- **Fabric Loader**: >=0.16.14
- **Fabric API**: >=0.128.1
- **Java**: >=17

### 开发工具
- **构建工具**: Gradle 8.0+
- **Fabric Loom**: 1.6-SNAPSHOT
- **推荐IDE**: IntelliJ IDEA / Eclipse

### 主要依赖
- **Gson**: 2.10.1 (JSON解析)
- **LWJGL**: 3.3.2 (图形渲染)
- **SLF4J**: 日志框架

### 架构设计
- **模块化架构**: 每个功能都是独立模块，支持单独启用/禁用
- **事件驱动**: 基于Minecraft事件系统
- **Mixin注入**: 修改原版代码实现功能
- **配置驱动**: 统一的配置管理系统

## 📋 系统要求

### 最低要求
- **操作系统**: Windows 10+, macOS 10.15+, Linux
- **Java版本**: OpenJDK 17 或 Oracle JDK 17
- **内存**: 4GB RAM (推荐8GB+)
- **存储**: 100MB 可用空间

### 推荐配置
- **操作系统**: Windows 11, macOS 12+, Ubuntu 20.04+
- **Java版本**: OpenJDK 17 LTS
- **内存**: 8GB+ RAM
- **显卡**: 支持OpenGL 3.2+

## 🚀 安装指南

### 玩家安装
1. **下载Fabric Loader**
   - 访问 [Fabric官网](https://fabricmc.net/use/)
   - 下载对应Minecraft版本的Fabric Loader

2. **安装Fabric API**
   - 下载 [Fabric API](https://modrinth.com/mod/fabric-api)
   - 放入 `mods` 文件夹

3. **安装Better Experience Mod**
   - 下载最新版本的Better Experience Mod
   - 放入 `mods` 文件夹

4. **启动游戏**
   - 选择Fabric配置文件启动游戏
   - 按 `B键` 打开配置界面

### 开发者安装
```bash
# 克隆项目
git clone https://github.com/Tanfreefish/better_experience.git
cd better_experience

# 构建项目
./gradlew build

# 运行开发环境
./gradlew runClient
```

## 🔧 项目启动

### 环境准备
```bash
# 确保Java 17已安装
java -version

# 确保Gradle已安装（或使用项目自带的gradlew）
./gradlew --version
```

### 开发环境配置
1. **导入项目到IDE**
   ```bash
   # 生成IDE配置文件
   ./gradlew idea    # IntelliJ IDEA
   ./gradlew eclipse # Eclipse
   ```

2. **配置运行环境**
   - 设置JVM参数: `-Xmx2G -XX:+UseG1GC`
   - 设置工作目录: 项目根目录
   - 设置主类: `com.aeolyn.better_experience.BetterExperienceMod`

3. **运行测试**
   ```bash
   # 运行单元测试
   ./gradlew test
   
   # 运行客户端
   ./gradlew runClient
   
   # 构建发布版本
   ./gradlew build
   ```

### 多版本构建
```bash
# 构建特定版本
./gradlew build -Pminecraft_version=1.21.6

# 构建所有支持版本
./gradlew buildAllVersions
```

## 🎮 使用指南

### 按键绑定
- **B键**: 打开配置界面
- **R键**: 一键整理背包/容器
- **Shift+R**: 智能转移物品

### 配置说明
1. **3D渲染配置**
   - 启用/禁用3D渲染功能
   - 自定义物品渲染对象
   - 设置渲染参数

2. **副手限制配置**
   - 启用/禁用副手限制
   - 配置白名单物品
   - 设置限制规则

3. **背包整理配置**
   - 选择排序策略（名称/数量/类型）
   - 配置智能转移规则
   - 设置合并模式

### 配置文件位置
- **主配置**: `config/better_experience.json`
- **3D渲染配置**: `config/better_experience/render3d/`
- **副手配置**: `config/better_experience/offhand/`
- **背包配置**: `config/better_experience/inventory/`


### 日志位置
- **客户端日志**: `.minecraft/logs/latest.log`
- **崩溃报告**: `.minecraft/crash-reports/`

## 🤝 贡献指南

### 开发流程
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范
- 使用Java 17语法
- 遵循Google Java Style Guide
- 添加适当的注释和文档
- 编写单元测试

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 📝 许可证 / License

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者 / Author

**Aeolyn** - [GitHub](https://github.com/Sin-Fish)

## 🤝 贡献 / Contributing

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式 / Contact

- GitHub: [@Aeolyn](https://github.com/Sin-Fish)
- 项目主页: [Better Experience](https://github.com/Sin-Fish/better_experience)

## 🔄 更新日志 / Changelog

### v1.3.4
- 背包增强模块逻辑简化

### v1.3.3
- 背包增强模块重构
- GUI界面重构
- 增加通用配置

### v1.3.0
- 增加背包增强模块，支持R键排序（对背包与容器都适用），智能转移
- 简易的GUI更新

### v1.2.4
- 更新到Minecraft 1.21.6
- 更新Fabric Loader到0.16.14
- 更新Fabric API到0.128.1+1.21.6
- 优化配置管理系统
- 增强日志系统功能
- 改进GUI界面用户体验

### v1.2.0
- 新增副手限制系统
- 新增配置导入导出功能
- 新增统一配置界面
- 支持35种物品的3D渲染
- 完善错误处理和日志系统

### v1.1.2
- 重构项目代码，使用统一的config管理3d物品的渲染对象
- 增加自定义渲染功能，使玩家可以在游戏中自行决定渲染对象

### v1.1.0
- 增加3d物品渲染系统

