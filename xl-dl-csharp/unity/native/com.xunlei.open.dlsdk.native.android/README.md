# 迅雷下载 Unity Android 平台包指南

这是 `com.xunlei.open.dlsdk` 的 Android 平台动态库包。

当前仅支持 `arm64-v8a` 架构，动态库位于：

```text
Runtime/Plugins/Android/libs/arm64-v8a/libdk.so
```

使用时需要同时引入 API 包：

```json
{
  "dependencies": {
    "com.xunlei.open.dlsdk": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity#v1.0.0",
    "com.xunlei.open.dlsdk.native.android": "https://github.com/xunlei-open/xunlei-dlsdk.git?path=/xl-dl-csharp/unity/native/com.xunlei.open.dlsdk.native.android#v1.0.0"
  }
}
```
