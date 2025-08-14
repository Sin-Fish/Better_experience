# Handheld3D Mod

一个为Minecraft 1.21.6设计的Fabric mod，为手持物品提供3D渲染效果。

## 🎯 当前状态

### ✅ 已完成功能
- **项目构建系统** - 完整的Gradle构建配置
- **基础框架** - Fabric mod基础结构
- **Mixin系统** - 物品渲染拦截框架
- **灯笼检测** - 能够检测到灯笼物品的渲染
- **日志系统** - 详细的调试日志输出

### 🔧 技术实现
- **Minecraft版本**: 1.21.6
- **Fabric Loader**: 0.16.13
- **Fabric API**: 0.128.1+1.21.6
- **Java版本**: 17+
- **Gradle版本**: 8.6

## 🏗️ 项目结构

```
Handheld3D/
├── build.gradle                    # 构建配置
├── gradle.properties              # 项目属性
├── README.md                      # 项目文档
├── src/main/java/com/example/handheld3d/
│   ├── Handheld3DMod.java         # 主mod类
│   └── mixin/
│       ├── LanternItemRendererMixin.java    # 灯笼渲染mixin
│       └── PlayerArmRendererMixin.java      # 玩家手臂渲染mixin
└── src/main/resources/
    ├── fabric.mod.json            # mod元数据
    └── handheld3d.mixins.json     # mixin配置
```

## 🚀 开发环境

### 构建项目
```bash
# 克隆项目
git clone https://github.com/Tanfreefish/handheld3d.git
cd handheld3d

# 构建项目
./gradlew build

# 运行开发环境
./gradlew runClient
```

### 构建输出
构建成功后，jar文件位于：
```
build/libs/handheld3d-1.0.0.jar
```

## 📋 功能特性

### 🏮 灯笼3D渲染 (计划中)
- [ ] 将灯笼渲染为3D方块模型
- [ ] 支持第一人称和第三人称视角
- [ ] 自动调整位置和大小
- [ ] 左手/右手支持

### 🎮 兼容性
- ✅ Minecraft 1.21.6
- ✅ Fabric Loader 0.16.13+
- ✅ Fabric API 0.128.1+1.21.6
- ✅ Java 17+

## 🔍 调试信息

当前mod会在控制台输出以下调试信息：
```
Handheld3D: 检测到灯笼物品渲染 - item.minecraft.lantern
Handheld3D: 渲染模式 - FIRST_PERSON_RIGHT_HAND
Handheld3D: 左手模式 - false
```

## 📝 下一步计划

### 短期目标
1. **实现3D渲染逻辑** - 使用反射调用Minecraft渲染API
2. **测试渲染效果** - 在游戏中验证3D灯笼显示
3. **优化性能** - 减少反射调用开销

### 长期目标
1. **扩展物品支持** - 添加更多物品的3D渲染
2. **配置文件** - 添加用户可配置选项
3. **性能优化** - 缓存反射方法调用
4. **发布mod** - 上传到CurseForge/Modrinth

## 🛠️ 技术细节

### Mixin注入点
- **LanternItemRendererMixin**: 注入`ItemRenderer.renderItem`方法
- **PlayerArmRendererMixin**: 注入`PlayerEntityRenderer.renderArm`方法

### 渲染流程
1. 检测物品是否为灯笼
2. 检查渲染模式是否为手持模式
3. 取消原版渲染
4. 执行自定义3D渲染逻辑

## 🐛 已知问题

- 方法签名警告 (不影响功能)
- 需要手动安装Fabric Loader 0.16.13+

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

- GitHub: https://github.com/Tanfreefish/handheld3d
- 问题反馈: 请在GitHub上提交Issue
