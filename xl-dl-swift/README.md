# XL Download Swift SDK

XL Download Swift SDK 适用于在 macOS 和 iOS 应用中接入迅雷下载能力，面向 Swift 项目使用。

## 支持平台

| macOS Universal | iOS |
| --- | --- |
| ✅ | ✅ |

## 环境要求

- Swift 5.9 或更高版本。
- Xcode Command Line Tools。
- macOS 11 或更高版本 / iOS 13 或更高版本。

## 接入入口

| 场景 | 文档 |
| --- | --- |
| macOS | [Examples/macOS](./Examples/macOS/) |
| iOS | [Examples/iOS](./Examples/iOS/) |

## 安装

下载 GitHub Release 压缩包：

```bash
curl -L -o xunlei-dlsdk-swift-1.0.0.zip \
  https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-swift-1.0.0.zip
unzip xunlei-dlsdk-swift-1.0.0.zip
```

在你的 SwiftPM 项目中通过本地路径引用：

```swift
dependencies: [
    .package(path: "../xunlei-dlsdk-swift")
]
```

然后在 target 中依赖 `XlDlSwift`：

```swift
.target(
    name: "App",
    dependencies: ["XlDlSwift"]
)
```

命令行运行 macOS 程序时，需要让动态链接器找到 `libdk.dylib`：

```bash
export DYLD_LIBRARY_PATH="$PWD/xunlei-dlsdk-swift/Binaries/macos:$DYLD_LIBRARY_PATH"
```

iOS 项目需要把 `xunlei-dlsdk-swift/Binaries/ios/dk.framework` 加到 App target，并由 Xcode 完成 embed 和签名。

## 最小示例

```swift
import Foundation
import XlDlSwift

let sdk = XLDownloadAPI()

let initResult = sdk.initialize(
    appId: "your-app-id",
    appVersion: "1.0",
    configPath: "/tmp/xl_dl_sdk_conf",
    saveTasks: true
)

guard initResult == XLDLSuccess || initResult == XLDLAlreadyInit else {
    fatalError("init failed: \(initResult)")
}

let token = try sdk.getLoginToken(apiKey: "your-api-key")
let login = sdk.login(token: token.token)
guard login.result == XLDLSuccess else {
    fatalError("login failed: \(login.result)")
}

let task = sdk.createP2SPTask(
    url: "https://example.com/file.zip",
    savePath: "/tmp/ThunderDownload",
    saveName: "file.zip"
)

if task.result == XLDLSuccess {
    _ = sdk.startTask(taskId: task.taskId)
}

while true {
    let stateResult = sdk.getTaskState(taskId: task.taskId)
    if let state = stateResult.state {
        print("\rstate:\(state.stateCode) downloaded:\(state.downloadedSize)/\(state.totalSize) speed:\(state.speed)", terminator: "")
        fflush(stdout)
        if state.stateCode == XLDownloadTaskStatusSucceeded || state.stateCode == XLDownloadTaskStatusFailed {
            print()
            break
        }
    } else {
        break
    }
    Thread.sleep(forTimeInterval: 1)
}

_ = sdk.uninit()
```

完整示例见 `Examples/macOS` 和 `Examples/iOS`。

## 相关文档

- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
