# 迅雷下载 Java Desktop SDK 指南

XL Download Java Desktop SDK 适用于在 Windows、macOS 和 Linux 的 Java 桌面应用中接入迅雷下载能力。

## 支持平台

| Windows x64 | Windows x86 | macOS Universal | Linux x64 |
| --- | --- | --- | --- |
| ✅ | ✅ | ✅ | ✅ |

## 环境要求

- Java 8 或更高版本。

## 安装

下载 [`xunlei-dlsdk-java-desktop-1.0.0.zip`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.0.zip)，解压后把桌面主包和应用要支持的平台 native 包放入应用工程的 `libs/` 目录后引入：

```gradle
implementation(files("libs/xl-dl-desktop-1.0.0.jar"))
runtimeOnly(files("libs/xl-dl-native-windows-x64-1.0.0.jar"))
```

压缩包内包含：

| 目标平台 | JAR |
| --- | --- |
| 主包 | `xl-dl-desktop-1.0.0.jar` |
| Windows x64 | `xl-dl-native-windows-x64-1.0.0.jar` |
| Windows x86 | `xl-dl-native-windows-x86-1.0.0.jar` |
| macOS Universal | `xl-dl-native-macos-universal-1.0.0.jar` |
| Linux x64 | `xl-dl-native-linux-x64-1.0.0.jar` |

跨平台应用可以同时引入多个 native JAR。

## 使用

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;

public class App {
    public static void main(String[] args) throws Exception {
        int initResult = XLDownloadAPI.init(
                "your-app-id",
                "1.0",
                "/tmp/xl_dl_sdk_conf",
                true);
        if (initResult != XLDownloadAPI.ERROR_SUCCESS
                && initResult != XLDownloadAPI.ERROR_ALREADY_INIT) {
            throw new IllegalStateException("init failed: " + initResult);
        }

        XLDownloadAPI.LoginTokenResult token = XLDownloadAPI.getLoginToken("your-api-key");

        XLDownloadAPI.LoginResult login = XLDownloadAPI.login(token.token);
        if (login.result != XLDownloadAPI.ERROR_SUCCESS) {
            XLDownloadAPI.uninit();
            throw new IllegalStateException("login failed: " + login.result);
        }

        XLDownloadAPI.CreateTaskResult task = XLDownloadAPI.createP2spTask(
                "https://example.com/file.zip",
                "/tmp/ThunderDownload",
                "file.zip");
        if (task.result != XLDownloadAPI.ERROR_SUCCESS) {
            XLDownloadAPI.uninit();
            throw new IllegalStateException("create task failed: " + task.result);
        }
        int startResult = XLDownloadAPI.startTask(task.taskId);
        if (startResult != XLDownloadAPI.ERROR_SUCCESS) {
            XLDownloadAPI.uninit();
            throw new IllegalStateException("start task failed: " + startResult);
        }

        while (true) {
            Thread.sleep(1000);
            XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(task.taskId);
            if (stateResult.result != XLDownloadAPI.ERROR_SUCCESS) {
                break;
            }
            XLDownloadAPI.TaskState state = stateResult.state;
            System.out.printf("\rstate:%d downloaded:%d/%d speed:%d",
                    state.stateCode, state.downloadedSize, state.totalSize, state.speed);
            if (state.stateCode == XLDownloadAPI.TASK_STATUS_SUCCEEDED
                    || state.stateCode == XLDownloadAPI.TASK_STATUS_FAILED) {
                System.out.println();
                break;
            }
        }

        XLDownloadAPI.uninit();
    }
}
```

## 示例项目

完整示例见 [示例代码](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/examples/desktop)。

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/desktop)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
