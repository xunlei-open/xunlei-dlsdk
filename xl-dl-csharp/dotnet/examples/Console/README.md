# 控制台示例

构建并运行：

```bash
dotnet run --project examples/Console/Xunlei.XlDl.ConsoleExample.csproj
```

NuGet 接入需要引用托管包，并按要运行的目标平台引用对应 native 包：

```xml
<PackageReference Include="Xunlei.Open.DlSdk" Version="1.0.0" />
<PackageReference Include="Xunlei.Open.DlSdk.Native.win-x64" Version="1.0.0" />
```
