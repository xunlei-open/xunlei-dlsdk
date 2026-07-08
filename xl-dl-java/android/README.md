# 迅雷下载 Java Android SDK 指南

XL Download Java Android SDK 适用于在 Android arm64-v8a 应用中接入迅雷下载能力。

## 支持平台

| Android arm64-v8a |
| --- |
| ✅ |

## 环境要求

- Android minSdk 23 或更高版本。

## 安装

下载 [`xunlei-dlsdk-java-android-1.0.2.aar`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-android-1.0.2.aar)，放入应用工程的 `libs/` 目录后引入：

```gradle
implementation(files("libs/xunlei-dlsdk-java-android-1.0.2.aar"))
```

Java API 入口：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

## 使用

```java
import android.content.Context;

import com.xunlei.open.dlsdk.XLDownloadAPI;

import java.io.File;

public final class DownloadSdk {
    public static void startDemoDownload(Context context) {
        new Thread(() -> {
            try {
                runDownload(context.getApplicationContext());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void runDownload(Context context) throws Exception {
        String cfgPath = context.getFilesDir().getAbsolutePath() + "/xl_dl_sdk_conf";
        File saveDir = context.getExternalFilesDir(null);
        if (saveDir == null) {
            throw new IllegalStateException("external files dir is unavailable");
        }
        String savePath = saveDir.getAbsolutePath();

        int initResult = XLDownloadAPI.init("your-app-id", "1.0", cfgPath, true);
        if (initResult != XLDownloadAPI.ERROR_SUCCESS
                && initResult != XLDownloadAPI.ERROR_ALREADY_INIT) {
            throw new IllegalStateException("init failed: " + initResult);
        }

        try {
            XLDownloadAPI.LoginTokenResult token = XLDownloadAPI.getLoginToken("your-api-key");
            if (token.code != 0 || token.token == null || token.token.length() == 0) {
                throw new IllegalStateException("get login token failed: " + token.message);
            }

            XLDownloadAPI.LoginResult login = XLDownloadAPI.login(token.token);
            if (login.result != XLDownloadAPI.ERROR_SUCCESS) {
                throw new IllegalStateException("login failed: " + login.result);
            }

            XLDownloadAPI.CreateTaskResult task = XLDownloadAPI.createP2spTask(
                    "https://example.com/file.zip",
                    savePath,
                    "file.zip");
            if (task.result != XLDownloadAPI.ERROR_SUCCESS) {
                throw new IllegalStateException("create task failed: " + task.result);
            }

            int startResult = XLDownloadAPI.startTask(task.taskId);
            if (startResult != XLDownloadAPI.ERROR_SUCCESS) {
                XLDownloadAPI.deleteTask(task.taskId, true);
                throw new IllegalStateException("start task failed: " + startResult);
            }

            while (true) {
                Thread.sleep(1000);
                XLDownloadAPI.TaskStateResult stateResult = XLDownloadAPI.getTaskState(task.taskId);
                if (stateResult.result != XLDownloadAPI.ERROR_SUCCESS) {
                    break;
                }
                XLDownloadAPI.TaskState state = stateResult.state;
                if (state.stateCode == XLDownloadAPI.TASK_STATUS_SUCCEEDED
                        || state.stateCode == XLDownloadAPI.TASK_STATUS_FAILED) {
                    break;
                }
            }
        } finally {
            XLDownloadAPI.uninit();
        }
    }
}
```

## 示例项目

完整示例见 [示例代码](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/examples/android)。

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/android)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
