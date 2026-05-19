# Java Desktop 示例

桌面 Java 示例代码。

示例使用 Java SDK 入口：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

示例依赖由两部分组成：

- Desktop 包：`xl-dl-desktop-1.0.0.jar`
- 目标平台 native 包：例如 `xl-dl-native-windows-x64-1.0.0.jar`

示例是独立 Gradle 项目。接入应用按上级 README 从 GitHub Release 下载桌面端压缩包，并从中选择 JAR；源码调试时，本示例使用 Maven local 模拟构建产物：

```bash
cd ../..
./gradlew publishToMavenLocal
```

然后在本示例目录运行：

```bash
gradle run
```
