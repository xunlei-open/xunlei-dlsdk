# 迅雷下载 Java macOS Universal 平台包指南

这是 `xl-dl-desktop-1.0.3.jar` 的 macOS universal 平台动态库包。

从 [`xunlei-dlsdk-java-desktop-1.0.3.zip`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.3.zip) 的 `libs/` 目录中选择 `xl-dl-native-macos-universal-1.0.3.jar`。

使用方式：

```gradle
implementation(files("libs/xl-dl-desktop-1.0.3.jar"))
runtimeOnly(files("libs/xl-dl-native-macos-universal-1.0.3.jar"))
```
