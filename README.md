# 💰 SalaryTimer — 薪资实时计时器

> Android 16 (API 36) | Jetpack Compose | Material 3 | Glance Widget

实时计算你每秒赚了多少钱，让你「看见」时间的价值。

---

## ✨ 功能特性

### 核心功能
- **实时薪资计算** — 每秒刷新，精确到小数点后 6 位
- **动态进度环** — 今日工作完成度的可视化展示
- **月度收入追踪** — 本月累计收入和完成百分比
- **下班倒计时** — 距离下班还有多久
- **状态自动识别** — 未上班 / 工作中 / 午休中 / 已下班

### 设置项
| 设置 | 说明 |
|------|------|
| 月薪金额 | 输入你的税前/税后月薪 |
| 上下班时间 | 精确到分钟（默认 09:00 - 18:00） |
| 午休时间 | 扣除午休不计入工时（默认 12:00 - 13:00） |
| 休息制度 | 双休(22天) / 大小周(25天) / 单休(26天) / 法定(21天) / 自定义 |

### 桌面小组件
- 3×2 Glance AppWidget
- 显示实时金额、进度条、状态、倒计时
- 每 30 分钟自动更新

---

## 🏗️ 项目结构

```
SalaryTimer/
├── app/src/main/
│   ├── java/com/salarytimer/
│   │   ├── data/
│   │   │   ├── SalarySettings.kt      # 数据模型 + 休息制度枚举
│   │   │   ├── SalaryDataStore.kt     # DataStore 持久化
│   │   │   └── SalaryCalculator.kt    # 核心计算引擎
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt           # 色彩体系
│   │   │   │   ├── Theme.kt           # Material 3 主题
│   │   │   │   └── Type.kt            # 字体排版
│   │   │   └── screens/
│   │   │       ├── HomeScreen.kt      # 主界面（进度环+卡片）
│   │   │       └── SettingsScreen.kt  # 设置页
│   │   ├── widget/
│   │   │   └── SalaryWidget.kt        # Glance 桌面小组件
│   │   ├── SalaryViewModel.kt         # MVVM ViewModel
│   │   ├── SalaryApp.kt               # Application
│   │   └── MainActivity.kt            # 入口
│   ├── res/
│   │   ├── layout/widget_loading.xml  # Widget 加载态
│   │   ├── xml/salary_widget_info.xml # Widget 元数据
│   │   ├── values/strings.xml
│   │   └── drawable/                  # 矢量图标
│   └── AndroidManifest.xml
├── build.gradle.kts                   # 根构建脚本
├── app/build.gradle.kts               # App 模块构建
└── settings.gradle.kts
```

---

## 🚀 构建 & 运行

### 前提条件
- Android Studio Ladybug (2024.2) 或更高
- JDK 17+
- Android SDK 36

### 步骤
```bash
# 1. 用 Android Studio 打开项目目录
# File → Open → 选择 SalaryTimer/ 文件夹

# 2. 等待 Gradle Sync 完成

# 3. 连接设备或启动模拟器，点击 ▶ Run
```

### 命令行构建
```bash
cd SalaryTimer
./gradlew assembleDebug
# APK 位于 app/build/outputs/apk/debug/app-debug.apk
```

---

## 📐 核心算法

```
每秒薪资 = 月薪 ÷ (月工作天数 × 每日工时 × 3600)

每日工时 = (下班时间 - 上班时间) - (午休结束 - 午休开始)

今日已赚 = 上午有效工作秒数 × 每秒薪资 + 下午有效工作秒数 × 每秒薪资

本月累计 = 已过完整工作天数 × 每日目标 + 今日已赚
```

**休息制度天数：**
- 双休：22 天/月（年 252 工作日 ÷ 12）
- 大小周：约 25 天/月（单周休 1 天，双周休 2 天）
- 单休：26 天/月
- 法定工作日：21 天/月（年 250 工作日 ÷ 12）

---

## 🎨 设计理念

- **深色优先** — 暗色主题护眼，适合长期挂机查看
- **动态色彩** — 支持 Android 12+ Material You 取色
- **微动画** — 工作状态脉动点、进度环平滑过渡
- **信息层次** — 核心金额最大最醒目，辅助信息渐次弱化

---

## 📋 TODO

- [ ] 持久化通知栏（前台 Service 实时显示）
- [ ] 月度统计图表
- [ ] 加班模式（1.5x/2x/3x 薪资倍率）
- [ ] 多语言支持（中/英）
- [ ] Widget 4×1 紧凑模式
- [ ] Material You 动态图标

---

**License:** MIT
