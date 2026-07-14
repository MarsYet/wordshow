# 看见 — 即时文字显示工具

一款支持**手机与平板**的 Android 应用。输入文字或导入文件，在屏幕上以滚动或静止方式**全屏大字显示**，支持语音输入、字体调节、视觉特效与逐句字幕播报。

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green" />
  <img src="https://img.shields.io/badge/minSdk-24-blue" />
  <img src="https://img.shields.io/badge/language-Kotlin-purple" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange" />
</p>

---

## 功能

### 📝 展板模式
- **打字 / 语音输入**，实时同步到显示区
- **静止大字显示**：居中展示，自适应缩放
- **跑马灯滚动**：速度可调，左右滑动拖拽进度
- **全屏模式**：手机自动横屏，控制栏 3 秒自动隐藏
- **语音输入**：基于讯飞 SparkChain，按住说话，松手识别，声纹实时反馈

### 🎬 字幕模式
- 文本按句号/问号/感叹号**自动断句**，逐句播报
- 支持导入 **.docx / .txt / .md / .json / .xml / .csv** 文件
- 播放/暂停、上一句/下一句，速度可调

### 🎨 视觉定制
- **5 种文字特效**：渐变、阴影、发光、跳动、LED 广告牌
- **7 种字体**：默认/衬线/无衬线/等宽/手写及粗细组合
- **9 种颜色**：自动跟随背景 + 8 个预设色

### 🏷 快捷功能
- **预设短语**：双行可滚动，可自定义增删
- **预设配置**：一键保存/加载完整显示配置
- **历史记录**：输入过的文字可复用
- **深浅背景**：一键切换

### 📱 设备适配
- 手机 / 平板自动识别，参数自适应
- 双指缩放字号
- 全屏控制栏自动隐藏

---

## 技术栈

| 类别 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM (ViewModel + StateFlow) |
| 语音 | 讯飞 SparkChain SDK |
| 存储 | DataStore Preferences |
| 构建 | Gradle 9.4.1 (Kotlin DSL) |
| 最低支持 | Android 7.0 (API 24) |

---

## 构建

```bash
# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease
```

> 需要在讯飞开放平台注册并获取 AppID / APIKey / APISecret，填入 `WordShowApp.kt` 中。

---

## 项目结构

```
com.xiao.wordshow/
├── MainActivity.kt              # 应用入口
├── WordShowApp.kt               # SDK 初始化
├── ui/
│   ├── input/                   # 输入页
│   ├── display/                 # 显示页 + 组件
│   ├── settings/                # 预设配置页
│   ├── navigation/              # 路由
│   └── theme/                   # 主题
├── data/
│   ├── model/                   # 数据模型
│   ├── voice/                   # 语音识别封装
│   └── preferences/             # DataStore 存储
└── util/                        # 工具类
```

---

## License

MIT
