# 迅雷下载 Java SDK 指南

XL Download Java SDK 面向 Java/Kotlin 应用开发者，支持桌面端和 Android 应用接入迅雷下载能力。

按应用运行环境选择接入方式：

- Java Desktop SDK：Windows、macOS、Linux 桌面应用。
- Java Android SDK：Android arm64-v8a 应用。

两个 SDK 都使用同一个核心入口类：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

## 支持平台

| SDK | Windows x64 | Windows x86 | macOS Universal | Linux x64 | Android arm64-v8a |
| --- | --- | --- | --- | --- | --- |
| Java Desktop | ✅ | ✅ | ✅ | ✅ | ❌ |
| Java Android | ❌ | ❌ | ❌ | ❌ | ✅ |

## 接入入口

| 场景 | 文档 | 安装产物 |
| --- | --- | --- |
| Java Desktop | [desktop](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/desktop) | [xunlei-dlsdk-java-desktop-1.0.2.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.2.zip) 中的 `xl-dl-desktop-1.0.2.jar` + 按需选择的平台 native JAR |
| Java Android | [android](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java/android) | [xunlei-dlsdk-java-android-1.0.2.aar](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-android-1.0.2.aar) |

## 相关文档

- [Github](https://github.com/xunlei-open/xunlei-dlsdk/tree/main/xl-dl-java)
- [接入流程与凭证申请](https://open.xunlei.com/doc?doc=access_flow)
- [API 参考文档](https://open.xunlei.com/doc?doc=xl_dl_init)
- [错误码说明](https://open.xunlei.com/doc?doc=error_code)
