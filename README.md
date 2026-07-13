# SINANO

面向中国科学院苏州纳米技术与纳米仿生研究所（SINANO）文萃路园区师生的 Android 辅助工具，集成了**文萃入园码**、**取件码管理**、**纳米所信息入口**与**桌面小部件**等功能。

## 功能

### 1. 文萃入园码
- 通过文萃入园 ID 自动登录并换取 `satoken`
- 生成园区闸机可识别的二维码
- 页面内按设定间隔自动刷新（默认 8 秒，可在设置中调整 3 ~ 60 秒）
- 进入二维码页面时自动提高屏幕亮度，方便闸机扫描
- 支持手动点击刷新
- 二维码生成失败或 Token 失效时自动重新登录并重试

### 2. 取件码
- 本地读取手机短信，自动提取快递/驿站/丰巢等取件码
- 仅解析最近 30 天内的短信
- 支持标记“已取件”，并可查看已取件列表
- 所有短信数据**仅在本地处理，不会上传**

### 3. 纳米所
- 所区信息入口页
- 从此页进入“文萃入园码”子页面

### 4. 我
- **设置**：配置文萃入园 ID、屏幕亮度增强、二维码自动刷新间隔
- **关于**：应用信息与作者说明

### 5. 桌面小部件
- 添加“文萃入园码二维码”小部件到桌面
- 每 8 秒尝试自动刷新（受系统省电策略限制，实际间隔可能变长）
- 点击二维码手动刷新
- 应用被杀死后，WorkManager 会作为降级方案周期性尝试恢复刷新

### 6. 快捷设置磁贴
- 下拉状态栏的快捷设置中可添加“文萃入园码”磁贴
- 点击磁贴直接跳转到二维码页面

## 获取文萃入园 ID

1. 在手机上安装原始慧湖通/文萃入园 App 并登录。
2. 使用抓包工具（如 HttpCanary、Charles、Fiddler、PCap Remote 等）对手机流量进行抓包。
3. 找到请求：
   ```
   GET https://api.215123.cn/web-app/auth/certificateLogin?openId=xxxxxxxx
   ```
4. 记录 URL 中的 `openId` 参数值，此即**文萃入园 ID**。
5. 将该值填入本 App 的“设置”页或桌面小部件配置界面。

> 注意：本应用不会上传或泄露你的文萃入园 ID，所有数据仅保存在本地 `SharedPreferences` 中。

## 项目结构

```
HuiHuTong/
├── app/src/main/java/com/example/huihutong/
│   ├── MainActivity.kt                      # 主 Activity，底部导航 + ViewPager2
│   ├── MainViewModel.kt                     # 页面状态与 Toolbar 控制
│   ├── MainPagerAdapter.kt                  # 三个主 Tab 的 Fragment 适配器
│   ├── HuiHuTongApp.kt                      # Application
│   ├── PrefsHelper.kt                       # SharedPreferences 封装（文萃入园 ID、satoken、缩放、刷新间隔等）
│   ├── NanoInstituteFragment.kt             # “纳米所” Tab
│   ├── HuiHuTongFragment.kt                 # “文萃入园码”二维码页面
│   ├── HuiHuTongViewModel.kt                # 二维码刷新逻辑与 UI 状态
│   ├── PickupCodeFragment.kt                # “取件码” Tab：读取短信、展示待取件
│   ├── PickupCodeAdapter.kt                 # 取件码列表适配器
│   ├── PickupCodeStatus.kt / PickupCodeStatusPrefs.kt  # 取件状态与持久化
│   ├── PickedUpFragment.kt                  # 已取件列表
│   ├── ProfileFragment.kt                   # “我” Tab
│   ├── SettingsFragment.kt                  # 设置页
│   ├── AboutFragment.kt                     # 关于页
│   ├── QrCodeTileService.kt                 # 快捷设置磁贴服务
│   ├── api/
│   │   ├── ApiModels.kt                     # API 响应数据类
│   │   ├── HuiHuTongApi.kt                  # Retrofit 接口
│   │   └── HuiHuTongApiService.kt           # API 服务单例
│   ├── qr/
│   │   └── QrCodeGenerator.kt               # ZXing 二维码生成
│   └── widget/
│       ├── HuiHuTongWidgetProvider.kt       # AppWidgetProvider
│       ├── WidgetAlarmReceiver.kt           # 8 秒闹钟接收器
│       ├── HuiHuTongWidgetWorker.kt         # WorkManager 降级刷新
│       ├── WidgetConfigureActivity.kt       # 小部件配置页
│       └── WidgetUpdateHelper.kt            # 刷新逻辑：取码 / 换 Token / 渲染
├── app/src/main/res/
│   ├── layout/
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
- **AndroidX ViewPager2 / Fragment / ViewModel / RecyclerView / SwipeRefreshLayout** — UI
- **AndroidX AppCompat / Material3** — UI 组件

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

### 首次配置

1. 打开 App，进入底部导航“我” → “设置”。
2. 输入抓包得到的**文萃入园 ID**，按需调整刷新间隔与亮度增强开关，点击**保存**。

### 使用文萃入园码

1. 点击底部导航“纳米所”。
2. 点击卡片进入“文萃入园码”页面，二维码会自动生成并按设定间隔刷新。
3. 点击二维码或“刷新二维码”按钮可立即手动刷新。

### 添加桌面小部件

1. 返回桌面，长按空白处 → **添加小部件** → 找到 **“文萃入园码二维码”**。
2. 首次添加会弹出配置页，确认文萃入园 ID 与缩放比例，点击**保存**。
3. 小部件会显示二维码，并尝试每 8 秒自动刷新。
4. 点击二维码图片可立即手动刷新。

### 使用取件码

1. 点击底部导航“取件码”。
2. 授权读取短信权限。
3. 应用会自动读取最近 30 天内的快递短信并提取取件码。
4. 点击取件码可标记为“已取件”；底部“已取件快递”可查看历史记录。

### 添加快捷设置磁贴

1. 下拉状态栏，点击编辑快捷设置。
2. 找到“文萃入园码”磁贴并添加到面板。
3. 点击磁贴即可快速打开二维码页面。

## 关于自动刷新

Android 系统对后台任务有严格限制：

- `WorkManager` 周期性任务最短只能 15 分钟，**无法满足 8 秒**。
- 本项目使用 `AlarmManager` + `BroadcastReceiver` 实现 8 秒循环刷新。
- 在 Android 12（API 31）及以上，精确闹钟需要 `SCHEDULE_EXACT_ALARM` 权限；首次安装后请在系统设置中允许本应用“设置闹钟和提醒”。
- 小米 MIUI 等国产系统可能进一步限制后台闹钟，导致实际刷新间隔大于 8 秒。此时**点击小部件手动刷新**最可靠。
- 同时注册了 `WorkManager` 作为降级方案，在应用被杀死后仍有机会周期性地重新拉取二维码。

## 常见问题

**Q：小部件显示“未配置文萃入园 ID”**
A：长按小部件删除后重新添加，或在“我” → “设置”中填写文萃入园 ID 并保存。

**Q：点击刷新后二维码不变**
A：网络请求失败或返回空时会自动尝试换 Token；若多次失败，请检查文萃入园 ID 是否正确，以及是否被系统限制联网。

**Q：二维码太小/太大**
A：在小部件配置页调整缩放比例；App 内二维码页面会按屏幕宽度自适应。

**Q：取件码列表为空**
A：请确认已授予短信读取权限，且近期有包含“取件码”“菜鸟驿站”“丰巢”等关键字的快递短信。

## 免责声明

本项目仅供学习交流，请遵守相关服务条款。使用本工具产生的任何后果由使用者自行承担。
