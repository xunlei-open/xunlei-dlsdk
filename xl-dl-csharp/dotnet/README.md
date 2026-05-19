# XL Download .NET Desktop SDK

XL Download .NET Desktop SDK 适用于在 Windows、macOS 和 Linux 的 .NET 桌面应用中接入迅雷下载能力。

## 支持平台

| Windows x64 | Windows x86 | macOS Universal | Linux x64 |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

## 环境要求

- .NET SDK 8.0 或更高版本。

## 安装

在应用项目中引用托管包，并按应用要支持的运行时引用对应 native 包。跨 RID 发布可以同时引用多个 native 包。

| 目标运行时 | PackageReference |
| --- | --- |
| Windows x64 | `<PackageReference Include="Xunlei.Open.DlSdk.Native.win-x64" Version="1.0.0" />` |
| Windows x86 | `<PackageReference Include="Xunlei.Open.DlSdk.Native.win-x86" Version="1.0.0" />` |
| macOS Universal | `<PackageReference Include="Xunlei.Open.DlSdk.Native.osx-universal" Version="1.0.0" />` |
| Linux x64 | `<PackageReference Include="Xunlei.Open.DlSdk.Native.linux-x64" Version="1.0.0" />` |

例如接入 Windows x64：

```xml
<ItemGroup>
  <PackageReference Include="Xunlei.Open.DlSdk" Version="1.0.0" />
  <PackageReference Include="Xunlei.Open.DlSdk.Native.win-x64" Version="1.0.0" />
</ItemGroup>
```

## 使用

```csharp
using Xunlei.XlDl;

using var sdk = new XLDownloadAPI();

int initResult = sdk.Initialize(
    appId: "your-app-id",
    appVersion: "1.0",
    configPath: "/tmp/xl_dl_sdk_conf",
    saveTasks: true);

if (initResult != XLDownloadAPI.ErrorSuccess &&
    initResult != XLDownloadAPI.ErrorAlreadyInit)
{
    throw new InvalidOperationException($"init failed: {initResult}");
}

LoginTokenResult token = await sdk.GetLoginTokenAsync("your-api-key");

var login = sdk.Login(token.Token);
if (login.Result != XLDownloadAPI.ErrorSuccess)
{
    throw new InvalidOperationException($"login failed: {login.Result}");
}

var task = sdk.CreateP2spTask(
    "https://example.com/file.zip",
    "/tmp/ThunderDownload",
    "file.zip");

if (task.Result == XLDownloadAPI.ErrorSuccess)
{
    sdk.StartTask(task.TaskId);
}

while (true)
{
    var state = sdk.GetTaskState(task.TaskId);
    Console.Write($"\rstate:{state.State.StateCode} downloaded:{state.State.DownloadedSize}/{state.State.TotalSize} speed:{state.State.Speed}");

    if (state.State.StateCode == XLDownloadAPI.TaskStatusSucceeded ||
        state.State.StateCode == XLDownloadAPI.TaskStatusFailed)
    {
        Console.WriteLine();
        break;
    }

    await Task.Delay(TimeSpan.FromSeconds(1));
}

sdk.Uninit();
```

## 示例项目

完整控制台示例见 [`examples/Console`](./examples/Console)。

## 相关文档

- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
