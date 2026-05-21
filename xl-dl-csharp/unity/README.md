# 迅雷下载 Unity SDK 指南

XL Download Unity SDK 适用于在 Unity 游戏或应用中接入迅雷下载能力，支持 Windows、macOS、Android 和 iOS 目标平台。

## 支持平台

| Windows x64 | macOS Universal | Android arm64-v8a | iOS |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

代码引用方式：

```csharp
using Xunlei.XlDl.Unity;
```

## 环境要求

- Unity 2021.3 或更高版本。

## 安装

在 Unity 项目的 `Packages/manifest.json` 中加入 API 包，并按项目要支持的平台加入对应 native 包。跨平台项目可以同时加入多个 native 包。

```json
{
  "dependencies": {
    "com.xunlei.open.dlsdk": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity#v1.0.0",
    "com.xunlei.open.dlsdk.native.windows-x64": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.windows-x64#v1.0.0"
  }
}
```

各平台 Git UPM 路径：

| 目标平台 | manifest 依赖项 |
| --- | --- |
| Windows x64 | `"com.xunlei.open.dlsdk.native.windows-x64": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.windows-x64#v1.0.0"` |
| macOS Universal | `"com.xunlei.open.dlsdk.native.macos-universal": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.macos-universal#v1.0.0"` |
| Android arm64-v8a | `"com.xunlei.open.dlsdk.native.android": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.android#v1.0.0"` |
| iOS | `"com.xunlei.open.dlsdk.native.ios": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.ios#v1.0.0"` |

## 使用

```csharp
using Xunlei.XlDl.Unity;

var sdk = new XLDownloadAPI();
const string configPath = "/tmp/xl_dl_sdk_conf";
int initResult = sdk.Initialize(appId, "1.0", configPath, true);
if (initResult != XLDownloadAPI.ErrorSuccess &&
    initResult != XLDownloadAPI.ErrorAlreadyInit)
{
    yield break;
}

LoginTokenResult loginToken = null;
string loginTokenError = null;
yield return sdk.GetLoginToken(apiKey, token => loginToken = token, error => loginTokenError = error);
if (loginTokenError != null || loginToken == null)
{
    sdk.Uninit();
    yield break;
}

var login = sdk.Login(loginToken.Token);
if (login.Result != XLDownloadAPI.ErrorSuccess)
{
    sdk.Uninit();
    yield break;
}

var create = sdk.CreateP2spTask("https://example.com/file.zip", "/tmp/ThunderDownload", "file.zip");
if (create.Result != XLDownloadAPI.ErrorSuccess)
{
    sdk.Uninit();
    yield break;
}

int startResult = sdk.StartTask(create.TaskId);
if (startResult != XLDownloadAPI.ErrorSuccess)
{
    sdk.Uninit();
    yield break;
}

while (true)
{
    var state = sdk.GetTaskState(create.TaskId);
    Debug.Log($"state:{state.State.StateCode} downloaded:{state.State.DownloadedSize}/{state.State.TotalSize} speed:{state.State.Speed}");
    if (state.State.StateCode == XLDownloadAPI.TaskStatusSucceeded ||
        state.State.StateCode == XLDownloadAPI.TaskStatusFailed)
    {
        break;
    }
    yield return new WaitForSeconds(1);
}

sdk.Uninit();
```

## 示例项目

完整示例见 Package Manager 中的 `基础下载示例`，源码位于 [示例代码](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-csharp/unity/Samples~/BasicDownload)。

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-csharp/unity)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
