# 慧湖通闸机二维码（MIUI 桌面小部件版）

原 Android App 的复刻版本，以**小米手机桌面小部件**为主要入口。添加小部件到桌面后，可自动刷新并显示慧湖通闸机二维码；点击二维码即可手动刷新。

## 功能

- 通过 `openId` 自动登录并换取 `satoken`
- 每 8 秒尝试自动刷新二维码（受系统省电策略限制，实际间隔可能变长）
- 点击小部件手动刷新
- 二维码生成失败/Token 失效时自动重新登录并重试
- 支持调整二维码显示缩放比例（0.4 ~ 1.0）
- 配置页与小部件共用同一套持久化配置

## 抓包获取 openId

1. 在手机上安装原始慧湖通 App 并登录。
2. 使用抓包工具（如 HttpCanary、Charles、Fiddler、PCap Remote 等）对手机流量进行抓包。
3. 找到请求：
   ```
   GET https://api.215123.cn/web-app/auth/certificateLogin?openId=xxxxxxxx
   ```
4. 记录 URL 中的 `openId` 参数值。
5. 将该值填入本 App 的配置页或小部件配置界面。

> 注意：本 App 不会上传或泄露你的 openId，所有数据仅保存在本地 `SharedPreferences` 中。

## 项目结构

```
HuiHuTong/
├── app/src/main/java/com/example/huihutong/
│   ├── MainActivity.kt                    # 主配置/预览页
│   ├── HuiHuTongApp.kt                    # Application
│   ├── PrefsHelper.kt                     # SharedPreferences 封装
│   ├── api/
│   │   ├── ApiModels.kt                   # API 响应数据类
│   │   ├── HuiHuTongApi.kt                # Retrofit 接口
│   │   └── HuiHuTongApiService.kt         # API 服务单例
│   ├── qr/
│   │   └── QrCodeGenerator.kt             # ZXing 二维码生成
│   └── widget/
│       ├── HuiHuTongWidgetProvider.kt     # AppWidgetProvider
│       ├── WidgetAlarmReceiver.kt         # 8 秒闹钟接收器
│       ├── HuiHuTongWidgetWorker.kt       # WorkManager 降级刷新
│       ├── WidgetConfigureActivity.kt     # 小部件配置页
│       └── WidgetUpdateHelper.kt          # 刷新逻辑：取码/换 Token/渲染
├── app/src/main/res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_widget_configure.xml
│   │   └── widget_layout.xml
│   ├── xml/appwidget_info.xml
│   └── ...
└── README.md
```

## 依赖

主要依赖（见 `app/build.gradle.kts`）：

- **ZXing Core** (`com.google.zxing:core`) — 二维码生成
- **Retrofit2 + Gson Converter** — 网络请求与 JSON 解析
- **OkHttp Logging Interceptor** — 日志
- **AndroidX WorkManager** — 后台降级刷新
- **AndroidX AppCompat / Material3** — UI

## 如何运行

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34/35

### 编译安装

1. 用 Android Studio 打开本项目。
2. 等待 Gradle 同步完成。
3. 连接手机或启动模拟器。
4. 点击 **Run**（▶）安装 App。

或使用命令行：

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

> Windows 上若 `./gradlew` 无法执行，可使用 `gradlew.bat assembleDebug`。

## 使用说明

1. 打开 App，输入抓包得到的 `openId`，调整缩放比例，点击**保存**。
2. 返回桌面，长按空白处 → **添加小部件** → 找到 **“慧湖通二维码”**。
3. 首次添加会弹出配置页，再次确认 `openId` 和缩放比例，点击**保存**。
4. 小部件会显示二维码，并尝试每 8 秒自动刷新。
5. 点击二维码图片可立即手动刷新。
6. 点击状态文字可打开主 App。

## 关于 8 秒自动刷新

Android 系统对后台任务有严格限制：

- `WorkManager` 周期性任务最短只能 15 分钟，**无法满足 8 秒**。
- 本项目使用 `AlarmManager` + `BroadcastReceiver` 实现 8 秒循环刷新。
- 在 Android 12（API 31）及以上，精确闹钟需要 `SCHEDULE_EXACT_ALARM` 权限；首次安装后请在系统设置中允许本应用“设置闹钟和提醒”。
- 小米 MIUI 等国产系统可能进一步限制后台闹钟，导致实际刷新间隔大于 8 秒。此时**点击小部件手动刷新**最可靠。
- 同时注册了 `WorkManager` 作为降级方案，在应用被杀死后仍有机会周期性地重新拉取二维码。

## 常见问题

**Q：小部件显示“未配置 openId”**
A：长按小部件删除后重新添加，或在主 App 中填写 openId 并保存。

**Q：点击刷新后二维码不变**
A：网络请求失败或返回空时会自动尝试换 Token；若多次失败，请检查 openId 是否正确，以及是否被系统限制联网。

**Q：二维码太小/太大**
A：在小部件配置页或主 App 中调整缩放比例（0.4 ~ 1.0）。

## 免责声明

本项目仅供学习交流，请遵守相关服务条款。使用本工具产生的任何后果由使用者自行承担。
