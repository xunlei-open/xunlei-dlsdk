# XL Download Node.js SDK

XL Download Node.js SDK 适用于在 Node.js 桌面工具（例如 Electron 应用）中接入迅雷下载能力，支持 TypeScript 和 JavaScript 调用。

## 支持平台

| Windows x64 | Windows x86 | macOS Universal | Linux x64 |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

## 环境要求

- Node.js 18 或更高版本。

## 包说明

- `@xunlei-open/dlsdk`：主 API 包。
- `@xunlei-open/dlsdk-native-windows-x64`
- `@xunlei-open/dlsdk-native-windows-x86`
- `@xunlei-open/dlsdk-native-macos-universal`
- `@xunlei-open/dlsdk-native-linux-x64`

## npm 安装

在应用项目中安装主包，并按应用要支持的平台安装对应 native 包。跨平台应用可以同时安装多个 native 包，运行时会加载当前平台对应的动态库。

| 目标平台 | 安装命令 |
| --- | --- |
| Windows x64 | `npm install @xunlei-open/dlsdk @xunlei-open/dlsdk-native-windows-x64` |
| Windows x86 | `npm install @xunlei-open/dlsdk @xunlei-open/dlsdk-native-windows-x86` |
| macOS Universal | `npm install @xunlei-open/dlsdk @xunlei-open/dlsdk-native-macos-universal` |
| Linux x64 | `npm install @xunlei-open/dlsdk @xunlei-open/dlsdk-native-linux-x64` |

例如接入 Windows x64：

```bash
npm install @xunlei-open/dlsdk @xunlei-open/dlsdk-native-windows-x64
```

## 最小示例

```ts
import fs from "node:fs";
import { XLDownloadAPI, ERROR_ALREADY_INIT, ERROR_SUCCESS, TASK_STATUS_SUCCEEDED, TASK_STATUS_FAILED } from "@xunlei-open/dlsdk";

const appId = "your-app-id";
const apiKey = "your-api-key";
const configPath = "/tmp/xl_dl_sdk_conf";
const savePath = "/tmp/ThunderDownload";

fs.mkdirSync(configPath, { recursive: true });
fs.mkdirSync(savePath, { recursive: true });

const sdk = new XLDownloadAPI();

const initResult = sdk.init({
  appId,
  appVersion: "1.0",
  configPath,
  saveTasks: true
});
if (initResult !== ERROR_SUCCESS && initResult !== ERROR_ALREADY_INIT) {
  throw new Error(`init failed: ${initResult}`);
}

const token = await sdk.getLoginToken(apiKey);
const login = sdk.login(token.token);
if (login.result !== ERROR_SUCCESS) {
  throw new Error(`login failed: ${login.result}`);
}

const task = sdk.createP2spTask({
  url: "https://example.com/file.zip",
  savePath,
  saveName: "file.zip"
});

if (task.result === ERROR_SUCCESS) {
  sdk.startTask(task.taskId);
}

while (true) {
  const state = sdk.getTaskState(task.taskId);
  process.stdout.write(`\rstate:${state.state.stateCode} downloaded:${state.state.downloadedSize}/${state.state.totalSize} speed:${state.state.speed}`);
  if (
    state.result !== ERROR_SUCCESS ||
    state.state.stateCode === TASK_STATUS_SUCCEEDED ||
    state.state.stateCode === TASK_STATUS_FAILED
  ) {
    console.log();
    break;
  }
  await new Promise(resolve => setTimeout(resolve, 1000));
}

sdk.uninit();
```

完整可运行示例见 [`examples/basic-download.ts`](https://github.com/xunlei-open/xunlei-dlsdk/blob/main/xl-dl-node/examples/basic-download.ts)。

## 相关文档

- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
