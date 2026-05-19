# com.xunlei.open.dlsdk.native.ios

这个包提供 `com.xunlei.open.dlsdk` 在 Unity iOS Player 使用的 `dk.framework`。

在 Unity 项目中同时引入：

- `com.xunlei.open.dlsdk`
- `com.xunlei.open.dlsdk.native.ios`

Unity 构建 iOS Player 时会把 `Runtime/Plugins/iOS/dk.framework` 作为原生插件加入 Xcode 工程。
