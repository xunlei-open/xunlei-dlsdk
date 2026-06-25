# 迅雷下载 Java Desktop 平台动态库包指南

Java Desktop 的平台动态库按 native JAR 打包，并随桌面端压缩包一起发布。

下载 [`xunlei-dlsdk-java-desktop-1.0.1.zip`](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.1.zip)，解压后从 `libs/` 目录按需引入目标运行环境匹配的 native JAR：

| 目标平台 | JAR |
| --- | --- |
| Windows x64 | `xl-dl-native-windows-x64-1.0.1.jar` |
| Windows x86 | `xl-dl-native-windows-x86-1.0.1.jar` |
| macOS Universal | `xl-dl-native-macos-universal-1.0.1.jar` |
| Linux x64 | `xl-dl-native-linux-x64-1.0.1.jar` |

Gradle 依赖示例：

```gradle
implementation(files("libs/xl-dl-desktop-1.0.1.jar"))
runtimeOnly(files("libs/xl-dl-native-windows-x64-1.0.1.jar"))
```
