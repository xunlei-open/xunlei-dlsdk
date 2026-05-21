# 迅雷下载 Java Linux x64 平台包指南

这是 `xl-dl-desktop-1.0.0.jar` 的 Linux x64 平台动态库包。

从 [`xunlei-dlsdk-java-desktop-1.0.0.zip`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.0.zip) 的 `libs/` 目录中选择 `xl-dl-native-linux-x64-1.0.0.jar`。

使用方式：

```gradle
implementation(files("libs/xl-dl-desktop-1.0.0.jar"))
runtimeOnly(files("libs/xl-dl-native-linux-x64-1.0.0.jar"))
```
