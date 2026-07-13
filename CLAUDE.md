# CLAUDE.md — Agent 开发准则

> 本文档供 Claude Code 及所有 AI Agent 在开发过程中自动读取并严格遵循。
> 每次会话启动时自动生效，Agent 不得偏离以下规范。

---

## 项目背景

**即时文字显示器（WordShow）** — 一款支持手机与平板的安卓应用。

用户输入文字（打字或语音），在屏幕上以**滚动**或**静止**方式大字显示，支持全屏、字体大小调节与视觉特效。适用于举牌、直播弹幕式表达、公共场合无声沟通等场景。

- 包名：`com.xiao.wordshow`
- 仓库：`git@github.com:MarsYet/wordshow.git`

---

## 技术栈约束（以本项目实际配置为准）

| 类别 | 选型 | 版本 |
|------|------|------|
| 开发语言 | Kotlin | **2.2.10** |
| UI 框架 | Jetpack Compose + Material3 | BOM **2026.02.01** |
| 构建工具 | Gradle + Kotlin DSL (.kts) | Gradle **9.4.1**，AGP **9.2.1** |
| JDK | Java 21 | **21.0.9** |
| 架构模式 | MVVM | ViewModel + StateFlow |
| 最低支持 | minSdk **24** / targetSdk **36** | Android 7.0+ |
| 本地存储 | DataStore | 用于保存设置/历史记录 |
| 语音输入 | 系统 RecognizerIntent | 阶段一；后续可换讯飞/百度 SDK |

### 硬性规则

- **语言**：仅 Kotlin，禁止新增 Java 代码
- **UI**：仅 Jetpack Compose，禁止新增 XML View（除非确有必要且注释说明理由）
- **构建脚本**：仅 Kotlin DSL（`.kts`），禁止 Groovy
- **架构**：严格 MVVM，ViewModel 不得持有 `Activity` / `Context` / `View` 的直接引用
- **向后兼容**：API > 24 时，必须在调用处做 `Build.VERSION.SDK_INT` 判断，并提供降级方案

---

## 项目当前架构状态

```
app/src/main/java/com/xiao/wordshow/
├── MainActivity.kt          # 应用入口，已集成 enableEdgeToEdge()、Compose 根节点
└── ui/theme/
    ├── Color.kt             # 调色板定义
    ├── Type.kt              # 字体排版
    └── Theme.kt             # 动态取色 + 深色/浅色主题（Material3）
```

> 当前处于 **M0（项目初始化）** 阶段。接下来的任务是按照目标架构搭建完整的 package 骨架和 P0 功能。

### 目标架构

```
com.xiao.wordshow/
├── MainActivity.kt
├── ui/
│   ├── input/
│   │   ├── InputScreen.kt
│   │   └── InputViewModel.kt
│   ├── display/
│   │   ├── DisplayScreen.kt
│   │   ├── DisplayViewModel.kt
│   │   └── components/
│   │       ├── ScrollingText.kt
│   │       ├── StaticText.kt
│   │       └── TextEffects.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── navigation/
│   │   └── AppNavHost.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── data/
│   ├── model/
│   │   └── DisplaySettings.kt
│   ├── voice/
│   │   └── VoiceRecognizer.kt
│   └── preferences/
│       └── SettingsRepository.kt
└── util/
    └── FullscreenUtil.kt
```

### 数据流

```
用户输入(打字/语音)
      ↓
InputViewModel（状态收集）
      ↓
共享 State（Navigation 传参 或 共享 ViewModel）
      ↓
DisplayViewModel（文本 + 设置合并）
      ↓
DisplayScreen（渲染：滚动/静止 + 字体大小 + 特效）
```

---

## 功能优先级

| 优先级 | 功能 | 说明 |
|--------|------|------|
| **P0** | 文字输入 + 实时同步 | 打字输入，实时显示 |
| **P0** | 静止大字显示 | 居中大字 |
| **P0** | 跑马灯滚动 | 速度可调 |
| **P0** | 滚动/静止切换 | 一键切换按钮 |
| **P0** | 全屏模式 | 隐藏状态栏/导航栏 |
| P1 | 语音输入 | 系统 RecognizerIntent |
| P1 | 字体大小调节 | 滑块或手势缩放 |
| P2 | 文字特效 | 渐变/阴影/发光/跳动动画 |
| P2 | 主题皮肤 | 多种配色方案 |
| P3 | 历史记录 | DataStore 持久化 |

---

## 里程碑

| 阶段 | 内容 |
|------|------|
| **M1** | 项目骨架 + P0 全部跑通 |
| M2 | P1 语音输入 + 字体调节 |
| M3 | P2 视觉特效 |
| M4 | 多屏适配 + 性能优化 + UI 打磨 |
| M5 | 测试 + 打包发布 |

> 当前：**M1 开始**

---

## 开发流程规范

1. **先计划后动手**：涉及多文件改动或架构调整时，必须先说明方案并等待确认；单文件小修复可直接执行
2. **小步提交**：每完成一个独立功能模块，用中文 commit message 做一次 Git 提交
3. **MVP 优先**：先写可独立编译运行的最小可用版本，再逐步完善
4. **双屏思维**：涉及 UI 的改动，必须同时考虑手机和平板两种尺寸下的表现
5. **编译验证**：功能完成后必须运行 `./gradlew assembleDebug` 确认编译通过

---

## 代码风格

- 严格遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Composable 函数：首字母大写，命名体现 UI 语义（如 `ScrollingText` 而非 `TextWidget`）
- 参数：尽量提供默认值，降低调用门槛
- **状态提升**（State Hoisting）：Composable 保持无状态，状态由 ViewModel 或父组件持有
- 单一职责：单个 Composable / 函数不超过 80 行，超过则拆分
- `remember` / `LaunchedEffect` 等副作用 API 必须有明确的 key

---

## 权限与安全

- 语音输入需要 `RECORD_AUDIO`：必须使用 `rememberLauncherForActivityResult` + 运行时权限申请，权限拒绝后优雅降级（提示用户改用打字输入）
- `AndroidManifest.xml` 中不声明不必要的权限
- 代码中禁止硬编码任何密钥、Token 或敏感信息
- 使用 DataStore（非 SharedPreferences）做本地持久化

---

## 性能要求

- 滚动动画目标 **60fps**，动画循环内禁止：
  - 字符串拼接 / 正则匹配
  - 对象分配（避免 GC 抖动）
  - IO 操作（文件读写、网络请求）
- 特效渲染注意平板大屏（≥10 寸），必要时根据设备性能降级（关闭部分特效）
- 全屏模式下注意系统手势冲突（Android 10+ 手势导航）

---

## 禁止事项

- ❌ 禁止未经讨论引入重量级三方库（DI 框架如 Hilt/Dagger、网络库如 Retrofit、图片库如 Glide 等）——当前项目不需要
- ❌ 禁止直接删除或大范围重构已有代码，除非先说明原因并征得同意
- ❌ 禁止在未验证编译的情况下声称"功能已完成"
- ❌ 禁止使用 `!!` 强制非空断言（除非在测试代码中）——使用 `?.`、`?:`、`requireNotNull` 等安全写法
- ❌ 禁止使用 `GlobalScope` 启动协程 —— 使用 `viewModelScope` 或 `rememberCoroutineScope`

---

## 遇到不确定的情况

- 需求模糊 → 给出一个**合理默认方案并说明假设**，不等不靠，不停下来反复追问
- 架构冲突 → **主动指出**并给出调整建议，不硬塞代码凑合
- 新 API 不确定兼容性 → 查 `@RequiresApi` 注解或 `Build.VERSION.SDK_INT` 判断

---

## 项目配置文件速查

| 文件 | 关键内容 |
|------|----------|
| `gradle/libs.versions.toml` | 所有依赖版本统一管理 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.4.1，镜像源 |
| `gradle.properties` | JVM 参数、超时配置 |
| `app/build.gradle.kts` | compileSdk=36, minSdk=24, targetSdk=36, R8 已启用 |
| `app/proguard-rules.pro` | ProGuard 混淆规则 |
