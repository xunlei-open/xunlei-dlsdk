# 迅雷下载 Java Desktop 示例指南

桌面 Java 示例代码。

示例使用 Java SDK 入口：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

示例依赖由两部分组成：

- Desktop 包：`xl-dl-desktop-1.0.1.jar`
- 目标平台 native 包：例如 `xl-dl-native-windows-x64-1.0.1.jar`

示例是独立 Gradle 项目。接入应用可下载 [xunlei-dlsdk-java-desktop-1.0.1.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-desktop-1.0.1.zip)，并从 `libs/` 目录选择桌面主包和目标平台 native JAR。

在 SDK 仓库内调试本示例时，可以先发布到 Maven local：

```bash
cd ../..
./gradlew publishToMavenLocal
```

然后在本示例目录运行：

```bash
gradle run
```
