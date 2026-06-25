# 迅雷下载 Swift macOS 示例指南

XL Download Swift macOS 接入适用于 macOS 11 或更高版本。

## 支持平台

| macOS Universal |
| --- |
| ✅ |

## 环境要求

- Swift 5.9 或更高版本。
- Xcode Command Line Tools。
- `Binaries/macos/libdk.dylib`。

## 安装

下载 [xunlei-dlsdk-swift-1.0.1.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-swift-1.0.1.zip) 并解压：

```bash
unzip xunlei-dlsdk-swift-1.0.1.zip
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

## 运行示例

运行前需要让动态链接器能找到 macOS 动态库：

```bash
export DYLD_LIBRARY_PATH="$PWD/Binaries/macos:$DYLD_LIBRARY_PATH"
swift run xl-dl-swift-macos-example
```

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
