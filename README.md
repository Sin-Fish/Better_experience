# Handheld3D Mod

一个为 Minecraft 添加 3D 手持物品渲染的 Fabric mod。

## 功能特性

- 将灯笼在手持时渲染为 3D 方块模型
- 支持第一人称和第三人称视角
- 自动调整玩家手臂位置以匹配 3D 物品
- 兼容 Minecraft 1.21.6

## 安装要求

- Minecraft 1.21.6
- Fabric Loader 0.15.0+
- Fabric API
- Java 17+

## 安装步骤

1. 下载并安装 Fabric Loader
2. 下载 Fabric API
3. 将本 mod 的 jar 文件放入 mods 文件夹
4. 启动游戏

## 使用方法

1. 在游戏中获得灯笼物品
2. 将灯笼放入主手或副手
3. 观察灯笼现在以 3D 方块形式渲染

## 开发信息

### 构建项目

```bash
./gradlew build
```

### 运行项目

```bash
./gradlew runClient
```

## 技术细节

本 mod 使用 Fabric Mixin 技术来修改 Minecraft 的渲染系统：

- `LanternItemRendererMixin`: 修改物品渲染逻辑，将灯笼渲染为 3D 方块
- `PlayerArmRendererMixin`: 调整玩家手臂位置以匹配 3D 物品

## 许可证

MIT License

## 问题反馈

如果遇到问题，请在 GitHub 上提交 issue。
