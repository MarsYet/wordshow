# 即时文字显示器（WordShow）—— 项目开工文档

> 版本：v1.0　日期：2026-07-13
> 基于立项文档 `markDown1783951322399.md`，结合本地项目实际配置编写。
> 本文档面向开发者与 AI Agent，提供完整的项目上下文和开工指引。

---

## 一、项目概述

### 1.1 基本信息

| 项 | 值 |
|----|-----|
| 中文名 | 即时文字显示器 |
| 英文/包名 | WordShow / `com.xiao.wordshow` |
| 仓库地址 | `git@github.com:MarsYet/wordshow.git` |
| 开发环境 | Android Studio + Claude Code |
| 项目状态 | **M1 — P0 功能开发开始** |

### 1.2 产品定位

一款支持**手机与平板**的安卓应用。用户输入文字（打字或语音），在屏幕上以滚动或静止方式**全屏大字显示**，支持字体大小调节与视觉特效。适用于举牌、直播弹幕式表达、公共场合无声沟通等场景。

### 1.3 目标用户

非技术背景的普通用户，注重**简洁易用**。

---

## 二、技术栈（按本地配置）

| 类别 | 选型 | 版本 |
|------|------|------|
| 语言 | Kotlin | **2.2.10** |
| UI | Jetpack Compose + Material3 | BOM **2026.02.01** |
| 构建 | Gradle (Kotlin DSL) | **9.4.1** |
| Android 插件 | AGP | **9.2.1** |
| JDK | Oracle JDK | **21.0.9** |
| 最低 API | minSdk | **24** (Android 7.0) |
| 目标/编译 API | targetSdk / compileSdk | **36** (ext 1) |
| 架构 | MVVM | ViewModel + StateFlow |
| 存储 | DataStore | Preferences + 历史记录 |
| 语音 | RecognizerIntent | 系统内置 |
| 版本控制 | Git + GitHub | SSH 连接 |

---

## 三、功能清单

| 优先级 | 功能 | 里程碑 |
|--------|------|--------|
| P0 | 文字输入（打字，实时同步显示区） | M1 |
| P0 | 静止大字显示（居中） | M1 |
| P0 | 跑马灯滚动显示（速度可调） | M1 |
| P0 | 滚动/静止一键切换 | M1 |
| P0 | 全屏模式 | M1 |
| P1 | 语音输入（系统 RecognizerIntent） | M2 |
| P1 | 字体大小调节（滑块/手势） | M2 |
| P2 | 文字特效（渐变/阴影/发光/跳动） | M3 |
| P2 | 主题皮肤（多套配色） | M3 |
| P3 | 历史记录 | M4 |

---

## 四、项目架构

### 4.1 目录结构

```
app/src/main/java/com/xiao/wordshow/
├── MainActivity.kt                 ✅ 已完成
├── ui/
│   ├── input/
│   │   ├── InputScreen.kt          ⏳ 待开发
│   │   └── InputViewModel.kt       ⏳ 待开发
│   ├── display/
│   │   ├── DisplayScreen.kt        ⏳ 待开发
│   │   ├── DisplayViewModel.kt     ⏳ 待开发
│   │   └── components/
│   │       ├── ScrollingText.kt    ⏳ 待开发
│   │       ├── StaticText.kt       ⏳ 待开发
│   │       └── TextEffects.kt      ⏳ 待开发 (P2)
│   ├── settings/
│   │   ├── SettingsScreen.kt       ⏳ 待开发 (P2)
│   │   └── SettingsViewModel.kt    ⏳ 待开发 (P2)
│   ├── navigation/
│   │   └── AppNavHost.kt           ⏳ 待开发
│   └── theme/
│       ├── Color.kt                ✅ 已完成
│       ├── Type.kt                 ✅ 已完成
│       └── Theme.kt               ✅ 已完成
├── data/
│   ├── model/
│   │   └── DisplaySettings.kt      ⏳ 待开发
│   ├── voice/
│   │   └── VoiceRecognizer.kt      ⏳ 待开发 (P1)
│   └── preferences/
│       └── SettingsRepository.kt   ⏳ 待开发 (P2)
└── util/
    └── FullscreenUtil.kt           ⏳ 待开发
```

### 4.2 数据流

```
用户输入（打字/语音）
      ↓
InputViewModel ── 状态收集
      ↓
Navigation 传参 / 共享 State
      ↓
DisplayViewModel ── 文本 + 设置合并
      ↓
DisplayScreen ── 渲染（滚动/静止 + 字体 + 特效）
```

### 4.3 关键设计原则

- **单向数据流**：State 向下传，Event 向上传
- **UI/逻辑分离**：ViewModel 不持有 Context
- **组件化**：滚动/静止/特效独立 Composable
- **自适应**：使用 `WindowSizeClass` 或百分比/权重适配手机和平板，不写死横竖屏限制

---

## 五、里程碑规划

| 阶段 | 内容 | 预估工时 |
|------|------|----------|
| M1 | 项目骨架 + P0 全部跑通 | 1-2 周 |
| M2 | P1 语音输入 + 字体调节 | 3-5 天 |
| M3 | P2 视觉特效 | 1-2 周 |
| M4 | 多屏适配 + 性能优化 + UI 打磨 | 1 周 |
| M5 | 测试 + 打包发布 | 3-5 天 |

> 当前进度：**M1 开始**（已有 Compose 项目骨架 + 主题配置）

---

## 六、环境配置速查

### 6.1 关键文件

| 文件 | 说明 |
|------|------|
| `CLAUDE.md` | Agent 开发准则（AI 必读） |
| `gradle/libs.versions.toml` | 统一版本管理 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.4.1 + 腾讯云镜像 |
| `gradle.properties` | JVM -Xmx2048m，超时 120s，Configuration Cache 已启用 |
| `app/build.gradle.kts` | R8 混淆已启用，minSdk=24，targetSdk=36 |
| `app/proguard-rules.pro` | Compose + Kotlin 混淆规则 |
| `.gitignore` | IDE 文件已排除 |

### 6.2 常用命令

```bash
# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease

# 运行测试
./gradlew test

# 清理项目
./gradlew clean

# 查看所有任务
./gradlew tasks

# 停止 Gradle Daemon
./gradlew --stop
```

### 6.3 Git 信息

```bash
# 远程仓库（SSH）
git remote -v   # origin  git@github.com:MarsYet/wordshow.git

# 当前分支
git branch      # master
```

---

## 七、开工检查清单

- [x] Git 仓库初始化
- [x] 首次提交并推送 GitHub
- [x] Gradle 构建通过（`./gradlew assembleDebug`）
- [x] 开发环境配置（Java 21, Gradle 9.4.1, Kotlin 2.2.10）
- [x] Compose 主题文件就绪
- [x] R8 混淆配置就绪
- [x] CLAUDE.md Agent 准则已就位
- [ ] 搭建 package 骨架（input / display / settings / navigation / data / util）
- [ ] Navigation Compose 路由跑通
- [ ] P0 功能开发
- [ ] M1 完成

---

## 八、下一步行动

1. ✅ `CLAUDE.md` 已就位 —— 所有 AI Agent 启动时自动加载
2. ✅ 本开工文档已保存为 `PROJECT_KICKOFF.md`
3. **下一步**：启动 Claude，说"按照 CLAUDE.md 的架构，帮我搭建项目的基础文件结构和空的 Composable 页面（输入页 + 显示页），先跑通导航"
4. 然后从 P0 功能开始，按里程碑 M1 → M5 逐步推进
