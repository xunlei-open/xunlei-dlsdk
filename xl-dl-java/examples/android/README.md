# 迅雷下载 Java Android 示例指南

Android Java 示例代码。

示例依赖 `xunlei-dlsdk-java-android-1.0.0.aar`：

```gradle
implementation(files("libs/xunlei-dlsdk-java-android-1.0.0.aar"))
```

示例是独立 Gradle 项目。接入应用可下载 [xunlei-dlsdk-java-android-1.0.0.aar](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-java-android-1.0.0.aar)，放入应用工程的 `libs/` 目录后引用。

在 SDK 仓库内调试本示例时，可以先发布到 Maven local：

```bash
cd ../..
./gradlew publishToMavenLocal
```

然后在本示例目录构建：

```bash
gradle assembleDebug
```

Java API 仍然使用：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```
