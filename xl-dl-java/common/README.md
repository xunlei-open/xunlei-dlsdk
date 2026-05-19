# Java 公共 API

这里说明 Java Desktop 和 Java Android 共用的下载 API。应用通过同一个入口完成初始化、登录、任务创建和任务管理。

对外 package 固定为：

```java
import com.xunlei.open.dlsdk.XLDownloadAPI;
```

主要能力：

- 初始化和反初始化 SDK。
- 获取 loginToken。
- 登录 SDK。
- 创建、启动、停止、删除下载任务。
- 查询任务状态和任务信息。
