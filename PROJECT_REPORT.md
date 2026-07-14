# 看见 (WordShow) — 项目完整报告

> 生成日期：2026-07-15 | 版本：v1.0 | 用途：AI Agent 上下文注入

---

## 一、项目概述

### 1.1 基本信息

| 属性 | 值 |
|------|-----|
| 中文名 | 看见 |
| 英文名 | WordShow |
| 包名 | com.xiao.wordshow |
| 版本 | 1.0 (versionCode=1, versionName="1.0") |
| 仓库 | git@github.com:MarsYet/wordshow.git |
| 开发周期 | 2026-07-13 ~ 2026-07-15 (3天) |
| 代码量 | 1600+ 行 Kotlin |
| 编译状态 | 零 Warning |

### 1.2 产品定位

一款支持**手机与平板**的 Android 应用。用户输入文字（打字或语音），在屏幕上以**滚动或静止**方式**全屏大字展示**，支持字体大小调节与视觉特效。适用于举牌、直播弹幕式表达、公共场合无声沟通等场景。

### 1.3 双模式设计

应用有两个核心模式，通过首页左上角滑块一键切换：

| 模式 | 功能 | 适用场景 |
|------|------|----------|
| 展板模式 | 打字/语音输入 → 全屏大字显示（静止/滚动/特效） | 举牌、应援、大型场合 |
| 字幕模式 | 文本断句 + 文件导入 → 逐句播报（可暂停/跳转） | 演讲提词、字幕播放 |

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 2.2.10 |
| UI 框架 | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| 架构模式 | MVVM | ViewModel + StateFlow |
| 导航 | Navigation Compose | 2.8.5 |
| 语音识别 | 讯飞 SparkChain SDK | 2.1.0 (SparkChain.aar + Codec.aar) |
| 本地存储 | DataStore Preferences | 1.1.1 |
| 构建系统 | Gradle (Kotlin DSL) | 9.4.1 |
| Android 插件 | AGP | 9.2.1 |
| JDK | Oracle JDK | 21.0.9 |
| 最低支持 | Android 7.0 | API 24 |
| 目标 SDK | Android 15 | API 36 (ext 1) |

### 2.2 架构分层

```
┌─────────────────────────────────────────┐
│ UI 层: Compose + Material3 + Navigation │
│ Canvas 动画 + WindowSizeClass 适配      │
├─────────────────────────────────────────┤
│ 业务逻辑层: ViewModel + StateFlow       │
│ Kotlin Coroutines                       │
├─────────────────────────────────────────┤
│ 数据/服务层:                            │
│ SparkChain SDK + AudioRecord PCM        │
│ WordParser (docx/txt 解析)             │
│ DataStore Preferences                   │
├─────────────────────────────────────────┤
│ 基础设施: Kotlin 2.2.10 + Gradle 9.4.1 │
│ AGP 9.2.1 + JDK 21 + R8 混淆           │
├─────────────────────────────────────────┤
│ 平台: Android 7.0+ | 手机+平板          │
└─────────────────────────────────────────┘
```

### 2.3 数据流

```
用户输入(打字/语音)
      ↓
InputViewModel (MutableStateFlow<String>)
      ↓
共享 ViewModel (Activity 作用域)
      ↓
DisplayViewModel (接收文本 + 设置)
      ↓
DisplayScreen 渲染 (静止/滚动 + 字体 + 特效)
```

---

## 三、项目文件结构

```
E:\wordshow\
├── README.md
├── CLAUDE.md                          # AI Agent 开发准则
├── PROJECT_REPORT.md                  # 本文件
├── settings.gradle.kts                # Gradle 设置（含 flatDir 仓库）
├── build.gradle.kts                   # 根构建脚本
├── gradle.properties                  # JVM 参数、超时配置
├── keystore.properties                # 签名配置（gitignore）
├── .gitignore
├── gradle/
│   ├── libs.versions.toml             # 版本目录（统一依赖管理）
│   └── wrapper/
│       ├── gradle-wrapper.properties  # Gradle 9.4.1 + 腾讯云镜像
│       └── gradle-wrapper.jar
├── app/
│   ├── build.gradle.kts               # 应用构建脚本（含签名配置）
│   ├── proguard-rules.pro             # 混淆规则
│   ├── kanjian.jks                    # 签名密钥（gitignore）
│   └── libs/
│       ├── SparkChain.aar             # 讯飞核心 SDK (4.5MB)
│       └── Codec.aar                  # 讯飞编解码库 (1.5MB)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/strings.xml     # app_name="看见"
│       │   ├── values/themes.xml
│       │   ├── values-night/themes.xml
│       │   ├── drawable/
│       │   │   ├── ic_launcher_foreground.xml  # 三条横线图标
│       │   │   └── ic_launcher_background.xml  # 灰黑背景
│       │   └── xml/backup_rules.xml
│       └── java/com/xiao/wordshow/
│           ├── MainActivity.kt        # 入口，enableEdgeToEdge + Theme
│           ├── WordShowApp.kt         # Application，SparkChain 初始化
│           ├── ui/
│           │   ├── input/
│           │   │   ├── InputScreen.kt      # 输入页（含历史/预设/文件导入）
│           │   │   └── InputViewModel.kt   # 文字状态管理
│           │   ├── display/
│           │   │   ├── DisplayScreen.kt    # 显示页（展板+字幕双模式）
│           │   │   ├── DisplayViewModel.kt # 显示配置+字幕状态
│           │   │   ├── AnimatedBg.kt       # 动态光池背景
│           │   │   └── components/
│           │   │       ├── ScrollingText.kt    # 跑马灯滚动
│           │   │       ├── TextEffects.kt      # 特效渲染（5种）
│           │   │       ├── StaticText.kt       # 静止大字
│           │   │       └── SubtitleDisplay.kt  # 字幕卡片
│           │   ├── settings/
│           │   │   ├── SettingsScreen.kt   # 预设配置管理页
│           │   │   └── SettingsViewModel.kt
│           │   ├── navigation/
│           │   │   ├── Routes.kt           # 路由常量
│           │   │   └── AppNavHost.kt       # NavHost 配置
│           │   └── theme/
│           │       ├── Color.kt            # 浅灰玻璃调色板
│           │       ├── Type.kt             # 字体排版
│           │       └── Theme.kt            # Material3 主题
│           ├── data/
│           │   ├── model/
│           │   │   └── DisplaySettings.kt  # TextEffect 枚举
│           │   ├── voice/
│           │   │   └── VoiceRecognizer.kt  # 讯飞 ASR 封装
│           │   └── preferences/
│           │       ├── HistoryRepository.kt # DataStore 历史/预设/设置
│           │       └── SettingsRepository.kt
│           └── util/
│               ├── FullscreenUtil.kt       # 全屏工具
│               ├── DeviceAdaptive.kt       # 设备自适应参数
│               └── WordParser.kt           # docx/txt 文件解析
```

---

## 四、核心功能详解

### 4.1 展板模式

**输入方式：**
- 打字输入：OutlinedTextField，支持多行
- 语音输入：按住 🎤 按钮 → AudioRecord 采集 PCM (16kHz/16bit/mono) → 每 40ms 流式送入讯飞 ASR → 实时返回部分识别结果显示在输入框 → 松手获取最终结果
- 声纹反馈：AudioRecord 实时 RMS + log10 归一化 → 7 根彩色柱状条随音量跳动

**显示方式：**
- 静止显示：居中大字，自适应缩放（onTextLayout.hasVisualOverflow 实测迭代缩小，每次缩小 12%，下限 10sp）
- 滚动显示：rememberInfiniteTransition + Animatable 驱动匀速线性动画，graphicsLayer.translationX 避免裁剪，wrapContentWidth(unbounded=true) 测量原生宽度
- 滚动/静止切换：底部控制栏 ⏯ 按钮一键切换

**全屏模式：**
- 手机：点击全屏按钮 → 自动旋转横屏 (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) → 系统栏隐藏 → 控制栏 3 秒后自动淡出 → 点击屏幕重新显示
- 平板：仅隐藏系统栏，不旋转
- 防 Activity 重建：AndroidManifest 配置 configChanges="orientation|screenSize|screenLayout"

**效果与字体：**
- 5 种文字特效：NONE（普通白字）/ GRADIENT（四色渐变刷）/ SHADOW（亮黄+黑色投影）/ GLOW（青色光晕双层）/ BOUNCE（缩放脉动+HSL 色相旋转）/ LED（橙红暖光+宽字距）
- 7 种字体：默认/默认粗体/默认细体/衬线/无衬线细/等宽/手写（FontFamily + FontWeight 组合）
- 9 种颜色：自动跟随背景 + 8 个预设色（白黄红绿蓝橙紫青）
- 字体和颜色通过弹出式 DropdownMenu 卡片选择
- 支持双指缩放 (detectTransformGestures)

### 4.2 字幕模式

**触发方式：** 首页左上角滑块从"展板"切到"字幕"

**输入方式：**
- 与展板模式共享打字和语音输入
- 额外支持文件导入：点击"📄 导入文件"按钮 → 系统文件选择器（支持 txt/docx/md/json/xml/csv）
- 导入后按钮显示"已加载 N 句"

**断句规则：**
```
replace(Regex("\\s+"), " ").split(Regex("(?<=[。！？!?\\n])")).map{trim}.filter{isNotBlank}
```
按句号、问号、感叹号、换行符拆分，中文和英文标点均支持。

**文件解析：**
- .docx：ZipFile 解压 → 读取 word/document.xml → XmlPullParser 提取 <w:t> 标签文本
- .txt/.md/.json/.xml/.csv：bufferedReader().readText() 直接读取
- 未知格式：尝试纯文本读取，失败则返回空列表并提示"不支持此文件格式"

**播报逻辑：**
- 进入显示时：inputViewModel 的文字按断句规则拆分 → 加上已导入的文件句子 → 合并加载到 DisplayViewModel
- 文字输入内容排在前面，导入文件内容排在后面
- 自动播放：LaunchedEffect + delay，间隔 = 3000 / scrollSpeed 毫秒（与速度滑块联动）
- 手动控制：上一句/暂停/下一句，支持点击字幕卡片跳到下一句

**控制栏（字幕模式）：**
- 字号滑块 + 速度滑块（与展板模式共享同一个 ViewModel 状态）
- ⏮ 上一句 / ⏯ 暂停播放 / ⏭ 下一句 / ⊠ 全屏

### 4.3 语音输入（讯飞 SparkChain 集成）

**SDK 初始化（WordShowApp.kt）：**
```kotlin
SparkChainConfig.builder()
    .appID("7a06bdcf")
    .apiKey("642d26ae7b75787595c01fb40dc11c08")
    .apiSecret("ODk4OTdlYjcyZWRhODgwZTkzMjYzNzg2")
    .workDir(filesDir.absolutePath + "/sparkchain")
    .logLevel(100)
SparkChain.getInst().init(this, config)
```

**语音识别流程：**
1. 用户按住 🎤 → 检查权限 → 创建 ASR 实例
2. ASR.language("zh_cn").domain("iat").accent("mandarin").vadEos(1000).ptt(true)
3. ASR.start(AudioAttributes(16000, "raw", 1, 16, 0))
4. AudioRecord 循环读取 → ShortArray → ByteArray → ASR.write(bytes)
5. ASR 回调 onResult(text, isFinal) → 实时更新输入框
6. 松手 → ASR.stop(false) → 获取最终结果

### 4.4 设备适配

**设备判定（DeviceAdaptive.kt）：**
```kotlin
val shortSideDp = minOf(config.screenWidthDp, config.screenHeightDp)
// 手机：shortSideDp < 600 → 默认64sp/最大300sp/速度10x
// 平板：shortSideDp >= 600 → 默认180sp/最大700sp/速度20x
```

### 4.5 自定义短语

- 展板模式下 FlowRow 双行展示预设短语
- 末尾"＋"按钮 → AlertDialog 管理窗口 → 输入文字添加 / 逐条删除
- DataStore 持久化，手动更新本地状态即时响应

### 4.6 预设配置

- 设置页（📂图标进入）→ 当前配置摘要实时显示
- 点击"＋"保存当前配置为命名预设
- 点击预设卡片一键加载全部配置（字体/颜色/特效/速度/字号）
- DataStore 持久化

### 4.7 历史记录

- 每次进入显示时自动保存输入文字
- 去重、最多 50 条、最新在前
- 输入页右上角 ≡ 图标 → ModalBottomSheet 展示
- 点击复用、滑动删除

### 4.8 深浅背景

- 控制栏 ☀ 按钮切换
- 深色模式：纯黑背景 #0A0A0A + 深灰控制栏
- 浅色模式：浅灰背景 #E8EBED + 白色玻璃控制栏（默认）
- 默认跟随系统主题，用户偏好持久化到 DataStore

---

## 五、关键配置说明

### 5.1 Gradle 依赖（libs.versions.toml）

```toml
[versions]
agp = "9.2.1"
kotlin = "2.2.10"
composeBom = "2026.02.01"
navigationCompose = "2.8.5"
lifecycleViewmodelCompose = "2.8.7"
datastorePreferences = "1.1.1"

[libraries]
# Compose BOM 管理的库（无需单独版本号）
androidx-compose-ui, compose-ui-graphics, compose-ui-tooling,
compose-ui-tooling-preview, compose-material3, material-icons-extended,
compose-ui-test-manifest, compose-ui-test-junit4

# 独立版本管理的库
androidx-core-ktx = "1.13.1"
androidx-activity-compose = "1.9.3"
androidx-lifecycle-runtime-ktx = "2.8.7"
androidx-lifecycle-viewmodel-compose = "2.8.7"
androidx-navigation-compose = "2.8.5"
androidx-datastore-preferences = "1.1.1"
androidx-material3-window-size-class
junit = "4.13.2"
androidx-junit = "1.2.1"
androidx-espresso-core = "3.6.1"

# 本地 AAR（讯飞 SDK）
implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
```

### 5.2 AndroidManifest 权限

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 5.3 签名配置

- 密钥文件：app/kanjian.jks (RSA 2048, SHA384withRSA, 有效期 10000 天)
- 配置文件：keystore.properties（已加入 .gitignore）
- 密码：kanjian123 / alias：kanjian
- Release APK：17MB，已启用 R8 混淆 + 资源压缩

### 5.4 混淆规则（proguard-rules.pro）

```
-keep class com.iflytek.sparkchain.** {*;}
-keep class com.google.gson.** {*;}      # SparkChain 依赖 Gson
-dontwarn com.google.gson.**
```

---

## 六、UI 设计规范

### 6.1 颜色系统

| 颜色 | 色值 | 用途 |
|------|------|------|
| GlassBg | #E8EBED | 浅色模式背景 |
| GlassWhite | 0xCCFFFFFF | 玻璃面板主色 |
| GlassGray | 0x88E0E4E8 | 玻璃面板辅色 |
| GlassBorder | 0x20FFFFFF | 玻璃边框 |
| TextDark | #2C3035 | 主文字色 |
| TextGray | #7A8088 | 次要文字色 |
| 深色背景 | #0A0A0A | 深色模式背景 |

### 6.2 阴影规范

| 组件 | 阴影 |
|------|------|
| 控制栏 | 14dp, alpha 0.2 |
| 滑块面板 | 14dp, alpha 0.2 |
| 按钮 | 14dp, alpha 0.2 |
| 切换滑块 | 14dp, alpha 0.2 |
| 麦克风按钮 | 14dp, alpha 0.2 |

### 6.3 圆角规范

| 组件 | 圆角 |
|------|------|
| 控制栏 | 18dp |
| 滑块面板 | 12dp |
| 按钮 | 26dp (胶囊) |
| 切换滑块轨道 | 20dp |
| 输入框 | 16dp |
| 字幕卡片 | 16dp |

---

## 七、关键问题与解决方案

| # | 问题 | 根因 | 解决方案 |
|---|------|------|----------|
| 1 | 国产设备语音不可用 | RecognizerIntent 依赖 Google 服务 | 替换为讯飞 SparkChain SDK |
| 2 | 长文本堆叠溢出 | lineHeight 未随字号缩放 | lineHeight = fontSize × 1.5 |
| 3 | 长文本字号不准 | 字符数估算不精确 | onTextLayout.hasVisualOverflow 实测迭代 |
| 4 | 横屏文字被遮挡 | Box overlay 布局重叠 | Column weight(1f) 分离文字区和控制栏 |
| 5 | 全屏横竖屏反复切换 | Activity 重建重置 orientation | configChanges 阻止重建 + onClick 主动触发 |
| 6 | 深浅切换需点击两次 | DataStore 异步回流覆盖手动切换 | mutableStateOf 直接驱动，DataStore 只做持久化 |
| 7 | 字体选择无反应 | NONE 特效分支漏传 fontFamily | 统一使用 baseStyle.copy() |
| 8 | RadioButton 点击失效 | Row clickable 和 RadioButton onClick 冲突 | 改用 TextButton + Check 图标 |
| 9 | R8 混淆失败 | Gson 类被 R8 移除 | 添加 -keep class com.google.gson.** |
| 10 | 滚动长文本被裁剪 | offset {} 被父容器裁剪 | graphicsLayer.translationX + wrapContentWidth(unbounded=true) |

---

## 八、待优化事项

1. 自动化测试覆盖（当前无单元测试和 UI 测试）
2. CI/CD 流水线（GitHub Actions 构建 + 签名）
3. 字幕导出 SRT 格式
4. 截图分享功能
5. 桌面 Widget 快捷入口
6. 多语言国际化支持
7. App 上架应用商店（需准备截图、隐私政策等）

---

## 九、Git 提交规范

- 分支策略：master（主分支）+ demo（开发分支）
- 提交信息：中文简述改动内容
- 提交频率：每完成一个独立功能模块提交一次
- 共 30+ 次提交，涵盖完整开发历程
