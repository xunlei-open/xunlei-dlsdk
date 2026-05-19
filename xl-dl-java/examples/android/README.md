# Java Android 示例

Android Java 示例代码。

示例依赖 `xunlei-dlsdk-java-android-1.0.0.aar`：

```gradle
implementation(files("libs/xunlei-dlsdk-java-android-1.0.0.aar"))
```

示例是独立 Gradle 项目。接入应用按上级 README 从 GitHub Release 下载 AAR；源码调试时，本示例使用 Maven local 模拟构建产物：

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
