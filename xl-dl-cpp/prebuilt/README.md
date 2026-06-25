# 迅雷下载 C++ 平台预编译库指南

这里按桌面平台整理预置运行时库。下载 [xunlei-dlsdk-cpp-1.0.1.zip](https://github.com/xunlei-open/xunlei-dlsdk/releases/latest/download/xunlei-dlsdk-cpp-1.0.1.zip) 后，可以选择目标平台对应的目录。

当前支持的平台：

- `windows-x64`
- `windows-x86`
- `macos-universal`
- `linux-x64`

CMake 安装时会根据当前构建平台安装对应平台的库，并提供 `xunlei::dlsdk` target。

需要安装非当前构建平台的运行时库时，可以传入 `-DXL_DL_CPP_NATIVE_PLATFORM=<平台值>`，平台值使用上面的目录名。
