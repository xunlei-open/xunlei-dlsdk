# xl-dl-android

XL Download Java Android SDK 适用于在 Android arm64-v8a 应用中接入迅雷下载能力。

## 支持平台

| Android arm64-v8a |
| --- |
| ✅ |

## 环境要求

- Android minSdk 23 或更高版本。

## 安装

下载 [`xunlei-dlsdk-java-android-1.0.0.aar`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-android-1.0.0.aar)，放入应用工程的 `libs/` 目录后引入：

```gradle
implementation(files("libs/xunlei-dlsdk-java-android-1.0.0.aar"))
```

Java API 入口：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

## 使用

```java
import android.content.Context;
import com.xunlei.open.dlsdk.XLDownloadAPI;

public final class DownloadSdk {
    public static void init(Context context) {
        int initResult = XLDownloadAPI.init(
                "your-app-id",
                "1.0",
                "/tmp/xl_dl_sdk_conf",
                true);
        if (initResult != XLDownloadAPI.ERROR_SUCCESS
                && initResult != XLDownloadAPI.ERROR_ALREADY_INIT) {
            throw new IllegalStateException("init failed: " + initResult);
        }
    }
}
```

## 示例项目

完整示例见 [`examples/android`](../examples/android)。

## 相关文档

- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
