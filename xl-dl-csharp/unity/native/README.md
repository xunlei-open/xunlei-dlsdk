# 迅雷下载 Unity 平台动态库包指南

Unity SDK 的 API 包为 `com.xunlei.open.dlsdk`，平台动态库通过对应的 native UPM 包提供。

当前支持的平台：

| 目标平台 | native 包路径 |
| --- | --- |
| Windows x64 | `unity/native/com.xunlei.open.dlsdk.native.windows-x64` |
| macOS Universal | `unity/native/com.xunlei.open.dlsdk.native.macos-universal` |
| Android arm64-v8a | `unity/native/com.xunlei.open.dlsdk.native.android` |
| iOS | `unity/native/com.xunlei.open.dlsdk.native.ios` |

项目可以按需要引入一个或多个平台包；Android native 包仅支持 `arm64-v8a`。
