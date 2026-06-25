# 迅雷下载 Java Windows x86 平台包指南

这是 `xl-dl-desktop-1.0.1.jar` 的 Windows x86 平台动态库包。

从 [`xunlei-dlsdk-java-desktop-1.0.1.zip`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.1.zip) 的 `libs/` 目录中选择 `xl-dl-native-windows-x86-1.0.1.jar`。

Gradle 依赖示例：

```gradle
implementation(files("libs/xl-dl-desktop-1.0.1.jar"))
runtimeOnly(files("libs/xl-dl-native-windows-x86-1.0.1.jar"))
```
