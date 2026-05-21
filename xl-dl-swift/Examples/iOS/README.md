# 迅雷下载 Swift iOS 示例指南

XL Download Swift iOS 接入适用于 iOS 13 或更高版本。

## 支持平台

| iOS |
| --- |
| ✅ |

## 环境要求

- Swift 5.9 或更高版本。
- Xcode。
- `Binaries/ios/dk.framework`。

## 安装

下载 [xunlei-dlsdk-swift-1.0.0.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-swift-1.0.0.zip) 并解压：

```bash
unzip xunlei-dlsdk-swift-1.0.0.zip
```

在 SwiftPM 项目中通过本地路径引用：

```swift
dependencies: [
    .package(path: "../xunlei-dlsdk-swift")
]
```

target 依赖 `XlDlSwift`：

```swift
.target(
    name: "App",
    dependencies: ["XlDlSwift"]
)
```

## Xcode 配置

应用工程需要链接 `Binaries/ios/dk.framework`，并由 Xcode 完成 embed 和签名。

## 使用

```swift
import XlDlSwift

let sdk = XLDownloadAPI()
let initResult = sdk.initialize(
    appId: "your-app-id",
    appVersion: "1.0",
    configPath: "/tmp/xl_dl_sdk_conf",
    saveTasks: true
)
```
