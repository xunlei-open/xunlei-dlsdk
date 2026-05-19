# 平台动态库包

`Xunlei.Open.DlSdk` 的平台动态库按 native NuGet 包分发。项目可以按要支持的目标运行时引用对应 native 包，跨 RID 发布时可以引用多个。

| 运行时 | 包名 |
| --- | --- |
| Windows x64 | `Xunlei.Open.DlSdk.Native.win-x64` |
| Windows x86 | `Xunlei.Open.DlSdk.Native.win-x86` |
| Linux x64 | `Xunlei.Open.DlSdk.Native.linux-x64` |
| macOS universal | `Xunlei.Open.DlSdk.Native.osx-universal` |

构建 native 包：

```bash
dotnet pack native/win-x64/Xunlei.XlDl.Native.win-x64.csproj -c Release -o artifacts
```
